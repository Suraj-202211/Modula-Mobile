package com.movtery.zalithlauncher.ui.particles

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.movtery.zalithlauncher.ui.theme.*
import kotlinx.coroutines.isActive
import kotlin.math.*
import kotlin.random.Random

private data class Particle(
    var x: Float,
    var y: Float,
    val speed:    Float,
    val size:     Float,
    val sineFreq: Float,
    val sineAmp:  Float,
    val color:    Color,
    var opacity:  Float,
    val phase:    Float
)

@Composable
fun FluxParticleBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 0.6f
) {
    val count = (60 * intensity).toInt().coerceIn(0, 60)

    val particles = remember {
        List(count) {
            Particle(
                x       = Random.nextFloat(),
                y       = Random.nextFloat(),
                speed   = Random.nextFloat() *
                          0.0006f + 0.0002f,
                size    = Random.nextFloat() *
                          3f + 1.5f,
                sineFreq= Random.nextFloat() *
                          2f + 0.5f,
                sineAmp = Random.nextFloat() *
                          0.02f + 0.005f,
                color   = if (Random.nextBoolean())
                              FluxGold else FluxAmber,
                opacity = Random.nextFloat() *
                          0.4f + 0.1f,
                phase   = Random.nextFloat() * PI.toFloat() * 2f
            )
        }
    }

    var frameTime by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameMillis { time ->
                frameTime = time
            }
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val t = frameTime * 0.001f

        particles.forEach { p ->
            p.y -= p.speed
            if (p.y < -0.05f) {
                p.y = 1.05f
                p.x = Random.nextFloat()
            }
            val xOffset = sin(t * p.sineFreq + p.phase) *
                          p.sineAmp
            val fadeTop = (p.y * 5f).coerceIn(0f, 1f)
            val fadeBtm = ((1f - p.y) * 5f).coerceIn(0f,1f)
            val alpha   = p.opacity * fadeTop * fadeBtm

            drawCircle(
                color  = p.color.copy(alpha = alpha),
                radius = p.size,
                center = Offset(
                    (p.x + xOffset) * w,
                    p.y * h
                )
            )
        }
    }
}
