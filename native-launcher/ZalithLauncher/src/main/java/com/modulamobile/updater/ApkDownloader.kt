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

        try {
            return@withContext when (payload) {
                is DownloadPayload.FullApk -> processFullApk(payload, info, updateDir, onStatus, onProgress)
                is DownloadPayload.Patch -> processSinglePatch(payload, info, updateDir, onStatus, onProgress)
                is DownloadPayload.PatchChain -> processPatchChain(payload, info, updateDir, onStatus, onProgress)
            }
        } catch (e: Exception) {
            Log.e("OTA-PATCH", "[OTA-PATCH] Exact Exception: ${e.message}", e)
            // Cleanup
            updateDir.listFiles()?.forEach {
                if (it.name.endsWith(".apk") || it.name.endsWith(".patch")) {
                    it.delete()
                }
            }
            throw e
        }
    }

    private suspend fun processFullApk(
        payload: DownloadPayload.FullApk,
        info: UpdateInfo,
        updateDir: File,
        onStatus: (status: String) -> Unit,
        onProgress: (progress: Float, downloadedMb: Float, totalMb: Float, speedMbps: Float) -> Unit
    ): File {
        val downloadFile = File(updateDir, "ModulaMobile-${info.versionName}.apk")
        if (downloadFile.exists()) downloadFile.delete()

        Log.d(TAG, "[UPDATE] FULL APK DOWNLOAD START: ${payload.url}")

        downloadFileContent(payload.url, downloadFile, payload.sizeBytes) { downloaded, total, speed ->
            val p = if (total > 0) downloaded.toFloat() / total.toFloat() else 0f
            onProgress(p, downloaded / 1024f / 1024f, total / 1024f / 1024f, speed)
        }

        Log.d(TAG, "[UPDATE] Full APK download complete: ${downloadFile.length()} bytes")
        onStatus("VERIFYING")
        val actualSha = calculateSha256(downloadFile) ?: ""
        if (!actualSha.equals(payload.sha256, ignoreCase = true)) {
            throw IOException("Full APK SHA-256 verification failed (expected: ${payload.sha256}, actual: $actualSha)")
        }
        if (!SignatureVerifier.verifySignatures(context, downloadFile)) {
            throw IOException("Full APK signing certificate verification failed")
        }
        return downloadFile
    }

    private suspend fun processSinglePatch(
        payload: DownloadPayload.Patch,
        info: UpdateInfo,
        updateDir: File,
        onStatus: (status: String) -> Unit,
        onProgress: (progress: Float, downloadedMb: Float, totalMb: Float, speedMbps: Float) -> Unit
    ): File {
        val patchFile = File(updateDir, "ModulaMobile-${info.versionName}.patch")
        if (patchFile.exists()) patchFile.delete()

        Log.d(TAG, "[UPDATE] 1. PATCH DOWNLOAD START: ${payload.url}")

        downloadFileContent(payload.url, patchFile, payload.sizeBytes) { downloaded, total, speed ->
            val p = if (total > 0) downloaded.toFloat() / total.toFloat() else 0f
            onProgress(p, downloaded / 1024f / 1024f, total / 1024f / 1024f, speed)
        }

        val currentApkFile = File(context.applicationInfo.sourceDir)
        val expectedSourceSha = info.patchFromSha256 ?: payload.sourceSha256
        val expectedTargetSha = info.patchToSha256 ?: info.apkSha256

        return applyPatchAndVerify(
            currentApkFile,
            patchFile,
            payload.sourceVersionCode,
            expectedSourceSha,
            payload.sha256,
            expectedTargetSha,
            updateDir,
            "ModulaMobile-${info.versionName}.apk",
            onStatus
        )
    }

    private suspend fun processPatchChain(
        payload: DownloadPayload.PatchChain,
        info: UpdateInfo,
        updateDir: File,
        onStatus: (status: String) -> Unit,
        onProgress: (progress: Float, downloadedMb: Float, totalMb: Float, speedMbps: Float) -> Unit
    ): File {
        Log.d(TAG, "[UPDATE] PATCH CHAIN START: ${payload.patches.size} patches")
        var currentApkFile = File(context.applicationInfo.sourceDir)
        var totalDownloadedBytes = 0L
        val totalChainBytes = payload.sizeBytes

        val finalApkName = "ModulaMobile-${info.versionName}.apk"
        
        for ((index, patchEntry) in payload.patches.withIndex()) {
            val isLast = index == payload.patches.lastIndex
            val stepName = "patch_${patchEntry.fromVersionCode}_to_${patchEntry.toVersionCode}"
            val patchFile = File(updateDir, "$stepName.patch")
            if (patchFile.exists()) patchFile.delete()

            Log.d(TAG, "[UPDATE] Downloading chain patch ${index + 1}/${payload.patches.size}: ${patchEntry.patchUrl}")
            
            downloadFileContent(patchEntry.patchUrl, patchFile, patchEntry.patchSizeBytes) { downloaded, _, speed ->
                val currentTotalDownloaded = totalDownloadedBytes + downloaded
                val p = if (totalChainBytes > 0) currentTotalDownloaded.toFloat() / totalChainBytes.toFloat() else 0f
                onProgress(p, currentTotalDownloaded / 1024f / 1024f, totalChainBytes / 1024f / 1024f, speed)
            }
            totalDownloadedBytes += patchEntry.patchSizeBytes

            val targetApkName = if (isLast) finalApkName else "temp_apk_${patchEntry.toVersionCode}.apk"
            
            val newApkFile = applyPatchAndVerify(
                currentApkFile,
                patchFile,
                patchEntry.fromVersionCode,
                patchEntry.sourceSha256,
                patchEntry.patchSha256,
                patchEntry.targetSha256,
                updateDir,
                targetApkName,
                onStatus,
                verifySignature = isLast // Only verify signature on the final APK
            )
            
            // If this wasn't the first step (meaning we used a temp apk), delete the old temp apk
            if (index > 0) {
                currentApkFile.delete()
            }
            currentApkFile = newApkFile
        }

        return currentApkFile
    }

    private suspend fun applyPatchAndVerify(
        sourceApkFile: File,
        patchFile: File,
        expectedSourceVersionCode: Int,
        expectedSourceSha: String,
        expectedPatchSha: String,
        expectedTargetSha: String,
        updateDir: File,
        targetApkName: String,
        onStatus: (status: String) -> Unit,
        verifySignature: Boolean = true
    ): File {
        val installedApkSha256 = calculateSha256(sourceApkFile) ?: ""
        val actualPatchSha256 = calculateSha256(patchFile) ?: ""

        Log.d("OTA-PATCH", "[OTA-PATCH] ===== PATCH PROCESSING =====")
        Log.d("OTA-PATCH", "[OTA-PATCH] Source APK: ${sourceApkFile.absolutePath}")
        Log.d("OTA-PATCH", "[OTA-PATCH] Source APK SHA256: $installedApkSha256 (Expected: $expectedSourceSha)")
        Log.d("OTA-PATCH", "[OTA-PATCH] Patch: ${patchFile.absolutePath}")
        Log.d("OTA-PATCH", "[OTA-PATCH] Patch SHA256 actual: $actualPatchSha256 (Expected: $expectedPatchSha)")

        // Version chain check only applies if the source is the originally installed APK
        if (sourceApkFile.absolutePath == context.applicationInfo.sourceDir) {
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

            if (expectedSourceVersionCode != installedVersionCode) {
                Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = VERSION_CHAIN")
                throw IOException("FAILURE STAGE = VERSION_CHAIN: installed is $installedVersionCode, patch requires $expectedSourceVersionCode")
            }
        }

        if (!installedApkSha256.equals(expectedSourceSha, ignoreCase = true)) {
            Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = VERSION_CHAIN")
            throw IOException("FAILURE STAGE = VERSION_CHAIN: source SHA $installedApkSha256 != required $expectedSourceSha")
        }

        val patchShaMatch = actualPatchSha256.equals(expectedPatchSha, ignoreCase = true)
        if (!patchShaMatch) {
            Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = PATCH_SHA256")
            throw IOException("FAILURE STAGE = PATCH_SHA256: expected $expectedPatchSha, actual $actualPatchSha256")
        }

        val targetApkFile = File(updateDir, targetApkName)
        if (targetApkFile.exists()) targetApkFile.delete()

        onStatus("APPLYING")
        Log.d("OTA-PATCH", "[OTA-PATCH] Starting BsPatch.applyPatch()")
        
        val result = BsPatch.applyPatch(sourceApkFile.absolutePath, targetApkFile.absolutePath, patchFile.absolutePath)
        patchFile.delete()

        Log.d("OTA-PATCH", "[OTA-PATCH] BsPatch result code = $result")

        if (result != 0) {
            Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = BSPATCH")
            if (targetApkFile.exists()) targetApkFile.delete()
            throw IOException("FAILURE STAGE = BSPATCH: code $result")
        }

        if (!targetApkFile.exists() || targetApkFile.length() == 0L) {
            Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = BSPATCH")
            throw IOException("FAILURE STAGE = BSPATCH: output APK missing or empty")
        }

        onStatus("VERIFYING")
        val actualTargetSha256 = calculateSha256(targetApkFile) ?: ""
        Log.d("OTA-PATCH", "[OTA-PATCH] Generated APK SHA256 actual = $actualTargetSha256 (Expected: $expectedTargetSha)")

        val targetShaMatch = actualTargetSha256.equals(expectedTargetSha, ignoreCase = true)
        if (!targetShaMatch) {
            Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = OUTPUT_SHA256")
            targetApkFile.delete()
            throw IOException("FAILURE STAGE = OUTPUT_SHA256: expected $expectedTargetSha, actual $actualTargetSha256")
        }

        if (verifySignature) {
            Log.d("OTA-PATCH", "[OTA-PATCH] Signature verification starting")
            val sigMatch = SignatureVerifier.verifySignatures(context, targetApkFile)
            Log.d("OTA-PATCH", "[OTA-PATCH] Signature verification result = $sigMatch")

            if (!sigMatch) {
                Log.e("OTA-PATCH", "[OTA-PATCH] FAILURE STAGE = SIGNATURE_VERIFICATION")
                targetApkFile.delete()
                throw IOException("FAILURE STAGE = SIGNATURE_VERIFICATION")
            }
        }

        return targetApkFile
    }

    private suspend fun downloadFileContent(
        url: String,
        targetFile: File,
        totalBytes: Long,
        onProgress: (downloaded: Long, total: Long, speedMbps: Float) -> Unit
    ) {
        httpClient.prepareGet(url) {
            header("User-Agent", "ModulaMobile/${BuildConfig.VERSION_NAME}")
            timeout {
                requestTimeoutMillis = null
            }
        }.execute { response ->
            if (!response.status.isSuccess()) {
                throw IOException("Download failed: HTTP ${response.status.value}")
            }

            var downloadedBytes = 0L
            var lastTime = System.currentTimeMillis()
            var lastBytes = 0L

            FileOutputStream(targetFile).use { output ->
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
                        onProgress(downloadedBytes, totalBytes, speed)
                        lastTime = now
                        lastBytes = downloadedBytes
                    }
                }
            }
            onProgress(downloadedBytes, totalBytes, 0f)
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
