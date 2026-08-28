package com.modulamobile.updater

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.SigningInfo
import android.os.Build
import android.util.Log
import java.io.File

object SignatureVerifier {
    private const val TAG = "UPDATE"

    fun verifySignatures(context: Context, newApkFile: File): Boolean {
        return try {
            val pm = context.packageManager
            val currentPkgInfo = pm.getPackageInfo(
                context.packageName,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else @Suppress("DEPRECATION") PackageManager.GET_SIGNATURES
            )
            
            val newPkgInfo = pm.getPackageArchiveInfo(
                newApkFile.absolutePath,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else @Suppress("DEPRECATION") PackageManager.GET_SIGNATURES
            ) ?: return false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val currentSigningInfo = currentPkgInfo.signingInfo
                val newSigningInfo = newPkgInfo.signingInfo
                if (currentSigningInfo == null || newSigningInfo == null) return false
                
                val currentSignatures = if (currentSigningInfo.hasMultipleSigners()) currentSigningInfo.apkContentsSigners else currentSigningInfo.signingCertificateHistory
                val newSignatures = if (newSigningInfo.hasMultipleSigners()) newSigningInfo.apkContentsSigners else newSigningInfo.signingCertificateHistory
                
                if (currentSignatures.isNullOrEmpty() || newSignatures.isNullOrEmpty()) return false
                
                // For simplicity, verify that at least one matching signature exists
                currentSignatures.any { currentSig ->
                    newSignatures.any { newSig -> currentSig == newSig }
                }
            } else {
                @Suppress("DEPRECATION")
                val currentSignatures = currentPkgInfo.signatures
                @Suppress("DEPRECATION")
                val newSignatures = newPkgInfo.signatures
                
                if (currentSignatures.isNullOrEmpty() || newSignatures.isNullOrEmpty()) return false
                
                currentSignatures.any { currentSig ->
                    newSignatures.any { newSig -> currentSig == newSig }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Signature verification failed", e)
            false
        }
    }
}
