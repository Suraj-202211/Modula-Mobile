package com.modulamobile.updater

import android.content.Context
import android.os.Build
import android.util.Log
import com.movtery.zalithlauncher.BuildConfig
import java.io.File
import java.security.MessageDigest

object PayloadSelector {
    private const val TAG = "UPDATE"

    @Volatile
    private var cachedInstalledSha256: String? = null
    @Volatile
    private var cachedSourceDir: String? = null

    fun selectPayload(context: Context, info: UpdateInfo): DownloadPayload {
        val pm = context.packageManager
        var installedVersionName = BuildConfig.VERSION_NAME
        val installedVersionCode = try {
            val pkgInfo = pm.getPackageInfo(context.packageName, 0)
            installedVersionName = pkgInfo.versionName ?: BuildConfig.VERSION_NAME
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
        val installedApkSha256 = getOrCalculateInstalledSha256(sourceDir)
        val patchToVersionCode = info.patchToVersionCode ?: info.versionCode

        Log.d(TAG, "[UPDATE] ==============================")
        Log.d(TAG, "[UPDATE] OTA PAYLOAD SELECTION")
        Log.d(TAG, "[UPDATE] ==============================")

        Log.d(TAG, "[UPDATE] Installed versionName: $installedVersionName")
        Log.d(TAG, "[UPDATE] Installed versionCode: $installedVersionCode")

        Log.d(TAG, "[UPDATE] Remote versionName: ${info.versionName}")
        Log.d(TAG, "[UPDATE] Remote versionCode: ${info.versionCode}")

        Log.d(TAG, "[UPDATE] Full APK URI: ${info.apkUrl}")
        Log.d(TAG, "[UPDATE] Full APK size: ${info.apkSizeBytes}")
        Log.d(TAG, "[UPDATE] Full APK SHA-256: ${info.apkSha256}")

        Log.d(TAG, "[UPDATE] Patch URI: ${info.patchUrl ?: "null"}")
        Log.d(TAG, "[UPDATE] Patch size: ${info.patchSizeBytes ?: "null"}")
        Log.d(TAG, "[UPDATE] Patch from versionCode: ${info.patchFromVersionCode ?: "null"}")
        Log.d(TAG, "[UPDATE] Patch to versionCode: $patchToVersionCode")
        Log.d(TAG, "[UPDATE] Patch from SHA-256: ${info.patchFromSha256 ?: "null"}")
        Log.d(TAG, "[UPDATE] Patch to SHA-256: ${info.patchToSha256 ?: info.apkSha256}")
        Log.d(TAG, "[UPDATE] Patch SHA-256: ${info.patchSha256 ?: "null"}")

        Log.d(TAG, "[UPDATE] Installed APK SHA-256: ${installedApkSha256 ?: "null"}")

        // Individually evaluate checks per Part 2 specification
        val check1UpdateExists = info.versionCode > installedVersionCode
        Log.d(TAG, "[UPDATE] CHECK 1 - update exists: ${if (check1UpdateExists) "PASS" else "FAIL"}")

        val check2PatchUriExists = !info.patchUrl.isNullOrBlank()
        Log.d(TAG, "[UPDATE] CHECK 2 - patch URI exists: ${if (check2PatchUriExists) "PASS" else "FAIL"}")

        val check3SourceVersionMatches = info.patchFromVersionCode != null && info.patchFromVersionCode == installedVersionCode
        Log.d(TAG, "[UPDATE] CHECK 3 - source version matches: ${if (check3SourceVersionMatches) "PASS" else "FAIL"}")

        val check4TargetVersionMatches = patchToVersionCode == info.versionCode
        Log.d(TAG, "[UPDATE] CHECK 4 - target version matches: ${if (check4TargetVersionMatches) "PASS" else "FAIL"}")

        val check5ShaMatches = !info.patchFromSha256.isNullOrBlank() &&
                installedApkSha256 != null &&
                info.patchFromSha256.equals(installedApkSha256, ignoreCase = true)
        Log.d(TAG, "[UPDATE] CHECK 5 - installed APK SHA matches patch source: ${if (check5ShaMatches) "PASS" else "FAIL"}")

        val check6MetadataValid = info.patchSizeBytes != null && info.patchSizeBytes > 0 && !info.patchSha256.isNullOrBlank()
        Log.d(TAG, "[UPDATE] CHECK 6 - patch metadata valid: ${if (check6MetadataValid) "PASS" else "FAIL"}")

        val check7PatchSmaller = info.patchSizeBytes != null && info.apkSizeBytes > 0 && info.patchSizeBytes < info.apkSizeBytes
        Log.d(TAG, "[UPDATE] CHECK 7 - patch smaller than full APK: ${if (check7PatchSmaller) "PASS" else "FAIL"}")

        val isCompatible = check1UpdateExists && check2PatchUriExists && check3SourceVersionMatches &&
                check4TargetVersionMatches && check5ShaMatches && check6MetadataValid && check7PatchSmaller

        val failureReason = when {
            !check1UpdateExists -> "Update does not exist or remote versionCode (${info.versionCode}) <= installed ($installedVersionCode)"
            !check2PatchUriExists -> "patch URI does not exist or is null"
            !check3SourceVersionMatches -> "source version mismatch: patch requires ${info.patchFromVersionCode}, installed is $installedVersionCode"
            !check4TargetVersionMatches -> "target version mismatch: patch targets $patchToVersionCode, remote is ${info.versionCode}"
            !check5ShaMatches -> "installed APK SHA-256 ($installedApkSha256) does not match patchFromSha256 (${info.patchFromSha256})"
            !check6MetadataValid -> "patch metadata invalid: size=${info.patchSizeBytes}, sha=${info.patchSha256}"
            !check7PatchSmaller -> "patch size (${info.patchSizeBytes}) is not smaller than full APK (${info.apkSizeBytes})"
            else -> "none"
        }

        if (!isCompatible) {
            Log.d(TAG, "[UPDATE] FAILURE REASON: $failureReason")
        }

        Log.d(TAG, "[UPDATE] PATCH COMPATIBLE: $isCompatible")

        if (isCompatible) {
            Log.d(TAG, "[UPDATE] SELECTED PAYLOAD: PATCH")
            Log.d(TAG, "[UPDATE] SELECTED PAYLOAD SIZE: ${info.patchSizeBytes}")
            return DownloadPayload.Patch(
                url = info.patchUrl!!,
                sizeBytes = info.patchSizeBytes!!,
                sha256 = info.patchSha256!!,
                sourceVersionCode = info.patchFromVersionCode!!,
                targetVersionCode = patchToVersionCode,
                sourceSha256 = info.patchFromSha256!!
            )
        } else {
            Log.d(TAG, "[UPDATE] SELECTED PAYLOAD: FULL_APK")
            Log.d(TAG, "[UPDATE] SELECTED PAYLOAD SIZE: ${info.apkSizeBytes}")
            return DownloadPayload.FullApk(
                url = info.apkUrl,
                sizeBytes = info.apkSizeBytes,
                sha256 = info.apkSha256,
                targetVersionCode = info.versionCode
            )
        }
    }

    private fun getOrCalculateInstalledSha256(sourceDir: String): String? {
        val cached = cachedInstalledSha256
        if (cached != null && cachedSourceDir == sourceDir) {
            return cached
        }
        val calculated = calculateSha256(File(sourceDir))
        if (calculated != null) {
            cachedInstalledSha256 = calculated
            cachedSourceDir = sourceDir
        }
        return calculated
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
