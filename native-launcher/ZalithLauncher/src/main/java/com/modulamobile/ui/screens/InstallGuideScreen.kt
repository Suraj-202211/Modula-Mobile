package com.modulamobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.modulamobile.ui.glass.*
import com.modulamobile.ui.theme.*

@Composable
fun InstallGuideScreen(
    onBack: () -> Unit
) {
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
                    "INSTALL GUIDE",
                    style = ModulaTypography.labelSmall.copy(letterSpacing = 4.sp, fontWeight = FontWeight.Black),
                    color = com.modulamobile.ui.theme.LocalModulaColors.current.primary
                )
            }
            
            Text("HOW TO INSTALL", style = ModulaTypography.displayLarge.copy(fontSize = 32.sp, letterSpacing = (-1).sp), color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Follow these steps carefully", style = ModulaTypography.labelSmall.copy(letterSpacing = 3.sp, fontWeight = FontWeight.Bold), color = TextMuted)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                StepCard(
                    step = 1,
                    title = "Download the APK",
                    sub = "Tap the button below to get the Modula Mobile installer from GitHub.",
                    action = {
                        GlassButton(
                            text = "DOWNLOAD NOW",
                            variant = GlassVariant.DARK,
                            onClick = { /* Download */ },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )
                    }
                )
                
                StepCard(
                    step = 2,
                    title = "Open the APK file",
                    sub = "Once downloaded, open your 'Downloads' folder or notification shade and tap the ModulaMobile.apk file."
                )
                
                StepCard(
                    step = 3,
                    title = "Allow Installation",
                    sub = "If prompted by your browser, go to Settings → Apps → Special access → Install unknown apps → and toggle 'Allow' for your browser.",
                    tip = "Usually found in 'Advanced' or 'Special' settings"
                )
                
                StepCard(
                    step = 4,
                    title = "Launch Modula Mobile",
                    sub = "Open the app from your home screen, sign in, download your favorite version, and enjoy zero-stutter Minecraft!"
                )
                
                GlassCard(variant = GlassVariant.GOLD, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Account data is synced via Microsoft/Mojang. Your local worlds may need manual backup if moving files.",
                            style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black),
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepCard(step: Int, title: String, sub: String, tip: String? = null, action: @Composable (() -> Unit)? = null) {
    val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary
    GlassCard(
        variant = GlassVariant.DARK,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Box(
                    modifier = Modifier.size(40.dp).background(themeAccentColor.copy(alpha=0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(step.toString(), style = ModulaTypography.displayLarge.copy(fontSize = 20.sp), color = themeAccentColor)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title.uppercase(), style = ModulaTypography.titleLarge.copy(fontSize = 14.sp), color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(sub, style = ModulaTypography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
                    
                    if (tip != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.background(themeAccentColor.copy(alpha=0.1f), RoundedCornerShape(8.dp)).border(1.dp, themeAccentColor.copy(alpha=0.2f), RoundedCornerShape(8.dp)).padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, tint = themeAccentColor, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tip.uppercase(), style = ModulaTypography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black), color = themeAccentColor.copy(alpha=0.7f))
                        }
                    }
                }
            }
            if (action != null) {
                Spacer(modifier = Modifier.height(12.dp))
                action()
            }
        }
    }
}
