package com.modulamobile.updater

import android.content.Context
import android.os.Build
import android.os.StatFs
import android.util.Base64
import android.util.Log
import com.movtery.zalithlauncher.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChecker @Inject constructor(
    private val httpClient: HttpClient,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "UpdateChecker"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun checkForUpdate(): UpdateInfo? =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Checking for updates...")

                // Fetch raw release.json directly
                val response = httpClient.get("https://github.com/Suraj-202211/Modula-Mobile/releases/latest/download/release.json")

                if (response.status.value != 200) {
                    Log.e(TAG, "HTTP ${response.status.value}")
                    return@withContext null
                }

                val jsonString = response.bodyAsText()
                Log.d(TAG, "Update JSON: $jsonString")

                // Parse RemoteData
                val remoteData = json.decodeFromString<com.movtery.zalithlauncher.upgrade.RemoteData>(jsonString)
                val file = remoteData.files.firstOrNull() ?: return@withContext null
                
                // Map to UpdateInfo
                val updateInfo = UpdateInfo(
                    versionCode = remoteData.code,
                    versionName = remoteData.version,
                    releaseNotes = remoteData.defaultBody.markdown.lines(),
                    mandatory = false,
                    apkUrl = file.uri,
                    apkSizeBytes = file.size,
                    apkSha256 = file.apkSha256 ?: "",
                    patchUrl = file.patchUri,
                    patchSizeBytes = file.patchSize,
                    patchSha256 = file.patchSha256,
                    patchFromVersionCode = file.patchForVersionCode ?: file.patchForVersionCodeLegacy,
                    patchFromSha256 = file.patchFromSha256
                )

                val currentVersion = BuildConfig.VERSION_CODE

                Log.d(TAG, "Current: $currentVersion Remote: ${updateInfo.versionCode} PatchFrom: ${updateInfo.patchFromVersionCode}")

                // Return update if newer version is available
                if (updateInfo.versionCode > currentVersion) {
                    Log.d(TAG, "Update available: " + updateInfo.versionName)
                    updateInfo
                } else {
                    Log.d(TAG, "Already up to date")
                    null
                }

            } catch (e: Exception) {
                Log.e(TAG, "Update check failed", e)
                null
            }
        }

    fun hasEnoughStorage(requiredBytes: Long): Boolean {
        val stat = StatFs(context.filesDir.path)
        val available = stat.availableBlocksLong * stat.blockSizeLong
        return available > requiredBytes * 2
    }

    fun canInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true
    }
}
