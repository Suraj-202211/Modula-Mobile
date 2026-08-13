package com.modulamobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import coil3.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

import com.modulamobile.ui.theme.*
import com.modulamobile.ui.theme.OrbitronFamily
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.game.account.SkinResolver
import com.movtery.zalithlauncher.game.version.installed.Version
import androidx.hilt.navigation.compose.hiltViewModel
import com.modulamobile.ui.settings.SettingsViewModel
import com.modulamobile.ui.glass.*

data class NewsItem(
    val title: String,
    val category: String,
    val date: String,
    val text: String,
    val imageUrl: String,
    val link: String
)

@Composable
fun HomeScreen(
    onNavigateToVersions: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToMods: () -> Unit = {},
    onLaunchGame: (Version?) -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val currentAccount by AccountsManager.currentAccountFlow.collectAsState(initial = null)
    val ramAllocationMb = AllSettings.ramAllocation.state ?: 4096
    val advancedDebug by settingsViewModel.advancedDebug.collectAsState()
    val unlockFps by settingsViewModel.unlockFps.collectAsState()
    val fluxVoiceEnabled = true // Placeholder for now
    
    val currentVersion = VersionsManager.currentVersion.collectAsState()
    
    val username = currentAccount?.username ?: "Player"
    var avatarUrl by remember(currentAccount) { mutableStateOf<String?>(null) }
    
    var newsItems by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var isLoadingNews by remember { mutableStateOf(true) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(currentAccount) {
        avatarUrl = SkinResolver.getAvatarUrl(currentAccount)
    }
    
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = URL("https://raw.githubusercontent.com/NOVE300IQ/modula-news/main/news.json").readText()
                val json = JSONObject(jsonString)
                val entries = json.getJSONArray("entries")
                val parsedItems = mutableListOf<NewsItem>()
                for (i in 0 until entries.length()) {
                    val entry = entries.getJSONObject(i)
                    val imgObj = entry.optJSONObject("playPageImage")
                    parsedItems.add(
                        NewsItem(
                            title = entry.optString("title"),
                            category = entry.optString("category"),
                            date = entry.optString("date"),
                            text = entry.optString("text"),
                            imageUrl = imgObj?.optString("url") ?: "",
                            link = entry.optString("readMoreLink")
                        )
                    )
                }
                newsItems = parsedItems
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingNews = false
            }
        }
    }

    val listState = rememberLazyListState()
    val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary

    @Composable
    fun SectionHeader(title: String) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(title.uppercase(), style = ModulaTypography.headlineMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Black), color = themeAccentColor)
            Text("EXPLORE ALL >", style = ModulaTypography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = themeAccentColor)
        }
    }


    Box(modifier = Modifier.fillMaxSize().background(ColorBg0)) {
        com.modulamobile.ui.components.FluxParticles()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 40.dp, bottom = 100.dp)
        ) {
            
            // Hero Card
            item {
                GlassHeroCard(
                    modifier = Modifier.fillMaxWidth().height(256.dp)
                ) {
                    // Background Image
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1627398242454-45a1465c2479?q=80&w=1000&auto=format&fit=crop",
                        contentDescription = "Background",
                        modifier = Modifier.matchParentSize().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient overlay
                    Box(modifier = Modifier.matchParentSize().background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, ColorBg0),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    ))
                    
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            GlassChip(
                                text = "v${currentVersion.value?.getVersionName() ?: "1.0.0"}",
                                selected = true
                            )
                            
                            Box(modifier = Modifier.size(48.dp).background(Color(0xFF1F1F1F), CircleShape).border(2.dp, themeAccentColor, CircleShape).clip(CircleShape).padding(4.dp), contentAlignment = Alignment.Center) {
                                if (avatarUrl != null) {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.Person,
                                        contentDescription = "Avatar",
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(username.uppercase(), style = ModulaTypography.titleLarge.copy(fontSize = 24.sp, fontFamily = OrbitronFamily, fontWeight = FontWeight.Black), color = Color.White)
                            Text("READY TO LAUNCH", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp), color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            GlassButton(
                                text = "Launch Game",
                                onClick = { onLaunchGame(currentVersion.value) },
                                variant = GlassVariant.GOLD,
                                modifier = Modifier.fillMaxWidth(0.8f),
                                icon = Icons.Filled.PlayArrow
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("${if (currentAccount?.accountType == "MICROSOFT") "MICROSOFT" else "OFFLINE"} • ${ramAllocationMb / 1024}GB RAM", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp), color = TextMuted)
                        }
                    }
                }
            }

            // Stats
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassCard(variant = GlassVariant.DARK, modifier = Modifier.weight(1f).height(80.dp)) {
                            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Rounded.Memory, contentDescription = null, tint = themeAccentColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${ramAllocationMb / 1024}GB", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.White)
                                Text("ALLOCATED", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                        }
                        GlassCard(variant = GlassVariant.DARK, modifier = Modifier.weight(1f).height(80.dp)) {
                            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = if(unlockFps) themeAccentColor else TextMuted, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(if(unlockFps) "UNLOCKED" else "CAPPED", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.White)
                                Text("ENGINE", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                        }
                        GlassCard(variant = GlassVariant.DARK, modifier = Modifier.weight(1f).height(80.dp)) {
                            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Rounded.Security, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("ACTIVE", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color(0xFF4CAF50))
                                Text("SECURITY", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassCard(variant = GlassVariant.DARK, modifier = Modifier.weight(1f).height(80.dp)) {
                            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = themeAccentColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("ON", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.White)
                                Text("WIFI", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Golden Flux Engine
            item {
                GlassCard(variant = GlassVariant.GOLD, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GOLDEN FLUX ENGINE", style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Black, fontFamily = OrbitronFamily, fontSize = 12.sp), color = Color.White)
                            Text("144Hz UI • Zero Stutter • Hardware Security", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp), color = Color.White.copy(alpha = 0.7f))
                        }
                        
                        val isEngineActive = unlockFps && ramAllocationMb >= 4096
                        Box(modifier = Modifier.background(if(isEngineActive) Color(0xFF2ECA71) else Color(0xFF4CAF50).copy(alpha=0.5f), RoundedCornerShape(2.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(if(isEngineActive) "ACTIVE" else "PARTIAL", style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize=9.sp), color = ColorBg0)
                        }
                    }
                }
            }
            


            // Recent Activity Section
            item {
                SectionHeader("MODERN MODPACKS")
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        GlassCard(variant = GlassVariant.DARK, modifier = Modifier.width(240.dp).height(120.dp)) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1607513746994-51f730a43854?q=80&w=600&auto=format&fit=crop",
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, ColorBg0))))
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Bottom) {
                                Text("BETTER MINECRAFT", style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Black, fontFamily = OrbitronFamily, fontSize=12.sp), color = Color.White)
                                Text("FABRIC 1.20.1", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                        }
                    }
                    item {
                        GlassCard(variant = GlassVariant.DARK, modifier = Modifier.width(240.dp).height(120.dp)) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1542314831-c6a4d14eff85?q=80&w=600&auto=format&fit=crop",
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, ColorBg0))))
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Bottom) {
                                Text("RLCRAFT OPTIMIZED", style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Black, fontFamily = OrbitronFamily, fontSize=12.sp), color = Color.White)
                                Text("FORGE 1.12.2", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                        }
                    }
                }
                
                SectionHeader("RECENT ACTIVITY")
                
                GlassCard(variant = GlassVariant.DARK, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NO RECENT ACTIVITY. DOWNLOAD A VERSION TO GET STARTED.", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp), color = TextMuted)
                    }
                }
            }
            
            // Community News Section
            item {
                SectionHeader("COMMUNITY NEWS")
            }
            
            if (isLoadingNews) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = themeAccentColor)
                    }
                }
            } else if (newsItems.isEmpty()) {
                item {
                    Text("No news available at the moment.", color = TextMuted, style = ModulaTypography.labelSmall)
                }
            } else {
                items(newsItems.size) { index ->
                    val item = newsItems[index]
                    GlassCard(
                        variant = GlassVariant.DARK,
                        modifier = Modifier.fillMaxWidth().clickable { if (item.link.isNotEmpty()) uriHandler.openUri(item.link) }
                    ) {
                        Column {
                            if (item.imageUrl.isNotEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().height(128.dp)) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = item.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xFF18181D)))))
                                }
                            }
                            Column(modifier = Modifier.padding(16.dp)) {
                                GlassBadge(text = item.category.uppercase())
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(item.title, style = ModulaTypography.titleLarge.copy(fontSize = 16.sp), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.text, style = ModulaTypography.labelSmall, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }

            item {
                GlassCard(variant = GlassVariant.DARK, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color(0xFF0D1829))) {
                            Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("KERALA'S OWN", style = ModulaTypography.headlineMedium.copy(fontSize = 12.sp, fontFamily=OrbitronFamily), color = Color(0xFF4CA0E0))
                                    Text("MODULA IS MADE FROM", style = ModulaTypography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text("GOD'S OWN COUNTRY", style = ModulaTypography.headlineMedium.copy(fontSize = 16.sp, fontFamily=OrbitronFamily), color = Color(0xFF2ECA71))
                                }
                                Box(modifier = Modifier.size(64.dp).background(Color(0xFF1E3A8A), RoundedCornerShape(8.dp)))
                            }
                        }
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(modifier = Modifier.background(themeAccentColor, RoundedCornerShape(2.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("UPDATE", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = ColorBg0)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Coming Soon: Official Textures!", style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize=14.sp), color = Color.White)
                            Text("We are hard at work on the first official Modula high-performance texture pack. Stay tuned to our Discord for the early access beta!", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp), color = TextSecondary)
                        }
                    }
                }
            }

        }
    }
}
