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
        val installedApkFile = File(sourceDir)
        val installedApkSha256 = getOrCalculateInstalledSha256(sourceDir)
        val patchToVersionCode = info.patchToVersionCode ?: info.versionCode

        // STEP 1 — LOG INSTALLED VERSION
        Log.d("OTA-DIAG", "[OTA-DIAG] Installed versionCode = $installedVersionCode")
        Log.d("OTA-DIAG", "[OTA-DIAG] Installed versionName = $installedVersionName")
        Log.d("OTA-DIAG", "[OTA-DIAG] Installed APK sourceDir = $sourceDir")
        Log.d("OTA-DIAG", "[OTA-DIAG] Installed APK SHA-256 = $installedApkSha256")
        Log.d("OTA-DIAG", "[OTA-DIAG] Installed APK size = ${if (installedApkFile.exists()) installedApkFile.length() else 0}")

        // STEP 2 — LOG COMPLETE REMOTE v1.0.22 METADATA
        Log.d("OTA-DIAG", "[OTA-DIAG] Remote versionCode = ${info.versionCode}")
        Log.d("OTA-DIAG", "[OTA-DIAG] Remote versionName = ${info.versionName}")
        Log.d("OTA-DIAG", "[OTA-DIAG] Full APK URL = ${info.apkUrl}")
        Log.d("OTA-DIAG", "[OTA-DIAG] Full APK size = ${info.apkSizeBytes}")
        Log.d("OTA-DIAG", "[OTA-DIAG] Full APK SHA-256 = ${info.apkSha256}")

        Log.d("OTA-DIAG", "[OTA-DIAG] Patch URL = ${info.patchUrl ?: "null"}")
        Log.d("OTA-DIAG", "[OTA-DIAG] Patch size = ${info.patchSizeBytes ?: "null"}")
        Log.d("OTA-DIAG", "[OTA-DIAG] Patch SHA-256 = ${info.patchSha256 ?: "null"}")
        Log.d("OTA-DIAG", "[OTA-DIAG] patchFromVersionCode = ${info.patchFromVersionCode ?: "null"}")
        Log.d("OTA-DIAG", "[OTA-DIAG] patchToVersionCode = $patchToVersionCode")
        Log.d("OTA-DIAG", "[OTA-DIAG] patchForVersionCode = ${info.patchFromVersionCode ?: "null"}")
        Log.d("OTA-DIAG", "[OTA-DIAG] patchFromSha256 = ${info.patchFromSha256 ?: "null"}")
        Log.d("OTA-DIAG", "[OTA-DIAG] patchToSha256 = ${info.patchToSha256 ?: info.apkSha256}")

        // STEP 3 — LOG EVERY PAYLOAD SELECTOR CHECK
        val check1UpdateExists = info.versionCode > installedVersionCode
        Log.d("OTA-DIAG", "[OTA-DIAG] CHECK 1 update exists = ${if (check1UpdateExists) "PASS" else "FAIL"}")

        val check2PatchUriExists = !info.patchUrl.isNullOrBlank()
        Log.d("OTA-DIAG", "[OTA-DIAG] CHECK 2 patch URI exists = ${if (check2PatchUriExists) "PASS" else "FAIL"}")

        val check3SourceVersionMatches = info.patchFromVersionCode != null && info.patchFromVersionCode == installedVersionCode
        Log.d("OTA-DIAG", "[OTA-DIAG] CHECK 3 source version matches = ${if (check3SourceVersionMatches) "PASS" else "FAIL"}")

        val check4TargetVersionMatches = patchToVersionCode == info.versionCode
        Log.d("OTA-DIAG", "[OTA-DIAG] CHECK 4 target version matches = ${if (check4TargetVersionMatches) "PASS" else "FAIL"}")

        val check5ShaMatches = !info.patchFromSha256.isNullOrBlank() &&
                installedApkSha256 != null &&
                info.patchFromSha256.equals(installedApkSha256, ignoreCase = true)
        Log.d("OTA-DIAG", "[OTA-DIAG] CHECK 5 installed APK SHA matches patch source SHA = ${if (check5ShaMatches) "PASS" else "FAIL"}")

        val check6MetadataValid = info.patchSizeBytes != null && info.patchSizeBytes > 0 && !info.patchSha256.isNullOrBlank()
        Log.d("OTA-DIAG", "[OTA-DIAG] CHECK 6 patch metadata valid = ${if (check6MetadataValid) "PASS" else "FAIL"}")

        val check7PatchSmaller = info.patchSizeBytes != null && info.apkSizeBytes > 0 && info.patchSizeBytes < info.apkSizeBytes
        Log.d("OTA-DIAG", "[OTA-DIAG] CHECK 7 patch smaller than full APK = ${if (check7PatchSmaller) "PASS" else "FAIL"}")

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

        // STEP 4 — VERSION-CHAIN DIAGNOSIS
        if (!check3SourceVersionMatches && info.patchFromVersionCode != null) {
            Log.d("OTA-DIAG", "[OTA-DIAG] PATCH INCOMPATIBLE: installed v$installedVersionName (code $installedVersionCode), patch requires versionCode ${info.patchFromVersionCode}")
        }

        Log.d("OTA-DIAG", "[OTA-DIAG] FINAL PAYLOAD = ${if (isCompatible) "PATCH" else "FULL"}")
        if (!isCompatible) {
            Log.d("OTA-DIAG", "[OTA-DIAG] REJECTION REASON: $failureReason")
        }

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
