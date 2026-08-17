package com.modulamobile.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class FluxTheme {
    DEFAULT, ONYX, VOLCANIC, NEON, ARCTIC
}

object GlobalState {
    // Engine Metrics
    var ramAllocation by mutableStateOf(4096f) // Dynamic based on device RAM
    var fpsUnlock by mutableStateOf(false)
    var richPresence by mutableStateOf(true)
    var performanceMode by mutableStateOf(false)

    // Visual Fidelity
    var particleDensity by mutableStateOf(100f)
    var motionInterpolation by mutableStateOf(100f)
    var uiTransparency by mutableStateOf(100f)
    var uiScaling by mutableStateOf(100f) // 50 to 150
    var bloomEffects by mutableStateOf(true)
    var dynamicShadows by mutableStateOf(true)
    var autoUpdate by mutableStateOf(true)
    var showFPS by mutableStateOf(false)
    var enableSnapshots by mutableStateOf(false)
    var advancedDebug by mutableStateOf(false)
    var lowRamMode by mutableStateOf(false)
    var batterySaver by mutableStateOf(false)
    var gpuAcceleration by mutableStateOf(true)

    // System & Experimental
    var touchHaptics by mutableStateOf(true)
    var secureBoot by mutableStateOf(true)
    
    var jvmParameters by mutableStateOf("-Xmx4096M -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200")

    // UI Customization
    var activeTheme by mutableStateOf(FluxTheme.DEFAULT)
    var language by mutableStateOf("English (US)")

    // Voice Engine
    var fluxVoiceEnabled by mutableStateOf(true)
    var proximityChat by mutableStateOf(true)
    var noiseSuppression by mutableStateOf(true)
    var highBitrate by mutableStateOf(true)
}
