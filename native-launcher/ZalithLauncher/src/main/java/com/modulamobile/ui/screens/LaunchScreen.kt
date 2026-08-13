package com.modulamobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.modulamobile.ui.glass.*
import com.modulamobile.ui.theme.*

@Composable
fun LaunchScreen(
    onBack: () -> Unit
) {
    val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary
    
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(ColorBg0)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "MODULA LAUNCHER",
                    style = ModulaTypography.labelSmall.copy(letterSpacing = 4.sp, fontWeight = FontWeight.Black),
                    color = Color.White.copy(alpha=0.4f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GlassCard(
                variant = GlassVariant.DARK,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(themeAccentColor, RoundedCornerShape(24.dp))
                            .rotate(rotation),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("M", style = ModulaTypography.displayLarge.copy(fontSize = 40.sp, fontStyle = FontStyle.Italic), color = ColorBg1, modifier = Modifier.rotate(-15f))
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text("STARTING MINECRAFT", style = ModulaTypography.displayLarge.copy(fontSize = 24.sp, letterSpacing = (-1).sp), color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassBadge(text = "v1.20.1")
                        GlassBadge(text = "Fabric 0.16.0")
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Progress Bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("⚡ Initializing Modula Mobile Engine...", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black), color = TextSecondary)
                            Text("10%", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black), color = themeAccentColor)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.White.copy(alpha=0.05f), CircleShape).clip(CircleShape)) {
                            Box(modifier = Modifier.fillMaxWidth(0.1f).fillMaxHeight().background(themeAccentColor))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("312 / 312 FILES VERIFIED", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black), color = Color.White.copy(alpha=0.3f))
                            Text("12.4 MB/S", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black), color = Color.White.copy(alpha=0.3f))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Steps
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                listOf("Account", "Files", "JVM Args", "Bridge", "Ready").forEachIndexed { index, step ->
                    val isActive = index == 0
                    val isCompleted = false
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(12.dp).border(2.dp, if (isActive) themeAccentColor else Color.White.copy(alpha=0.2f), CircleShape).background(if (isCompleted) themeAccentColor else ColorBg1, CircleShape)) {
                            if (isActive) {
                                Box(modifier = Modifier.fillMaxSize().background(themeAccentColor, CircleShape))
                            }
                        }
                        Text(step.uppercase(), style = ModulaTypography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black), color = if (isActive) themeAccentColor else if (isCompleted) themeAccentColor.copy(alpha=0.6f) else Color.White.copy(alpha=0.2f))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // JVM Parameter Chain
            GlassCard(
                variant = GlassVariant.DARK,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha=0.03f)).border(1.dp, Color.White.copy(alpha=0.05f)).padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Settings, contentDescription = null, tint = themeAccentColor, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("JVM PARAMETER CHAIN", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black), color = Color.White.copy(alpha=0.5f))
                        }
                        GlassButton(
                            text = "COPY",
                            variant = GlassVariant.DARK,
                            onClick = { /* Copy */ },
                            modifier = Modifier.height(24.dp).padding(horizontal = 8.dp)
                        )
                    }
                    
                    Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha=0.4f)).padding(16.dp)) {
                        Text(
                            "java -Xmx4G -Xms512M -XX:+UseG1GC -Dminecraft.launcher.brand=ModulaMobile net.minecraft.client.main.Main",
                            style = ModulaTypography.labelSmall.copy(fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            color = Color.White.copy(alpha=0.6f)
                        )
                    }
                }
            }
        }
    }
}
