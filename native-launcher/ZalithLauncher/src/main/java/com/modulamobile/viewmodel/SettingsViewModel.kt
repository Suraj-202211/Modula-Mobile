package com.modulamobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulamobile.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val ramAllocationMb = settingsRepository.ramAllocationMb
    val jvmArgs = settingsRepository.jvmArgs

    fun setRamAllocation(mb: Int) {
        viewModelScope.launch {
            settingsRepository.setRamAllocation(mb)
        }
    }

    fun setJvmArgs(args: String) {
        viewModelScope.launch {
            settingsRepository.setJvmArgs(args)
        }
    }

    // A helper method to calculate Max RAM based on device specifications
    fun getMaxRamMb(): Int {
        val runtime = Runtime.getRuntime()
        // Approximate total device memory (for a better implementation, use ActivityManager.MemoryInfo)
        // Here we just use the max memory exposed to the JVM process as a proxy, or hardcode 8192 MB for now
        // For accurate device RAM:
        // val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // val memInfo = ActivityManager.MemoryInfo()
        // actManager.getMemoryInfo(memInfo)
        // return (memInfo.totalMem / (1024 * 1024)).toInt()
        
        return (runtime.maxMemory() / (1024 * 1024)).toInt().coerceAtLeast(4096) 
    }
}
