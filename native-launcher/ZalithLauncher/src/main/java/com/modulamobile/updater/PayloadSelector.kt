package com.modulamobile.updater

import android.content.Context
import android.util.Log
import java.io.File
import java.security.MessageDigest

object PayloadSelector {
    private const val TAG = "UPDATE"

    fun selectPayload(context: Context, info: UpdateInfo): DownloadPayload {
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
            Log.e(TAG, "Failed to get installed version code", e)
            -1
        }
        
        val sourceDir = context.applicationInfo.sourceDir
        val installedApkSha256 = calculateSha256(File(sourceDir))
        
        Log.d(TAG, "[UPDATE] Installed versionCode: $installedVersionCode")
        Log.d(TAG, "[UPDATE] Installed APK SHA-256: $installedApkSha256")
        Log.d(TAG, "[UPDATE] Remote versionCode: ${info.versionCode}")
        Log.d(TAG, "[UPDATE] Remote versionName: ${info.versionName}")
        
        Log.d(TAG, "[UPDATE] Patch from versionCode: ${info.patchFromVersionCode}")
        Log.d(TAG, "[UPDATE] Patch from SHA-256: ${info.patchFromSha256}")
        Log.d(TAG, "[UPDATE] Patch SHA-256: ${info.patchSha256}")
        Log.d(TAG, "[UPDATE] Patch size: ${info.patchSizeBytes}")

        val usePatch: Boolean
        var reason = ""

        if (info.patchUrl == null) {
            usePatch = false
            reason = "patchUri is null"
        } else if (info.patchSizeBytes == null || info.patchSha256 == null) {
            usePatch = false
            reason = "patch metadata incomplete"
        } else if (info.patchFromVersionCode != installedVersionCode) {
            usePatch = false
            reason = "patchFromCode ${info.patchFromVersionCode} does not match installed versionCode $installedVersionCode"
        } else if (info.patchFromSha256 != installedApkSha256) {
            usePatch = false
            reason = "installed APK SHA-256 does not match patchFromSha256"
        } else {
            usePatch = true
            reason = "exact patch match"
        }

        Log.d(TAG, "[UPDATE] Patch compatibility: $usePatch")
        Log.d(TAG, "[UPDATE] Patch compatibility reason: $reason")

        if (usePatch) {
            Log.d(TAG, "[UPDATE] Selected payload: PATCH")
            Log.d(TAG, "[UPDATE] Selected payload size: ${info.patchSizeBytes}")
            return DownloadPayload.Patch(
                url = info.patchUrl!!,
                sizeBytes = info.patchSizeBytes!!,
                sha256 = info.patchSha256!!,
                sourceVersionCode = info.patchFromVersionCode!!,
                sourceSha256 = info.patchFromSha256!!
            )
        } else {
            Log.d(TAG, "[UPDATE] Selected payload: FULL APK")
            Log.d(TAG, "[UPDATE] Full APK selected because: $reason")
            return DownloadPayload.FullApk(
                url = info.apkUrl,
                sizeBytes = info.apkSizeBytes,
                sha256 = info.apkSha256
            )
        }
    }

    fun calculateSha256(file: File): String? {
        if (!file.exists()) return null
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { fis ->
                val buffer = ByteArray(8192)
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
