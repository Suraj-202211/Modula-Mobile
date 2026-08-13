package com.modulamobile.ui.state

import androidx.compose.runtime.staticCompositionLocalOf

data class ModulaUiSettings(
    val particleDensity: Float,
    val motionInterpolation: Float,
    val uiTransparency: Float,
    val uiScaling: Float,
    val bloomEnabled: Boolean,
    val shadowsEnabled: Boolean,
    val performanceMode: Boolean
)

val LocalUiSettings = staticCompositionLocalOf {
    ModulaUiSettings(
        particleDensity = 0.6f,
        motionInterpolation = 0.8f,
        uiTransparency = 0.1f,
        uiScaling = 1.0f,
        bloomEnabled = true,
        shadowsEnabled = true,
        performanceMode = false
    )
}
