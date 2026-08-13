package com.movtery.zalithlauncher.upgrade

object BsPatch {
    init {
        System.loadLibrary("bspatch")
    }

    /**
     * Applies a bsdiff patch to an old file to generate a new file.
     * 
     * @param oldApkPath Path to the currently installed APK
     * @param newApkPath Path where the new APK should be written
     * @param patchPath Path to the downloaded patch file
     * @return 0 on success, non-zero on error
     */
    @JvmStatic
    external fun applyPatch(oldApkPath: String, newApkPath: String, patchPath: String): Int
}
