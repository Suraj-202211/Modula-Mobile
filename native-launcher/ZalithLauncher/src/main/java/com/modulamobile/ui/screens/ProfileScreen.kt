package com.modulamobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import coil3.compose.AsyncImage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.movtery.zalithlauncher.ui.components.PlayerSkin
import com.movtery.zalithlauncher.ui.components.ModelAnimation

import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.SkinResolver
import com.movtery.zalithlauncher.setting.AllSettings
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import com.modulamobile.ui.theme.*
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.game.account.wardrobe.isSlimModel
import com.modulamobile.ui.glass.*

@Composable
fun ProfileScreen(
    onBack: () -> Unit
) {
    val currentAccount by AccountsManager.currentAccountFlow.collectAsState(initial = null)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val accountViewModel: com.movtery.zalithlauncher.viewmodel.AccountManageViewModel = hiltViewModel()
    
    var showCustomSkinDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var showSkinEditor by remember { mutableStateOf(false) }
    var customSkinUrlInput by remember { mutableStateOf(AllSettings.customSkinUrl.state) }
    
    val skinPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            customSkinUrlInput = uri.toString()
            coroutineScope.launch {
                AllSettings.customSkinUrl.save(uri.toString())
            }
            
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val account = currentAccount ?: return@launch
                val cacheFile = java.io.File(
                    com.movtery.zalithlauncher.path.PathManager.DIR_IMAGE_CACHE,
                    "skin_pick_${java.util.UUID.randomUUID()}"
                )
                runCatching {
                    context.copyLocalFile(uri, cacheFile)
                    com.movtery.zalithlauncher.game.account.wardrobe.validateSkinFile(cacheFile)
                }.onSuccess { isValid ->
                    if (isValid) {
                        val recommendedModel = if (cacheFile.isSlimModel()) {
                            com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType.ALEX
                        } else {
                            com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType.STEVE
                        }
                        accountViewModel.onIntent(
                            com.movtery.zalithlauncher.viewmodel.AccountManageIntent.ApplySkin(
                                account = account,
                                file = cacheFile,
                                model = recommendedModel
                            )
                        )
                    }
                }
            }
        }
    }

    val username = currentAccount?.username ?: "Player"
    val isMicrosoft = currentAccount?.accountType == "MICROSOFT"
    var avatarUrl by remember(currentAccount) { mutableStateOf<String?>(null) }
    
    LaunchedEffect(currentAccount) {
        avatarUrl = SkinResolver.getAvatarUrl(currentAccount)
    }

    val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary

    Box(modifier = Modifier.fillMaxSize().background(ColorBg0)) {

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 40.dp, bottom = 100.dp)
        ) {
            item {
                // Avatar Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(128.dp)
                                .border(2.dp, if (isMicrosoft) themeAccentColor else Color.White.copy(alpha=0.1f), CircleShape)
                                .background(ColorBg2, CircleShape)
                                .clip(CircleShape)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUrl != null) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(ColorBg2),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = "Avatar",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .size(32.dp)
                                .border(4.dp, ColorBg1, CircleShape)
                                .background(Color(0xFF4CAF50), CircleShape)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(username.uppercase(), style = ModulaTypography.displayLarge.copy(fontSize = 28.sp, letterSpacing = (-1).sp), color = Color.White)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassBadge(text = if (isMicrosoft) "MICROSOFT" else "OFFLINE")
                        if (isMicrosoft) {
                            GlassBadge(text = "PREMIUM")
                        }
                    }
                }
            }

            item {
                // Stats Cards
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard("MEMBER SINCE", "MAY 2026", Icons.Rounded.CalendarToday, Modifier.weight(1f))
                    StatCard("IDENTITY", "LOCAL", Icons.Rounded.Badge, Modifier.weight(1f))
                }
            }

            item {
                // Skin Management Station
                GlassCard(
                    variant = GlassVariant.DARK,
                    modifier = Modifier.fillMaxWidth().height(400.dp)
                ) {
                    Column {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(Color.White.copy(alpha=0.03f))
                                .border(1.dp, Color.White.copy(alpha=0.05f))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Person, contentDescription = "Skin", tint = themeAccentColor, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SKIN MANAGEMENT STATION", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing=2.sp, fontWeight = FontWeight.Bold), color = Color.White.copy(alpha=0.5f))
                            }
                            
                            GlassButton(
                                text = if (showSkinEditor) "CLOSE" else "MANAGE",
                                variant = GlassVariant.DARK,
                                onClick = { showSkinEditor = !showSkinEditor },
                                modifier = Modifier.height(28.dp).padding(horizontal = 8.dp)
                            )
                        }
                        
                        // Body
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Black.copy(alpha=0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            // 3D Skin Area
                            com.modulamobile.ui.components.SkinViewer(
                                username = currentAccount?.username ?: "steve",
                                uuid = if (currentAccount?.accountType.equals("Microsoft", ignoreCase = true)) currentAccount?.profileId else null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            
                            Box(
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = showSkinEditor
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .width(200.dp)
                                            .fillMaxHeight()
                                            .background(ColorBg1.copy(alpha=0.9f))
                                            .border(1.dp, Color.White.copy(alpha=0.05f))
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("SIMULATION", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black), color = Color.White.copy(alpha=0.3f))
                                            
                                            listOf("standing", "walking", "running").forEach { anim ->
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color.White.copy(alpha=0.05f), RoundedCornerShape(8.dp))
                                                        .clickable { /* Update skin animation */ }
                                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                                ) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        Text(anim.uppercase(), style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black), color = Color.White.copy(alpha=0.4f))
                                                        Icon(if (anim == "running") Icons.Rounded.Bolt else if (anim == "walking") Icons.Rounded.DirectionsWalk else Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White.copy(alpha=0.4f), modifier = Modifier.size(10.dp))
                                                    }
                                                }
                                            }
                                        }
                                        
                                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha=0.05f)))
                                        
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("DIRECT UPLOAD", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black), color = Color.White.copy(alpha=0.3f))
                                            
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.White.copy(alpha=0.05f), RoundedCornerShape(12.dp))
                                                    .border(1.dp, Color.White.copy(alpha=0.05f), RoundedCornerShape(12.dp))
                                                    .clickable { skinPickerLauncher.launch("image/*") }
                                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                                            ) {
                                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Icon(Icons.Rounded.Upload, contentDescription = null, tint = themeAccentColor, modifier = Modifier.size(14.dp))
                                                    Text("SELECT FILE", style = ModulaTypography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black), color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                GlassButton(
                    text = "SIGN OUT SESSION",
                    variant = GlassVariant.DARK,
                    onClick = { showSignOutConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp).border(1.dp, Color(0xFFF44336).copy(alpha=0.3f), RoundedCornerShape(8.dp))
                )
            }
        }
        
        if (showSignOutConfirm) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ColorBg1.copy(alpha=0.95f))
                    .clickable { showSignOutConfirm = false },
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    variant = GlassVariant.DARK,
                    modifier = Modifier.fillMaxWidth(0.85f).clickable(enabled=false) {}
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Box(modifier = Modifier.size(64.dp).background(Color(0xFFF44336).copy(alpha=0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("DE-AUTHORIZE?", style = ModulaTypography.titleLarge.copy(fontSize = 20.sp), color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Your authentication token will be revoked from the Flux engine.", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp, fontWeight=FontWeight.Bold), color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            GlassButton(
                                text = "REVOKE SESSION",
                                variant = GlassVariant.DARK,
                                onClick = { 
                                    showSignOutConfirm = false
                                    if (currentAccount != null) {
                                        accountViewModel.onIntent(com.movtery.zalithlauncher.viewmodel.AccountManageIntent.DeleteAccount(currentAccount!!))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFF44336).copy(alpha=0.2f), RoundedCornerShape(8.dp)),
                            )
                            GlassButton(
                                text = "KEEP SECURE",
                                variant = GlassVariant.GOLD,
                                onClick = { showSignOutConfirm = false },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary
    GlassCard(
        variant = GlassVariant.DARK,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = label, tint = themeAccentColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black), color = TextMuted)
            Text(value, style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = Color.White)
        }
    }
}
