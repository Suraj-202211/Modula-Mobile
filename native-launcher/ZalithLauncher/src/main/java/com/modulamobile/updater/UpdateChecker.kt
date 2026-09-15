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
                
                // Fetch patches.json as authoritative historical patch index
                val patchesList = try {
                    val patchResp = httpClient.get("https://github.com/Suraj-202211/Modula-Mobile/releases/latest/download/patches.json")
                    if (patchResp.status.value == 200) {
                        val patchJsonStr = patchResp.bodyAsText()
                        json.decodeFromString<PatchIndex>(patchJsonStr).patches
                    } else {
                        Log.d(TAG, "[UPDATE] patches.json returned HTTP ${patchResp.status.value}")
                        emptyList()
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "[UPDATE] patches.json not available: ${e.message}")
                    emptyList()
                }

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
                    patchToVersionCode = file.patchToVersionCode ?: remoteData.code,
                    patchFromSha256 = file.patchFromSha256,
                    patchToSha256 = file.patchToSha256 ?: file.apkSha256,
                    patches = patchesList
                )

                val currentVersion = getInstalledVersionCode(context)
                val currentVersionName = getInstalledVersionName(context)
                val packageName = context.packageName

                Log.d(TAG, "[UPDATE] Installed package: $packageName")
                Log.d("OTA", "[OTA] Installed versionCode: $currentVersion")
                Log.d("OTA", "[OTA] Installed versionName: $currentVersionName")
                Log.d("OTA", "[OTA] Remote versionCode: ${updateInfo.versionCode}")
                Log.d("OTA", "[OTA] Remote versionName: ${updateInfo.versionName}")
                Log.d(TAG, "[UPDATE] Remote release URL: ${updateInfo.apkUrl}")
                
                Log.d(TAG, "[UPDATE] Comparing remoteVersionCode=${updateInfo.versionCode} with installedVersionCode=$currentVersion")
                val isUpdateAvailable = updateInfo.versionCode > currentVersion
                Log.d("OTA", "[OTA] Update available: $isUpdateAvailable")

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
        return available > requiredBytes
    }

    fun canInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true
    }

    companion object {
        private const val TAG = "UpdateChecker"

        fun getInstalledVersionCode(context: Context): Int {
            return try {
                val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pkgInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    pkgInfo.versionCode
                }
            } catch (e: Exception) {
                BuildConfig.VERSION_CODE
            }
        }

        fun getInstalledVersionName(context: Context): String {
            return try {
                val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pkgInfo.versionName ?: BuildConfig.VERSION_NAME
            } catch (e: Exception) {
                BuildConfig.VERSION_NAME
            }
        }
    }
}
