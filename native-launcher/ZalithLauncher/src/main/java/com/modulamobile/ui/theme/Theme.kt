package com.modulamobile.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.modulamobile.ui.settings.SettingsViewModel

@Composable
fun getDynamicColorScheme(themeName: String) = darkColorScheme(
    primary = when (themeName) {
        "DEFAULT" -> GoldBright
        "ONYX" -> OnyxAccent
        "VOLCANIC" -> VolcanicAccent
        "NEON" -> NeonAccent
        "ARCTIC" -> ArcticAccent
        else -> GoldBright
    },
    background = when (themeName) {
        "DEFAULT" -> ColorBg0
        "ONYX" -> OnyxCanvas
        "VOLCANIC" -> VolcanicCanvas
        "NEON" -> NeonCanvas
        "ARCTIC" -> ArcticCanvas
        else -> ColorBg0
    },
    surface = ColorBg1,
    surfaceVariant = ColorBg2,
    error = ColorError,
    onPrimary = ColorBg0,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    onError = ColorBg0
)

@Composable
fun ModulaTheme(
    viewModel: SettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val themeSelection by viewModel.themeSelection.collectAsState()
    val colorScheme = getDynamicColorScheme(themeSelection)
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ModulaTypography,
        content = content
    )
}
