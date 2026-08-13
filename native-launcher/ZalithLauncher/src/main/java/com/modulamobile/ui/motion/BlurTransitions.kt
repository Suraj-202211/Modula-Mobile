package com.modulamobile.ui.motion

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.modulamobile.ui.theme.GoldBright
import com.modulamobile.ui.theme.GoldMid
import kotlinx.coroutines.delay

fun pushForwardEnter(): EnterTransition = slideInHorizontally(
    initialOffsetX = { (it * 0.3f).toInt() },
    animationSpec = tween(MotionTokens.DurationMedium, easing = MotionTokens.EaseOutExpo)
) + fadeIn(tween(MotionTokens.DurationMedium))

fun pushForwardExit(): ExitTransition = slideOutHorizontally(
    targetOffsetX = { -(it * 0.15f).toInt() },
    animationSpec = tween(MotionTokens.DurationMedium, easing = MotionTokens.EaseOutExpo)
) + fadeOut(tween(MotionTokens.DurationFast))

fun popBackEnter(): EnterTransition = slideInHorizontally(
    initialOffsetX = { -(it * 0.15f).toInt() },
    animationSpec = tween(MotionTokens.DurationMedium, easing = MotionTokens.EaseOutExpo)
) + fadeIn(tween(MotionTokens.DurationMedium))

fun popBackExit(): ExitTransition = slideOutHorizontally(
    targetOffsetX = { (it * 0.3f).toInt() },
    animationSpec = tween(MotionTokens.DurationMedium, easing = MotionTokens.EaseOutExpo)
) + fadeOut(tween(MotionTokens.DurationFast))

fun tabEnter(): EnterTransition = fadeIn(
    tween(MotionTokens.DurationFast, easing = MotionTokens.EaseInOutSine)
) + scaleIn(initialScale = 0.97f, animationSpec = tween(MotionTokens.DurationFast))

fun tabExit(): ExitTransition = fadeOut(
    tween(MotionTokens.DurationFast, easing = MotionTokens.EaseInOutSine)
) + scaleOut(targetScale = 0.97f, animationSpec = tween(MotionTokens.DurationFast))

fun modalEnter(): EnterTransition = slideInVertically(
    initialOffsetY = { it },
    animationSpec = tween(MotionTokens.DurationSlow, easing = MotionTokens.EaseOutExpo)
)

@Composable
fun GameLaunchTransition(active: Boolean, onComplete: () -> Unit) {
    val phase by animateIntAsState(
        targetValue = if (active) 4 else 0,
        animationSpec = keyframes {
            durationMillis = 700
            0 at 0
            1 at 200
            2 at 500
            3 at 650
            4 at 700
        }, label = "launchPhase"
    )

    if (active) {
        Box(Modifier.fillMaxSize()) {
            if (phase >= 1) {
                Canvas(Modifier.fillMaxSize()) {
                    drawRect(
                        Brush.radialGradient(
                            listOf(GoldBright.copy(alpha=0.9f), GoldMid.copy(alpha=0.4f), Color.Transparent),
                            center, size.maxDimension * 1.2f
                        )
                    )
                }
            }
            if (phase >= 2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Box(Modifier.fillMaxSize().graphicsLayer {
                    renderEffect = RenderEffect.createBlurEffect(40f, 40f, Shader.TileMode.CLAMP)
                        .asComposeRenderEffect()
                })
            }
            if (phase >= 3) {
                Box(Modifier.fillMaxSize().background(Color(0xFF060609)))
                LaunchedEffect(Unit) { delay(50); onComplete() }
            }
        }
    }
}

fun Modifier.staggerEntrance(index: Int, total: Int): Modifier = composed {
    val delay = (index * 35).coerceAtMost(280)
    val visible by produceState(false) {
        delay(delay.toLong())
        value = true
    }
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = tween(MotionTokens.DurationMedium, easing = MotionTokens.EaseOutExpo),
        label = "staggerOffset"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(MotionTokens.DurationMedium),
        label = "staggerAlpha"
    )
    this.offset(y = offsetY.dp).alpha(alpha)
}
