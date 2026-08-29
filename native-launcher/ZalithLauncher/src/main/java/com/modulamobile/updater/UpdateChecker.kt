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
                Log.d(TAG, "[UPDATE] Starting update check")
                
                // Fetch raw release.json directly
                val response = httpClient.get("https://github.com/Suraj-202211/Modula-Mobile/releases/latest/download/release.json")

                if (response.status.value != 200) {
                    Log.e(TAG, "[UPDATE] HTTP ${response.status.value}")
                    return@withContext null
                }

                val jsonString = response.bodyAsText()
                Log.d(TAG, "[UPDATE] Update JSON: $jsonString")

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
                val currentVersionName = BuildConfig.VERSION_NAME
                val packageName = context.packageName

                Log.d(TAG, "[UPDATE] Installed package: $packageName")
                Log.d(TAG, "[UPDATE] Installed versionCode: $currentVersion")
                Log.d(TAG, "[UPDATE] Installed versionName: $currentVersionName")
                Log.d(TAG, "[UPDATE] Remote versionCode: ${updateInfo.versionCode}")
                Log.d(TAG, "[UPDATE] Remote versionName: ${updateInfo.versionName}")
                Log.d(TAG, "[UPDATE] Remote release URL: ${updateInfo.apkUrl}")
                
                Log.d(TAG, "[UPDATE] Comparing remoteVersionCode=${updateInfo.versionCode} with installedVersionCode=$currentVersion")
                val isUpdateAvailable = updateInfo.versionCode > currentVersion
                Log.d(TAG, "[UPDATE] ${updateInfo.versionCode} > $currentVersion = $isUpdateAvailable")

                // Return update if newer version is available
                if (isUpdateAvailable) {
                    Log.d(TAG, "[UPDATE] Update available: " + updateInfo.versionName)
                    Log.d(TAG, "[UPDATE] Update check result: UPDATE_AVAILABLE")
                    updateInfo
                } else {
                    Log.d(TAG, "[UPDATE] Already up to date")
                    Log.d(TAG, "[UPDATE] Update check result: NO_UPDATE")
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
