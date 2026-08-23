package com.modulamobile.updater

import android.content.Context
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.upgrade.BsPatch
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
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
        onProgress: (progress: Float, downloadedMb: Float, totalMb: Float, speedMbps: Float) -> Unit
    ): File = withContext(Dispatchers.IO) {

        val updateDir = File(context.filesDir, "updates")
        updateDir.mkdirs()

        // Clean old APKs first
        updateDir.listFiles()?.forEach {
            if (it.name.endsWith(".apk")) {
                it.delete()
            }
        }

        val usePatch = info.patchForVersionCode == BuildConfig.VERSION_CODE && info.patchUrl != null
        
        val apkFile = File(updateDir, "ModulaMobile-${info.versionName}.apk")
        val downloadFile = if (usePatch) File(updateDir, "ModulaMobile-${info.versionName}.patch") else apkFile
        
        if (downloadFile.exists()) {
            downloadFile.delete()
        }

        val response = httpClient.get(if (usePatch) info.patchUrl!! else info.apkUrl) {
            header("User-Agent", "ModulaMobile/${BuildConfig.VERSION_NAME}")
        }

        if (!response.status.isSuccess()) {
            throw IOException("Download failed: ${response.status.value}")
        }

        val totalBytes = response.contentLength() ?: if (usePatch) (info.patchSizeBytes ?: info.apkSizeBytes) else info.apkSizeBytes
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
                    val speed = ((downloadedBytes - lastBytes).toFloat() / 1024f / 1024f) / elapsed

                    onProgress(
                        downloadedBytes.toFloat() / totalBytes.toFloat(),
                        downloadedBytes.toFloat() / 1024f / 1024f,
                        totalBytes.toFloat() / 1024f / 1024f,
                        speed
                    )

                    lastTime = now
                    lastBytes = downloadedBytes
                }
            }
        }

        // Final progress update
        onProgress(
            1f,
            downloadedBytes.toFloat() / 1024f / 1024f,
            totalBytes.toFloat() / 1024f / 1024f,
            0f
        )

        if (usePatch) {
            val currentApkPath = context.applicationInfo.sourceDir
            val result = BsPatch.applyPatch(currentApkPath, apkFile.absolutePath, downloadFile.absolutePath)
            if (result != 0) {
                if (apkFile.exists()) apkFile.delete()
                throw IOException("Failed to apply bsdiff patch. Code: $result")
            }
            // Delete patch file after applying
            downloadFile.delete()
        }

        apkFile
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
