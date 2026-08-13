package com.modulamobile.ui.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

object MotionTokens {
    // Durations
    const val DurationFast    = 150  // micro interactions
    const val DurationMedium  = 280  // screen transitions
    const val DurationSlow    = 420  // complex reveals
    const val DurationXSlow   = 600  // hero animations

    // Easing curves
    val EaseOutExpo  = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val EaseOutBack  = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
    val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
    
    val SpringSnappy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMediumLow
    )
    val SpringSmooth = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness    = Spring.StiffnessLow
    )
}
