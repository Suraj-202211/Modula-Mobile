package com.movtery.zalithlauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.ui.components.*
import com.movtery.zalithlauncher.ui.theme.*

@Composable
fun ProfileScreen(navController: NavHostController) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val username = account?.username ?: "Player"
    val isPremium = account?.accountType?.equals("Microsoft", ignoreCase = true) == true
    val context = LocalContext.current
    
    val playerSkin = remember { PlayerSkin(context) }
    var currentAnim by remember { mutableStateOf(ModelAnimation.NewIdle) }
    var animLabel by remember { mutableStateOf("STANDING") }

    DisposableEffect(Unit) {
        onDispose {
            playerSkin.destroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        
        // Glowing Avatar
        Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                                Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000)
                            )
                        )
                    )
                    .background(Color.Black.copy(alpha = 0.5f)) // To darken the inner part
            )
            AsyncImage(
                model = account?.getSkinFile()?.takeIf { it.exists() } ?: "https://mc-heads.net/avatar/$username/256",
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Bg4)
            )
            Box(
                Modifier
                    .size(20.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .clip(CircleShape)
                    .background(StateSuccess)
                    .border(2.dp, Bg1, CircleShape)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        Text(username.uppercase(), style = DisplayLG.copy(fontSize = 24.sp), color = TextHero, fontFamily = OrbitronFamily)
        Spacer(Modifier.height(8.dp))
        
        Box(modifier = Modifier.border(1.dp, TextSecondary.copy(0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
            Text("OFFLINE", style = LabelSM, color = TextSecondary)
        }
        
        Spacer(Modifier.height(32.dp))
        
        // Stats Cards
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FluxCard(variant = GlassVariant.DARK, modifier = Modifier.weight(1f), padding = 16.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("M", color = FluxGold, style = HeadingSM, fontFamily = OrbitronFamily)
                    Spacer(Modifier.height(8.dp))
                    Text("MEMBER SINCE", style = LabelSM, color = TextSecondary, fontFamily = OrbitronFamily)
                    Spacer(Modifier.height(4.dp))
                    Text("MAY 2026", style = TitleLG, color = TextHero, fontFamily = OrbitronFamily)
                }
            }
            FluxCard(variant = GlassVariant.DARK, modifier = Modifier.weight(1f), padding = 16.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("O", color = FluxGold, style = HeadingSM, fontFamily = OrbitronFamily)
                    Spacer(Modifier.height(8.dp))
                    Text("IDENTITY", style = LabelSM, color = TextSecondary, fontFamily = OrbitronFamily)
                    Spacer(Modifier.height(4.dp))
                    Text(if (isPremium) "PREMIUM" else "LOCAL", style = TitleLG, color = TextHero, fontFamily = OrbitronFamily)
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Skin Management Station
        FluxCard(variant = GlassVariant.DARK, padding = 0.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("O", color = FluxGold, style = BodySM)
                        Spacer(Modifier.width(8.dp))
                        Text("SKIN MANAGEMENT STATION", style = TitleLG.copy(fontSize = 12.sp), color = TextHero, fontFamily = OrbitronFamily)
                    }
                    Text("MANAGE", style = TitleLG.copy(fontSize = 12.sp), color = TextSecondary, fontFamily = OrbitronFamily)
                }
                
                HorizontalDivider(color = TextSecondary.copy(0.1f))
                
                // Content
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("FLUX ENGINE RENDERING", style = LabelSM.copy(fontSize = 10.sp), color = FluxGold, fontFamily = OrbitronFamily)
                    Spacer(Modifier.height(16.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                        AndroidView(
                            factory = { ctx ->
                                playerSkin.loadWebView(ctx) {
                                    if (account?.hasSkinFile == true) {
                                        playerSkin.loadSkin(account?.getSkinFile()?.inputStream(), account?.skinModelType)
                                    } else {
                                        playerSkin.loadSkin(skinId = account?.profileId, model = account?.skinModelType)
                                    }
                                    playerSkin.startAnim(currentAnim)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FluxButton(
                                label = "STAND", 
                                onClick = { currentAnim = ModelAnimation.NewIdle; animLabel = "STANDING"; playerSkin.startAnim(currentAnim) }, 
                                variant = FluxButtonVariant.GHOST
                            )
                            FluxButton(
                                label = "WALK", 
                                onClick = { currentAnim = ModelAnimation.Walking; animLabel = "WALKING"; playerSkin.startAnim(currentAnim) }, 
                                variant = FluxButtonVariant.GHOST
                            )
                            FluxButton(
                                label = "RUN", 
                                onClick = { currentAnim = ModelAnimation.Running; animLabel = "RUNNING"; playerSkin.startAnim(currentAnim) }, 
                                variant = FluxButtonVariant.GHOST
                            )
                        }
                        Text("$animLabel • UNTIL CUTOFF", style = LabelSM.copy(fontSize = 10.sp), color = FluxGold, fontFamily = OrbitronFamily)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { /* Sign Out Logic */ }
        ) {
            Text("<-", color = StateError, style = TitleLG)
            Spacer(Modifier.width(8.dp))
            Text("SIGN OUT SESSION", style = TitleLG, color = StateError, fontFamily = OrbitronFamily)
        }
        
        Spacer(Modifier.height(48.dp))
    }
}
