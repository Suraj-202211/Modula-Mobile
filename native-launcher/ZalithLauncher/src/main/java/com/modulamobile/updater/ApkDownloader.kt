package com.modulamobile.updater

import android.content.Context
import android.util.Log
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.upgrade.BsPatch
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import io.ktor.client.plugins.timeout
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApkDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: HttpClient
) {
    companion object {
        private const val TAG = "UPDATE"
    }

    suspend fun download(
        info: UpdateInfo,
        payload: DownloadPayload = PayloadSelector.selectPayload(context, info),
        onStatus: (status: String) -> Unit = {},
        onProgress: (progress: Float, downloadedMb: Float, totalMb: Float, speedMbps: Float) -> Unit
    ): File = withContext(Dispatchers.IO) {

        val updateDir = File(context.filesDir, "updates")
        updateDir.mkdirs()

        // Clean old APKs and patches first
        updateDir.listFiles()?.forEach {
            if (it.name.endsWith(".apk") || it.name.endsWith(".patch")) {
                it.delete()
            }
        }
        
        suspend fun doDownload(currentPayload: DownloadPayload): File {
            val usePatch = currentPayload is DownloadPayload.Patch
            
            val downloadFile = File(updateDir, if (usePatch) "ModulaMobile-${info.versionName}.patch" else "ModulaMobile-${info.versionName}.apk")
            if (downloadFile.exists()) {
                downloadFile.delete()
            }

            if (usePatch) {
                Log.d(TAG, "[UPDATE] 1. PATCH DOWNLOAD START: ${currentPayload.url}")
            } else {
                Log.d(TAG, "[UPDATE] FULL APK DOWNLOAD START: ${currentPayload.url}")
            }

            httpClient.prepareGet(currentPayload.url) {
                header("User-Agent", "ModulaMobile/${BuildConfig.VERSION_NAME}")
                timeout {
                    requestTimeoutMillis = null
                }
            }.execute { response ->
                if (!response.status.isSuccess()) {
                    throw IOException("Download failed: HTTP ${response.status.value}")
                }

                val totalBytes = currentPayload.sizeBytes
                var downloadedBytes = 0L
                var lastTime = System.currentTimeMillis()
                var lastBytes = 0L

                FileOutputStream(downloadFile).use { output ->
                    val channel = response.bodyAsChannel()
                    val buffer = ByteArray(8192)
                    while (!channel.isClosedForRead) {
                        val read = channel.readAvailable(buffer, 0, buffer.size)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        val now = System.currentTimeMillis()
                        if (now - lastTime >= 200) {
                            val elapsed = (now - lastTime).toFloat() / 1000f
                            val speed = if (elapsed > 0f) ((downloadedBytes - lastBytes).toFloat() / 1024f / 1024f) / elapsed else 0f

                            onProgress(
                                if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes.toFloat() else 0f,
                                downloadedBytes.toFloat() / 1024f / 1024f,
                                totalBytes.toFloat() / 1024f / 1024f,
                                speed
                            )

                            lastTime = now
                            lastBytes = downloadedBytes
                        }
                    }
                }

                onProgress(
                    1f,
                    downloadedBytes.toFloat() / 1024f / 1024f,
                    totalBytes.toFloat() / 1024f / 1024f,
                    0f
                )
            }

            if (usePatch) {
                val patchPayload = currentPayload as DownloadPayload.Patch

                val currentApkFile = File(context.applicationInfo.sourceDir)
                val currentApkPath = currentApkFile.absolutePath
                val installedApkSha256 = calculateSha256(currentApkFile) ?: ""
                val actualPatchSha256 = calculateSha256(downloadFile) ?: ""
                val expectedSourceSha = info.patchFromSha256 ?: patchPayload.sourceSha256
                val expectedTargetSha = info.patchToSha256 ?: info.apkSha256

                // Section 1: Patch Processing Start
                Log.d("OTA-PATCH", "[OTA-PATCH] ===== PATCH PROCESSING START =====")
                Log.d("OTA-PATCH", "[OTA-PATCH] Installed APK path: $currentApkPath")
                Log.d("OTA-PATCH", "[OTA-PATCH] Installed APK exists: ${currentApkFile.exists()}")
                Log.d("OTA-PATCH", "[OTA-PATCH] Installed APK size: ${currentApkFile.length()}")
                Log.d("OTA-PATCH", "[OTA-PATCH] Installed APK SHA256: $installedApkSha256")

                Log.d("OTA-PATCH", "[OTA-PATCH] Patch path: ${downloadFile.absolutePath}")
                Log.d("OTA-PATCH", "[OTA-PATCH] Patch exists: ${downloadFile.exists()}")
                Log.d("OTA-PATCH", "[OTA-PATCH] Patch size actual: ${downloadFile.length()}")
                Log.d("OTA-PATCH", "[OTA-PATCH] Patch size expected: ${currentPayload.sizeBytes}")
                Log.d("OTA-PATCH", "[OTA-PATCH] Patch SHA256 actual: $actualPatchSha256")
                Log.d("OTA-PATCH", "[OTA-PATCH] Patch SHA256 expected: ${currentPayload.sha256}")

                Log.d("OTA-PATCH", "[OTA-PATCH] Expected source version: ${patchPayload.sourceVersionCode}")
                Log.d("OTA-PATCH", "[OTA-PATCH] Expected target version: ${patchPayload.targetVersionCode}")
                Log.d("OTA-PATCH", "[OTA-PATCH] Expected source SHA: $expectedSourceSha")
                Log.d("OTA-PATCH", "[OTA-PATCH] Expected target SHA: $expectedTargetSha")

                // Section 7: Version Chain Check
                val pm = context.packageManager
                val installedVersionCode = try {
                    val pkgInfo = pm.getPackageInfo(context.packageName, 0)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        pkgInfo.longVersionCode.toInt()
                    } else {
                        @Suppress("DEPRECATION")
                        pkgInfo.versionCode
                    }
                } catch (e: Exception) {
                    -1
                }

                if (patchPayload.sourceVersionCode != installedVersionCode) {
                    Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = VERSION_CHAIN")
                    Log.e("OTA-PATCH", "[OTA-PATCH] Installed version is incompatible with this patch: installed is $installedVersionCode, patch requires ${patchPayload.sourceVersionCode}")
                    downloadFile.delete()
                    throw IOException("FAILURE STAGE = VERSION_CHAIN: installed is $installedVersionCode, patch requires ${patchPayload.sourceVersionCode}")
                }

                if (!installedApkSha256.equals(expectedSourceSha, ignoreCase = true)) {
                    Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = VERSION_CHAIN")
                    Log.e("OTA-PATCH", "[OTA-PATCH] Installed APK SHA mismatch: installed is $installedApkSha256, required is $expectedSourceSha")
                    downloadFile.delete()
                    throw IOException("FAILURE STAGE = VERSION_CHAIN: installed SHA $installedApkSha256 != required $expectedSourceSha")
                }

                // Section 2: Patch SHA Verification
                val patchShaMatch = actualPatchSha256.equals(patchPayload.sha256, ignoreCase = true)
                if (!patchShaMatch) {
                    Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = PATCH_SHA256")
                    Log.e("OTA-PATCH", "[OTA-PATCH] Expected = ${patchPayload.sha256}")
                    Log.e("OTA-PATCH", "[OTA-PATCH] Actual = $actualPatchSha256")
                    downloadFile.delete()
                    throw IOException("FAILURE STAGE = PATCH_SHA256: expected ${patchPayload.sha256}, actual $actualPatchSha256")
                }

                val apkFile = File(updateDir, "ModulaMobile-${info.versionName}.apk")
                if (apkFile.exists()) apkFile.delete()

                // Section 3: BsPatch Execution
                onStatus("APPLYING")
                Log.d("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = NONE")
                Log.d("OTA-PATCH", "[OTA-PATCH] Starting BsPatch.applyPatch()")
                Log.d("OTA-PATCH", "[OTA-PATCH] Old APK = $currentApkPath")
                Log.d("OTA-PATCH", "[OTA-PATCH] New APK = ${apkFile.absolutePath}")
                Log.d("OTA-PATCH", "[OTA-PATCH] Patch = ${downloadFile.absolutePath}")

                val result = BsPatch.applyPatch(currentApkPath, apkFile.absolutePath, downloadFile.absolutePath)
                downloadFile.delete() // clean up patch file

                Log.d("OTA-PATCH", "[OTA-PATCH] BsPatch result code = $result")
                Log.d("OTA-PATCH", "[OTA-PATCH] Output exists = ${apkFile.exists()}")
                Log.d("OTA-PATCH", "[OTA-PATCH] Output size = ${if (apkFile.exists()) apkFile.length() else 0}")

                if (result != 0) {
                    Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = BSPATCH")
                    Log.e("OTA-PATCH", "[OTA-PATCH] BSPATCH RESULT CODE = $result")
                    if (apkFile.exists()) apkFile.delete()
                    throw IOException("FAILURE STAGE = BSPATCH: code $result")
                }

                if (!apkFile.exists() || apkFile.length() == 0L) {
                    Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = BSPATCH")
                    Log.e("OTA-PATCH", "[OTA-PATCH] Output APK missing or empty after bspatch")
                    throw IOException("FAILURE STAGE = BSPATCH: output APK missing or empty")
                }

                // Section 4: Output APK SHA256 Verification
                onStatus("VERIFYING")
                val actualTargetSha256 = calculateSha256(apkFile) ?: ""
                Log.d("OTA-PATCH", "[OTA-PATCH] Generated APK SHA256 actual = $actualTargetSha256")
                Log.d("OTA-PATCH", "[OTA-PATCH] Generated APK SHA256 expected = $expectedTargetSha")

                val targetShaMatch = actualTargetSha256.equals(expectedTargetSha, ignoreCase = true)
                if (!targetShaMatch) {
                    Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = OUTPUT_SHA256")
                    Log.e("OTA-PATCH", "[OTA-PATCH] THIS IS THE IMPORTANT FAILURE.")
                    apkFile.delete()
                    throw IOException("FAILURE STAGE = OUTPUT_SHA256: expected $expectedTargetSha, actual $actualTargetSha256")
                }

                // Section 5: Signature Verification
                Log.d("OTA-PATCH", "[OTA-PATCH] Signature verification starting")
                val sigMatch = SignatureVerifier.verifySignatures(context, apkFile)
                Log.d("OTA-PATCH", "[OTA-PATCH] Signature verification result = $sigMatch")

                if (!sigMatch) {
                    Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = SIGNATURE_VERIFICATION")
                    apkFile.delete()
                    throw IOException("FAILURE STAGE = SIGNATURE_VERIFICATION")
                }

                return apkFile
            } else {
                Log.d(TAG, "[UPDATE] Full APK download complete: ${downloadFile.length()} bytes")
                onStatus("VERIFYING")
                val actualSha = calculateSha256(downloadFile) ?: ""
                if (!actualSha.equals(currentPayload.sha256, ignoreCase = true)) {
                    downloadFile.delete()
                    throw IOException("Full APK SHA-256 verification failed (expected: ${currentPayload.sha256}, actual: $actualSha)")
                }
                if (!SignatureVerifier.verifySignatures(context, downloadFile)) {
                    downloadFile.delete()
                    throw IOException("Full APK signing certificate verification failed")
                }
                return downloadFile
            }
        }
        
        try {
            doDownload(payload)
        } catch (e: Exception) {
            Log.e("OTA-PATCH", "[OTA-PATCH] Exact Exception: ${e.message}", e)
            File(updateDir, "ModulaMobile-${info.versionName}.patch").delete()
            File(updateDir, "ModulaMobile-${info.versionName}.apk").delete()
            // Section 6: Temporary diagnostic behavior - do not fall back to 218.4 MB APK
            throw e
        }
    }

    suspend fun verifySha256(file: File, expected: String): Boolean = withContext(Dispatchers.IO) {
        val actual = calculateSha256(file) ?: return@withContext false
        actual.equals(expected, ignoreCase = true)
    }

    fun calculateSha256(file: File): String? {
        if (!file.exists()) return null
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { fis ->
                val buffer = ByteArray(65536)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate SHA-256 for ${file.absolutePath}", e)
            null
        }
    }
}

