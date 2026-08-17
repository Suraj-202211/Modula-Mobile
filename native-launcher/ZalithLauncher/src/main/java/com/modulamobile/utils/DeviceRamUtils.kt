package com.modulamobile.utils

import android.app.ActivityManager
import android.content.Context
import kotlin.math.roundToInt

object DeviceRamUtils {
    
    /**
     * Gets the total physical RAM of the device in Megabytes.
     */
    fun getTotalRamMb(context: Context): Int {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        return (memInfo.totalMem / (1024 * 1024)).toInt()
    }
    
    /**
     * Gets the total physical RAM of the device in Gigabytes.
     */
    fun getTotalRamGb(context: Context): Float {
        return getTotalRamMb(context) / 1024f
    }

    /**
     * Calculates the safe maximum RAM allocation based on total device RAM.
     * Prevents Android from killing the JVM process due to excessive memory usage.
     */
    fun getSafeMaxRamMb(totalMb: Int): Int {
        return when {
            totalMb <= 3072 -> 1024
            totalMb <= 4096 -> 1536
            totalMb <= 6144 -> 2048
            totalMb <= 8192 -> 3072
            totalMb <= 12288 -> 4096
            else -> 6144
        }
    }
}
