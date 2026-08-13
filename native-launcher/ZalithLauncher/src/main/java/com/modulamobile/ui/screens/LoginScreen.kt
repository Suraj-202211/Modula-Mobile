package com.modulamobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import com.modulamobile.ui.glass.*

import com.modulamobile.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.localLogin
import android.app.Activity

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as? Activity
    var isMicrosoftLogging by remember { mutableStateOf(false) }
    
    val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary

    Box(modifier = Modifier.fillMaxSize().background(ColorBg0)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // M Logo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(themeAccentColor.copy(alpha=0.1f), CircleShape)
                    .border(1.dp, themeAccentColor.copy(alpha=0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxSize().border(1.dp, themeAccentColor.copy(alpha=0.3f), CircleShape))
                Text("M", color = themeAccentColor, fontSize = 48.sp, fontWeight = FontWeight.Black)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("MODULA MOBILE", style = ModulaTypography.displayLarge.copy(fontSize = 32.sp, letterSpacing=(-1).sp), color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "NEXT-GEN MINECRAFT PERFORMANCE", 
                style = ModulaTypography.labelSmall.copy(letterSpacing = 4.sp, fontWeight=FontWeight.Black), 
                color = TextMuted
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            // Microsoft Card
            GlassCard(
                variant = GlassVariant.GOLD,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha=0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("MICROSOFT ACCOUNT", color = Color.White, style = ModulaTypography.titleLarge.copy(fontSize=18.sp))
                            Text("PREMIUM • ONLINE PLAY", color = Color.White.copy(alpha=0.7f), style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing=2.sp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    GlassButton(
                        text = if (isMicrosoftLogging) "AUTHENTICATING..." else "SIGN IN WITH MICROSOFT",
                        variant = GlassVariant.DARK,
                        onClick = {
                            Toast.makeText(context, "Microsoft login is temporarily disabled.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Divider
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha=0.1f)))
                Text("OR", color = TextMuted, style = ModulaTypography.labelSmall.copy(fontWeight=FontWeight.Bold, letterSpacing=2.sp), modifier = Modifier.padding(horizontal = 16.dp))
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha=0.1f)))
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // Offline Card
            GlassCard(
                variant = GlassVariant.DARK,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(ColorBg4, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = themeAccentColor, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("OFFLINE ACCOUNT", color = Color.White, style = ModulaTypography.titleLarge.copy(fontSize=18.sp))
                            Text("SINGLEPLAYER • CUSTOM SERVERS", color = TextMuted, style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing=2.sp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("USERNAME", color = TextMuted, style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, fontWeight=FontWeight.Bold, letterSpacing=2.sp))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    GlassTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = "Enter your name (3-16 chars)",
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    GlassGhostButton(
                        text = "PLAY OFFLINE",
                        onClick = {
                            if (username.isNotEmpty()) {
                                localLogin(username, null)
                                AccountsManager.reloadAccounts()
                                onLoginSuccess()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            // Footer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("OFFLINE MODE CANNOT ACCESS OFFICIAL SERVERS", color = Color(0xFFF44336), style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing=2.sp), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("ABOUT MODULAMC", color = themeAccentColor, style = ModulaTypography.labelSmall.copy(letterSpacing=2.sp), fontWeight = FontWeight.Bold)
        }
    }
}
