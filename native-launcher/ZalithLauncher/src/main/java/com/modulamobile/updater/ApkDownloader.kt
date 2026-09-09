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

                Log.d(TAG, "[UPDATE] 2. PATCH DOWNLOAD COMPLETE")
                val patchFileSize = downloadFile.length()
                Log.d(TAG, "[UPDATE] 3. PATCH FILE SIZE: $patchFileSize bytes")
                Log.d(TAG, "[UPDATE] 4. PATCH SHA-256 expected: ${patchPayload.sha256}")
                val actualPatchSha256 = calculateSha256(downloadFile) ?: ""
                Log.d(TAG, "[UPDATE] 5. PATCH SHA-256 actual: $actualPatchSha256")
                val patchShaMatch = actualPatchSha256.equals(patchPayload.sha256, ignoreCase = true)
                Log.d(TAG, "[UPDATE] 6. PATCH SHA-256 PASS/FAIL: ${if (patchShaMatch) "PASS" else "FAIL"}")

                if (!patchShaMatch) {
                    downloadFile.delete()
                    throw IOException("PATCH_FAILED: Patch SHA-256 mismatch (expected: ${patchPayload.sha256}, actual: $actualPatchSha256)")
                }

                val currentApkFile = File(context.applicationInfo.sourceDir)
                val currentApkPath = currentApkFile.absolutePath
                val currentApkSize = currentApkFile.length()
                Log.d(TAG, "[UPDATE] 7. INSTALLED APK PATH: $currentApkPath")
                Log.d(TAG, "[UPDATE] 8. INSTALLED APK SIZE: $currentApkSize bytes")
                val installedApkSha256 = calculateSha256(currentApkFile) ?: ""
                Log.d(TAG, "[UPDATE] 9. INSTALLED APK SHA-256: $installedApkSha256")

                val expectedSourceSha = info.patchFromSha256 ?: patchPayload.sourceSha256
                val expectedTargetSha = info.patchToSha256 ?: info.apkSha256
                Log.d(TAG, "[UPDATE] 10. PATCH SOURCE SHA-256 from metadata: $expectedSourceSha")
                Log.d(TAG, "[UPDATE] 11. PATCH TARGET SHA-256 from metadata: $expectedTargetSha")

                if (!installedApkSha256.equals(expectedSourceSha, ignoreCase = true)) {
                    downloadFile.delete()
                    throw IOException("PATCH_FAILED: Installed v1.0.20 SHA mismatch (installed: $installedApkSha256, required: $expectedSourceSha)")
                }

                val apkFile = File(updateDir, "ModulaMobile-${info.versionName}.apk")
                if (apkFile.exists()) apkFile.delete()
                Log.d(TAG, "[UPDATE] 12. TARGET APK PATH: ${apkFile.absolutePath}")

                onStatus("APPLYING")
                Log.d(TAG, "[UPDATE] 13. BsPatch START")
                val result = BsPatch.applyPatch(currentApkPath, apkFile.absolutePath, downloadFile.absolutePath)
                Log.d(TAG, "[UPDATE] 14. BsPatch RESULT CODE: $result")

                downloadFile.delete() // clean up patch file

                if (result != 0) {
                    if (apkFile.exists()) apkFile.delete()
                    throw IOException("PATCH_FAILED: bspatch returned code $result")
                }

                val generatedApkExists = apkFile.exists()
                Log.d(TAG, "[UPDATE] 15. GENERATED APK EXISTS: $generatedApkExists")
                if (!generatedApkExists) {
                    throw IOException("PATCH_FAILED: Output APK missing after bspatch")
                }

                val generatedApkSize = apkFile.length()
                Log.d(TAG, "[UPDATE] 16. GENERATED APK SIZE: $generatedApkSize bytes")
                Log.d(TAG, "[UPDATE] 17. GENERATED APK SHA-256 expected: $expectedTargetSha")

                onStatus("VERIFYING")
                val actualTargetSha256 = calculateSha256(apkFile) ?: ""
                Log.d(TAG, "[UPDATE] 18. GENERATED APK SHA-256 actual: $actualTargetSha256")
                val targetShaMatch = actualTargetSha256.equals(expectedTargetSha, ignoreCase = true)
                Log.d(TAG, "[UPDATE] 19. GENERATED APK SHA-256 PASS/FAIL: ${if (targetShaMatch) "PASS" else "FAIL"}")

                if (!targetShaMatch) {
                    apkFile.delete()
                    throw IOException("PATCH_FAILED: Result APK SHA-256 mismatch (expected: $expectedTargetSha, actual: $actualTargetSha256)")
                }

                val sigMatch = SignatureVerifier.verifySignatures(context, apkFile)
                Log.d(TAG, "[UPDATE] 20. SIGNATURE VERIFICATION PASS/FAIL: ${if (sigMatch) "PASS" else "FAIL"}")
                if (!sigMatch) {
                    apkFile.delete()
                    throw IOException("PATCH_FAILED: signing certificate verification failed")
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
            Log.e(TAG, "[UPDATE] 21. EXACT EXCEPTION MESSAGE: ${e.message}", e)
            if (payload is DownloadPayload.Patch) {
                val failureReason = e.message ?: "Unknown patch error"
                Log.e(TAG, "[UPDATE] PATCH FAILED — FALLING BACK TO FULL APK")
                Log.e(TAG, "[UPDATE] FAILURE REASON: $failureReason")
                
                File(updateDir, "ModulaMobile-${info.versionName}.patch").delete()
                File(updateDir, "ModulaMobile-${info.versionName}.apk").delete()
                
                val fallbackPayload = DownloadPayload.FullApk(
                    url = info.apkUrl,
                    sizeBytes = info.apkSizeBytes,
                    sha256 = info.apkSha256,
                    targetVersionCode = info.versionCode
                )
                doDownload(fallbackPayload)
            } else {
                throw e
            }
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

