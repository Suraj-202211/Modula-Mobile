package com.modulamobile.updater

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Log
import java.io.File
import java.security.MessageDigest

object SignatureVerifier {
    private const val TAG = "UPDATE"

    fun verifySignatures(context: Context, newApkFile: File): Boolean {
        return try {
            val pm = context.packageManager
            Log.d(TAG, "[SignatureVerifier] Verifying signature for target APK: ${newApkFile.absolutePath} (exists=${newApkFile.exists()}, size=${newApkFile.length()})")

            val currentSignatures = getInstalledAppSignatures(context, pm)
            if (currentSignatures.isEmpty()) {
                Log.e(TAG, "[SignatureVerifier] Could not extract signatures for installed app: ${context.packageName}")
                return false
            }

            val archiveSignatures = getArchiveSignatures(pm, newApkFile)
            if (archiveSignatures.isEmpty()) {
                Log.e(TAG, "[SignatureVerifier] Could not extract signatures for archive APK: ${newApkFile.absolutePath}")
                return false
            }

            val currentDigests = currentSignatures.map { computeSha256(it.toByteArray()) }
            val archiveDigests = archiveSignatures.map { computeSha256(it.toByteArray()) }

            Log.d(TAG, "[SignatureVerifier] Current app signatures (${currentDigests.size}): $currentDigests")
            Log.d(TAG, "[SignatureVerifier] Archive APK signatures (${archiveDigests.size}): $archiveDigests")

            val matches = currentDigests.any { cur -> archiveDigests.contains(cur) }
            Log.d(TAG, "[SignatureVerifier] Signature verification match = $matches")
            matches
        } catch (e: Exception) {
            Log.e(TAG, "[SignatureVerifier] Signature verification failed with exception", e)
            false
        }
    }

    private fun getInstalledAppSignatures(context: Context, pm: PackageManager): List<Signature> {
        val sigs = mutableListOf<Signature>()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val pkgInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = pkgInfo.signingInfo
                if (signingInfo != null) {
                    val certs = if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                    certs?.let { sigs.addAll(it) }
                }
            }
            if (sigs.isEmpty()) {
                @Suppress("DEPRECATION")
                val pkgInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                pkgInfo.signatures?.let { sigs.addAll(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[SignatureVerifier] Failed to read installed package signatures", e)
        }
        return sigs
    }

    private fun getArchiveSignatures(pm: PackageManager, apkFile: File): List<Signature> {
        val sigs = mutableListOf<Signature>()
        try {
            // Note: On Android P+, getPackageArchiveInfo with GET_SIGNING_CERTIFICATES often fails to populate signingInfo
            // due to a known Android OS platform issue (Google Issue Tracker #159537841 & #243324820).
            // Therefore, we query GET_SIGNATURES first, which works consistently across all Android versions.
            @Suppress("DEPRECATION")
            val pkgInfoSignatures = pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNATURES)
            @Suppress("DEPRECATION")
            pkgInfoSignatures?.signatures?.let {
                if (it.isNotEmpty()) sigs.addAll(it)
            }

            // Fallback 1: Try GET_SIGNING_CERTIFICATES if available and list is still empty
            if (sigs.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val pkgInfoSigning = pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = pkgInfoSigning?.signingInfo
                if (signingInfo != null) {
                    val certs = if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                    certs?.let { sigs.addAll(it) }
                }
            }

            // Fallback 2: Combined flags
            if (sigs.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                @Suppress("DEPRECATION")
                val combinedFlags = PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_SIGNATURES
                @Suppress("DEPRECATION")
                val pkgInfoCombined = pm.getPackageArchiveInfo(apkFile.absolutePath, combinedFlags)
                val signingInfo = pkgInfoCombined?.signingInfo
                if (signingInfo != null) {
                    val certs = if (signingInfo.hasMultipleSigners()) signingInfo.apkContentsSigners else signingInfo.signingCertificateHistory
                    certs?.let { sigs.addAll(it) }
                }
                if (sigs.isEmpty()) {
                    @Suppress("DEPRECATION")
                    pkgInfoCombined?.signatures?.let { sigs.addAll(it) }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[SignatureVerifier] Failed to read archive signatures", e)
        }
        return sigs
    }

    private fun computeSha256(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
