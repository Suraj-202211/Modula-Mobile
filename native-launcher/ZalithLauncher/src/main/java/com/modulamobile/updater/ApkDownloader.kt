package com.modulamobile.updater

import android.content.Context
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
    suspend fun download(
        info: UpdateInfo,
        payload: DownloadPayload = PayloadSelector.selectPayload(context, info),
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
                if (!verifySha256(downloadFile, currentPayload.sha256)) {
                    downloadFile.delete()
                    throw IOException("Patch SHA-256 verification failed")
                }
                
                val currentApkPath = context.applicationInfo.sourceDir
                val apkFile = File(updateDir, "ModulaMobile-${info.versionName}.apk")
                if (apkFile.exists()) apkFile.delete()
                
                val result = BsPatch.applyPatch(currentApkPath, apkFile.absolutePath, downloadFile.absolutePath)
                downloadFile.delete() // remove patch file after applying
                
                if (result != 0) {
                    if (apkFile.exists()) apkFile.delete()
                    throw IOException("Failed to apply bsdiff patch. Code: $result")
                }
                
                if (!apkFile.exists()) {
                    throw IOException("Output APK missing after bspatch")
                }
                
                if (!verifySha256(apkFile, info.apkSha256)) {
                    apkFile.delete()
                    throw IOException("Result APK SHA-256 verification failed (expected: ${info.apkSha256})")
                }
                
                if (!SignatureVerifier.verifySignatures(context, apkFile)) {
                    apkFile.delete()
                    throw IOException("Signing certificate verification failed")
                }
                
                return apkFile
            } else {
                if (!verifySha256(downloadFile, currentPayload.sha256)) {
                    downloadFile.delete()
                    throw IOException("Full APK SHA-256 verification failed")
                }
                if (!SignatureVerifier.verifySignatures(context, downloadFile)) {
                    downloadFile.delete()
                    throw IOException("Signing certificate verification failed")
                }
                return downloadFile
            }
        }
        
        try {
            doDownload(payload)
        } catch (e: Exception) {
            android.util.Log.e("UPDATE", "Download failed", e)
            if (payload is DownloadPayload.Patch) {
                android.util.Log.w("UPDATE", "[UPDATE] PATCH FAILED: ${e.message}")
                android.util.Log.i("UPDATE", "[UPDATE] Falling back to FULL APK")
                
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
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        actual.equals(expected, ignoreCase = true)
    }
}
