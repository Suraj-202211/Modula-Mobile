package com.modulamobile.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ModulaColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val bgBase: Color,
    val bgSurface: Color
)

val LocalModulaColors = staticCompositionLocalOf<ModulaColors> {
    error("No ModulaColors provided")
}

object ThemeManager {
    fun getColors(themeName: String): ModulaColors {
        return when (themeName) {
            "ONYX" -> ModulaColors(OnyxAccent, Color(0xFF64748B), Color(0xFF94A3B8), OnyxCanvas, ColorBg1)
            "VOLCANIC" -> ModulaColors(VolcanicAccent, Color(0xFFF87171), Color(0xFFFCA5A5), VolcanicCanvas, ColorBg1)
            "NEON" -> ModulaColors(NeonAccent, Color(0xFFE879F9), Color(0xFFF0ABFC), NeonCanvas, ColorBg1)
            "ARCTIC" -> ModulaColors(ArcticAccent, Color(0xFF7DD3FC), Color(0xFFBAE6FD), ArcticCanvas, ColorBg1)
            else -> ModulaColors(GoldBright, GoldMid, GoldDeep, ColorBg0, ColorBg1)
        }
    }
}
