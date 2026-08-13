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

                // Fetch file content from GitHub API
                val response = httpClient.get(UpdateConfig.UPDATE_CHECK_URL) {
                    header("Authorization", "Bearer ${UpdateConfig.GITHUB_TOKEN}")
                    header("Accept", "application/vnd.github.v3+json")
                    header("X-GitHub-Api-Version", "2022-11-28")
                }

                if (response.status.value != 200) {
                    Log.e(TAG, "HTTP ${response.status.value}")
                    return@withContext null
                }

                // Parse GitHub API response
                val fileContent = json.decodeFromString<GitHubFileContent>(response.bodyAsText())

                // Decode base64 content
                val decodedBytes = Base64.decode(
                    fileContent.content.replace("\n", ""),
                    Base64.DEFAULT
                )
                val jsonString = String(decodedBytes)

                Log.d(TAG, "Update JSON: $jsonString")

                // Parse update info
                val updateInfo = json.decodeFromString<UpdateInfo>(jsonString)

                val currentVersion = BuildConfig.VERSION_CODE

                Log.d(TAG, "Current: $currentVersion Remote: ${updateInfo.versionCode}")

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
