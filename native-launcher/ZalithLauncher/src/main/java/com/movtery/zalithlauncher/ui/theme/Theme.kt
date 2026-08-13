package com.movtery.zalithlauncher.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val ModulaDarkColorScheme = darkColorScheme(
    primary          = FluxGold,
    onPrimary        = TextOnGold,
    primaryContainer = Bg4,
    secondary        = FluxAmber,
    onSecondary      = TextOnGold,
    tertiary         = FluxCopper,
    background       = Bg1,
    onBackground     = TextPrimary,
    surface          = Bg2,
    onSurface        = TextPrimary,
    surfaceVariant   = Bg3,
    onSurfaceVariant = TextSecondary,
    outline          = Glow20,
    outlineVariant   = Glow10,
    error            = StateError,
    onError          = TextHero
)

@Composable
fun ModulaMobileTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ModulaDarkColorScheme,
        content     = content
    )
}

@Composable
fun ZalithLauncherTheme(
    backgroundViewModel: com.movtery.zalithlauncher.viewmodel.BackgroundViewModel? = null,
    festivals: List<Any>? = null,
    content: @Composable () -> Unit
) {
    ModulaMobileTheme(content)
}
