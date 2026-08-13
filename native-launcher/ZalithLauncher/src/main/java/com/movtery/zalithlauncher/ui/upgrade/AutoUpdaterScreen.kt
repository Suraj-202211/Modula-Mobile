package com.movtery.zalithlauncher.ui.upgrade

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.upgrade.BsPatch
import com.movtery.zalithlauncher.upgrade.RemoteData
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import io.ktor.utils.io.jvm.javaio.*

@Composable
fun AutoUpdaterScreen(
    data: RemoteData,
    file: RemoteData.RemoteFile,
    onDismissRequest: () -> Unit
) {
    var statusText by remember { mutableStateOf("Preparing update...") }
    var progress by remember { mutableFloatStateOf(0f) }
    var hasError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            try {
                val currentVersionCode = com.movtery.zalithlauncher.BuildConfig.VERSION_CODE
                val usePatch = file.patchUri != null &&
                               file.patchForVersionCode == currentVersionCode &&
                               file.patchSize != null &&
                               file.patchSize < (file.size * 0.7)

                val downloadUrl = if (usePatch) file.patchUri!! else file.uri
                
                statusText = "Downloading update..."
                val connection = java.net.URL(downloadUrl).openConnection()
                val contentLength = connection.contentLength
                
                val cacheDir = context.cacheDir
                val downloadFile = File(cacheDir, if (usePatch) "update.patch" else "update.apk")
                
                if (downloadFile.exists()) downloadFile.delete()
                
                connection.getInputStream().use { input ->
                    downloadFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead = input.read(buffer)
                        var bytesCopied = 0L
                        while (bytesRead != -1) {
                            output.write(buffer, 0, bytesRead)
                            bytesCopied += bytesRead
                            if (contentLength > 0) {
                                progress = bytesCopied.toFloat() / contentLength.toFloat()
                            }
                            bytesRead = input.read(buffer)
                        }
                    }
                }

                if (usePatch) {
                    statusText = "Verifying patch integrity..."
                    if (!verifySha256(downloadFile, file.patchSha256)) {
                        throw Exception("Patch SHA-256 verification failed! The download may be corrupted.")
                    }
                }
                
                val finalApkFile = if (usePatch) {
                    statusText = "Applying patch... (this may take a moment)"
                    progress = 0f
                    val currentApkPath = context.applicationInfo.sourceDir
                    val patchedApk = File(cacheDir, "patched_update.apk")
                    if (patchedApk.exists()) patchedApk.delete()
                    
                    val result = BsPatch.applyPatch(currentApkPath, patchedApk.absolutePath, downloadFile.absolutePath)
                    if (result != 0) {
                        throw Exception("Failed to apply bsdiff patch. Code: $result")
                    }
                    patchedApk
                } else {
                    downloadFile
                }
                
                statusText = "Verifying APK integrity..."
                if (!verifySha256(finalApkFile, file.apkSha256)) {
                    throw Exception("APK SHA-256 verification failed! The file may be corrupted.")
                }
                
                statusText = "Ready to install!"
                installApk(context, finalApkFile)
                onDismissRequest()
                
            } catch (e: Exception) {
                e.printStackTrace()
                hasError = true
                statusText = "Error: ${e.message}"
            }
        }
    }

    Dialog(
        onDismissRequest = { if (hasError) onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = hasError, 
            dismissOnClickOutside = hasError,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Modula Mobile Update", style = MaterialTheme.typography.titleLarge)
                if (!hasError) {
                    if (progress > 0f && progress < 1f) {
                        CircularProgressIndicator(progress = { progress })
                    } else {
                        CircularProgressIndicator()
                    }
                }
                Text(text = statusText, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun installApk(context: Context, apkFile: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        apkFile
    )
    
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    context.startActivity(intent)
}

private fun verifySha256(file: File, expectedHash: String?): Boolean {
    if (expectedHash == null) return true // skip check if hash is not provided
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    file.inputStream().use {
        val buffer = ByteArray(8192)
        var read = it.read(buffer)
        while (read != -1) {
            digest.update(buffer, 0, read)
            read = it.read(buffer)
        }
    }
    val hashBytes = digest.digest()
    val hashString = hashBytes.joinToString("") { "%02x".format(it) }
    return hashString.equals(expectedHash, ignoreCase = true)
}
