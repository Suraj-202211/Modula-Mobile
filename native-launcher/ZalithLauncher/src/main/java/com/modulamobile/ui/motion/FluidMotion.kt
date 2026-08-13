package com.modulamobile.ui.motion

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlin.math.abs

fun Modifier.velocityBlur(lazyListState: LazyListState): Modifier = composed {
    val uiSettings = com.modulamobile.ui.state.LocalUiSettings.current

    val firstVisibleItemScrollOffset by remember {
        derivedStateOf { lazyListState.firstVisibleItemScrollOffset }
    }
    var prevOffset by remember { mutableIntStateOf(0) }
    var blurAmount by remember { mutableFloatStateOf(0f) }
    
    val animatedBlur by animateFloatAsState(
        targetValue = blurAmount,
        animationSpec = tween(if (uiSettings.performanceMode) 40 else 80, easing = LinearEasing),
        label = "blur"
    )

    LaunchedEffect(firstVisibleItemScrollOffset) {
        if (!uiSettings.performanceMode) {
            val velocity = abs(firstVisibleItemScrollOffset - prevOffset)
            blurAmount = (velocity * 0.06f).coerceIn(0f, 10f)
            prevOffset = firstVisibleItemScrollOffset
            delay(80)
            blurAmount = 0f
        }
    }

    if (!uiSettings.performanceMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.graphicsLayer {
            if (animatedBlur > 0.5f) {
                renderEffect = RenderEffect.createBlurEffect(
                    0.1f, animatedBlur, Shader.TileMode.CLAMP
                ).asComposeRenderEffect()
            }
        }
    } else this
}

fun Modifier.fluidPress(
    onClick: () -> Unit,
    haptic: HapticFeedback
): Modifier = composed {
    val uiSettings = com.modulamobile.ui.state.LocalUiSettings.current

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "scale"
    )
    val blurRadius by animateFloatAsState(
        targetValue = if (isPressed) 4f else 0f,
        animationSpec = tween(if (uiSettings.performanceMode) 40 else 80),
        label = "pressBlur"
    )
    
    this
        .scale(scale)
        .graphicsLayer {
            if (!uiSettings.performanceMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.5f) {
                renderEffect = RenderEffect.createBlurEffect(
                    blurRadius, blurRadius, Shader.TileMode.CLAMP
                ).asComposeRenderEffect()
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    tryAwaitRelease()
                    isPressed = false
                    onClick()
                }
            )
        }
}
