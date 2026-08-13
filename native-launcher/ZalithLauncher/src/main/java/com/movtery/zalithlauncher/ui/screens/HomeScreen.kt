package com.movtery.zalithlauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.*
import com.movtery.zalithlauncher.ui.theme.*
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ModulaNewsResponse(val entries: List<ModulaNewsEntry> = emptyList())

@Serializable
data class ModulaNewsEntry(
    val title: String,
    val text: String,
    val tag: String,
    val playPageImage: NewsImage,
    val newsPageImage: NewsImage
)

@Serializable
data class NewsImage(val url: String)

@Composable
fun HomeScreen(navController: NavHostController) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val username = account?.username ?: "Player"
    val ramGb = (AllSettings.ramAllocation.state ?: 4096) / 1024
    
    var newsList by remember { mutableStateOf<List<ModulaNewsEntry>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val response = GLOBAL_CLIENT.get("https://raw.githubusercontent.com/NOVE300IQ/modula-news/main/news.json").bodyAsText()
                val parsed = Json { ignoreUnknownKeys = true }.decodeFromString<ModulaNewsResponse>(response)
                newsList = parsed.entries
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Top Profile / Launch Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FluxGold.copy(0.5f), RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0F14), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Box(Modifier.background(FluxGold, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("v1.20.1", style = LabelSM, color = Color.Black, fontFamily = OrbitronFamily)
                        }
                        
                        // Rainbow glowing avatar
                        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(
                                                Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                                                Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000)
                                            )
                                        )
                                    )
                                    .background(Color.Black.copy(alpha = 0.5f))
                            )
                            AsyncImage(
                                model = account?.getSkinFile()?.takeIf { it.exists() } ?: "https://mc-heads.net/avatar/$username/256",
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Bg4)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Text(username.uppercase(), style = DisplayLG.copy(fontSize = 24.sp), color = TextHero, fontFamily = OrbitronFamily)
                    Text("READY TO LAUNCH", style = LabelSM, color = TextSecondary, fontFamily = OrbitronFamily)
                    
                    Spacer(Modifier.height(16.dp))
                    FluxButton(
                        label = "▶ Launch Game",
                        onClick = { },
                        variant = FluxButtonVariant.PRIMARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    Text("OFFLINE • ${ramGb}GB RAM", style = LabelSM.copy(fontSize = 10.sp), color = TextSecondary, fontFamily = OrbitronFamily)
                }
            }
        }

        // Stats Grid
        item {
            @Composable
            fun StatCard(icon: String, iconColor: Color, title: String, subtitle: String, modifier: Modifier = Modifier) {
                Box(
                    modifier = modifier
                        .border(1.dp, TextSecondary.copy(0.1f), RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F0F14), RoundedCornerShape(8.dp))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(icon, color = iconColor, style = HeadingSM, fontFamily = OrbitronFamily)
                        Spacer(Modifier.height(8.dp))
                        Text(title.uppercase(), style = LabelSM.copy(fontSize = 11.sp), color = TextHero, fontFamily = OrbitronFamily)
                        Text(subtitle.uppercase(), style = LabelSM.copy(fontSize = 9.sp), color = TextSecondary)
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("⛃", FluxGold, "${ramGb}GB", "ALLOCATED", Modifier.weight(1f))
                StatCard("⚡", FluxGold, "UNLOCKED", "FRAMES", Modifier.weight(1f))
                StatCard("✓", StateSuccess, "ACTIVE", "RENDERER", Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("🔊", Color(0xFF3399FF), "ON", "AUDIO", Modifier.weight(1f))
                Spacer(Modifier.weight(2f)) // Filler for empty grid spaces
            }
        }

        // Flux Engine Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FluxGold.copy(0.3f), RoundedCornerShape(8.dp))
                    .background(Color(0xFF141400), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                        Text("⚡", color = FluxGold, style = BodyLG)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("GOLDEN FLUX ENGINE", style = TitleLG.copy(fontSize = 14.sp), color = TextHero, fontFamily = OrbitronFamily)
                        Text("144HZ UI • ZERO STUTTER • HARDWARE SECURITY", style = LabelSM.copy(fontSize = 9.sp), color = TextSecondary, fontFamily = OrbitronFamily)
                    }
                    Box(Modifier.background(StateSuccess, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("ACTIVE", style = LabelSM, color = Color.Black, fontFamily = OrbitronFamily)
                    }
                }
            }
        }

        // Community News
        item {
            Text("COMMUNITY NEWS", style = TitleLG.copy(fontSize = 16.sp), color = FluxGold, fontFamily = OrbitronFamily)
            Spacer(Modifier.height(8.dp))
        }
        
        items(newsList) { news ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TextSecondary.copy(0.2f), RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F0F14))
            ) {
                Column {
                    AsyncImage(
                        model = news.newsPageImage.url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                    )
                    Column(Modifier.padding(12.dp)) {
                        Box(Modifier.background(FluxGold, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(news.tag, style = LabelSM.copy(fontSize = 9.sp), color = Color.Black, fontFamily = OrbitronFamily)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(news.title, style = TitleLG.copy(fontSize = 14.sp), color = TextHero, fontFamily = OrbitronFamily)
                        Spacer(Modifier.height(4.dp))
                        Text(news.text, style = LabelSM.copy(fontSize = 10.sp), color = TextSecondary, maxLines = 2)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
