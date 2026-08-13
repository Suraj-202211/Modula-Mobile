package com.modulamobile.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulamobile.data.settings.SettingsRepository
import com.movtery.zalithlauncher.setting.AllSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    // GROUP A: ENGINE SETTINGS (Directly wraps AllSettings)
    private val _ramAllocation = MutableStateFlow(AllSettings.ramAllocation.state)
    val ramAllocation = _ramAllocation.asStateFlow()

    private val _unlockFps = MutableStateFlow(AllSettings.unlockFps.state)
    val unlockFps = _unlockFps.asStateFlow()

    private val _jvmArgs = MutableStateFlow(AllSettings.jvmArgs.state)
    val jvmArgs = _jvmArgs.asStateFlow()

    private val _renderer = MutableStateFlow(AllSettings.renderer.state)
    val renderer = _renderer.asStateFlow()

    private val _sustainedPerformance = MutableStateFlow(AllSettings.sustainedPerformance.state)
    val sustainedPerformance = _sustainedPerformance.asStateFlow()

    private val _bigCoreAffinity = MutableStateFlow(AllSettings.bigCoreAffinity.state)
    val bigCoreAffinity = _bigCoreAffinity.asStateFlow()

    private val _useSurfaceView = MutableStateFlow(AllSettings.useSurfaceView.state)
    val useSurfaceView = _useSurfaceView.asStateFlow()

    fun setRamAllocation(value: Int) {
        AllSettings.ramAllocation.save(value)
        _ramAllocation.value = value
    }

    fun setUnlockFps(value: Boolean) {
        AllSettings.unlockFps.save(value)
        _unlockFps.value = value
    }

    fun setJvmArgs(value: String) {
        AllSettings.jvmArgs.save(value)
        _jvmArgs.value = value
    }

    fun setRenderer(value: String) {
        AllSettings.renderer.save(value)
        _renderer.value = value
    }

    fun setPerformanceMode(enabled: Boolean) {
        AllSettings.sustainedPerformance.save(enabled)
        AllSettings.bigCoreAffinity.save(enabled)
        AllSettings.useSurfaceView.save(!enabled)
        
        _sustainedPerformance.value = enabled
        _bigCoreAffinity.value = enabled
        _useSurfaceView.value = !enabled
    }

    // GROUP B: MODULA-ONLY COSMETIC SETTINGS (DataStore)
    val themeSelection = repository.themeSelection.stateIn(viewModelScope, SharingStarted.Eagerly, "DEFAULT")
    val particleDensity = repository.particleDensity.stateIn(viewModelScope, SharingStarted.Eagerly, 60)
    val motionInterpolation = repository.motionInterpolation.stateIn(viewModelScope, SharingStarted.Eagerly, 80)
    val uiTransparency = repository.uiTransparency.stateIn(viewModelScope, SharingStarted.Eagerly, 10)
    val uiScaling = repository.uiScaling.stateIn(viewModelScope, SharingStarted.Eagerly, 85)
    val bloomEffects = repository.bloomEffects.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val dynamicShadows = repository.dynamicShadows.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val richPresence = repository.richPresence.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val showFpsCounter = repository.showFpsCounter.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val enableSnapshots = repository.enableSnapshots.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val advancedDebug = repository.advancedDebug.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val languageSelection = repository.languageSelection.stateIn(viewModelScope, SharingStarted.Eagerly, "ENGLISH (US)")
    
    val lowRamMode = repository.lowRamMode.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val batterySaver = repository.batterySaver.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    
    val autoUpdate = repository.autoUpdate.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val gpuAcceleration = repository.gpuAcceleration.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setThemeSelection(value: String) = viewModelScope.launch { repository.setThemeSelection(value) }
    fun setParticleDensity(value: Int) = viewModelScope.launch { repository.setParticleDensity(value) }
    fun setMotionInterpolation(value: Int) = viewModelScope.launch { repository.setMotionInterpolation(value) }
    fun setUiTransparency(value: Int) = viewModelScope.launch { repository.setUiTransparency(value) }
    fun setUiScaling(value: Int) = viewModelScope.launch { 
        AllSettings.uiScaling.save(value)
        repository.setUiScaling(value) 
    }
    fun setBloomEffects(value: Boolean) = viewModelScope.launch { repository.setBloomEffects(value) }
    fun setDynamicShadows(value: Boolean) = viewModelScope.launch { repository.setDynamicShadows(value) }
    fun setRichPresence(value: Boolean) = viewModelScope.launch { repository.setRichPresence(value) }
    fun setShowFpsCounter(value: Boolean) = viewModelScope.launch { 
        AllSettings.showFPS.save(value) // Triggers in-game HUD FPS counter visibility
        repository.setShowFpsCounter(value) 
    }
    fun setEnableSnapshots(value: Boolean) = viewModelScope.launch { repository.setEnableSnapshots(value) }
    fun setAdvancedDebug(value: Boolean) = viewModelScope.launch { 
        AllSettings.advancedDebug.save(value)
        repository.setAdvancedDebug(value) 
    }
    fun setLanguageSelection(value: String) = viewModelScope.launch { repository.setLanguageSelection(value) }
    
    fun setAutoUpdate(value: Boolean) = viewModelScope.launch {
        repository.setAutoUpdate(value)
        if (value) {
            // Schedule UpdateCheckWorker
            // val workRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(6, TimeUnit.HOURS).build()
            // WorkManager.getInstance(context).enqueueUniquePeriodicWork("UpdateCheck", ExistingPeriodicWorkPolicy.KEEP, workRequest)
        } else {
            // WorkManager.getInstance(context).cancelUniqueWork("UpdateCheck")
        }
    }
    
    fun setGpuAcceleration(value: Boolean) = viewModelScope.launch {
        AllSettings.gpuAcceleration.save(value)
        repository.setGpuAcceleration(value)
    }

    // GROUP C: CROSS-GROUP SIDE EFFECTS
    fun setLowRamMode(value: Boolean) = viewModelScope.launch {
        repository.setLowRamMode(value)
        if (value) {
            setRamAllocation(1024)
            repository.setParticleDensity(0)
            repository.setBloomEffects(false)
            repository.setDynamicShadows(false)
        }
    }

    fun setBatterySaver(value: Boolean) = viewModelScope.launch {
        repository.setBatterySaver(value)
        if (value) {
            setUnlockFps(false)
            repository.setMotionInterpolation(30)
            repository.setParticleDensity(30) // Reduce particles (20% logic approx)
            repository.setBloomEffects(false)
            repository.setRichPresence(false) // Disable rich presence
            
            // Kill Discord RPC
            com.modulamobile.discord.DiscordRPCManager.shutdown()
            
            // Pause WorkManager
            // WorkManager.getInstance(context).cancelUniqueWork("UpdateCheck")
        }
    }

    fun resetToDefaults() = viewModelScope.launch {
        setRamAllocation(4096)
        setUnlockFps(true)
        setJvmArgs("-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=150")
        setAutoUpdate(true)
        setGpuAcceleration(true)
        setPerformanceMode(false)
        
        repository.setThemeSelection("DEFAULT")
        repository.setParticleDensity(60)
        repository.setMotionInterpolation(80)
        repository.setUiTransparency(10)
        repository.setUiScaling(85)
        repository.setBloomEffects(true)
        repository.setDynamicShadows(true)
        repository.setRichPresence(true)
        repository.setShowFpsCounter(false)
        repository.setEnableSnapshots(false)
        repository.setAdvancedDebug(false)
        repository.setLanguageSelection("English (US)")
        repository.setLowRamMode(false)
        repository.setBatterySaver(false)
        
        AllSettings.touchHaptics.save(true)
        AllSettings.secureBoot.save(true)
    }
}
