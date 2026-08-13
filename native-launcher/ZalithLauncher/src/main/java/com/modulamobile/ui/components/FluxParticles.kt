package com.modulamobile.ui.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.modulamobile.ui.theme.*
import com.modulamobile.ui.state.GlobalState
import com.modulamobile.ui.state.FluxTheme
import kotlinx.coroutines.isActive
import androidx.compose.runtime.withFrameMillis
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var speedY: Float,
    var radius: Float,
    var baseAlpha: Float,
    var colorProgress: Float,
    var phase: Float
)

@Composable
fun FluxParticles(modifier: Modifier = Modifier) {
    val uiSettings = com.modulamobile.ui.state.LocalUiSettings.current
    val modulaColors = com.modulamobile.ui.theme.LocalModulaColors.current

    if (uiSettings.particleDensity <= 0.05f) {
        return // No particles if density is ~0
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var isVisible by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) isVisible = false
            else if (event == Lifecycle.Event.ON_START) isVisible = true
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Determine particle count based on density slider and performance mode
    val maxParticles = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 100 else 40
    val targetParticles = (maxParticles * uiSettings.particleDensity).toInt()
    val activeCount = if (uiSettings.performanceMode) minOf(targetParticles, 15) else targetParticles
    
    // We instantiate the max array size once and only process/render activeCount
    val particles = remember {
        Array(maxParticles) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speedY = Random.nextFloat() * 0.9f + 0.3f,
                radius = Random.nextFloat() * 2.5f + 1.5f,
                baseAlpha = Random.nextFloat() * 0.55f + 0.15f,
                colorProgress = Random.nextFloat(),
                phase = Random.nextFloat() * 10f
            )
        }
    }

    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isVisible, uiSettings.performanceMode) {
        if (!isVisible) return@LaunchedEffect
        var lastFrameTime = withFrameMillis { it }
        val speedMultiplier = if (uiSettings.performanceMode) 0.5f else 1f

        while (isActive) {
            withFrameMillis { frameTime ->
                val delta = ((frameTime - lastFrameTime) / 16f) * speedMultiplier
                lastFrameTime = frameTime
                time = frameTime

                for (i in 0 until activeCount) {
                    val particle = particles[i]
                    particle.y -= particle.speedY * delta * 0.005f
                    particle.phase += delta * 0.05f
                    particle.x += sin(particle.phase) * 0.001f * delta

                    if (particle.y < -0.1f) {
                        particle.y = 1.1f
                        particle.x = Random.nextFloat()
                    }
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        time.hashCode()
        val w = size.width
        val h = size.height

        val themeColorStart = modulaColors.primary
        val themeColorEnd = modulaColors.secondary

        for (i in 0 until activeCount) {
            val particle = particles[i]
            val drawY = particle.y * h
            val drawX = particle.x * w
            
            val edgeFade = when {
                particle.y > 0.9f -> (1f - particle.y) * 10f
                particle.y < 0.1f -> particle.y * 10f
                else -> 1f
            }
            val currentAlpha = (particle.baseAlpha * edgeFade).coerceIn(0f, 1f)
            val color = lerp(themeColorStart, themeColorEnd, particle.colorProgress)

            drawCircle(
                color = color.copy(alpha = currentAlpha),
                radius = particle.radius,
                center = Offset(drawX, drawY)
            )
        }
    }
}
