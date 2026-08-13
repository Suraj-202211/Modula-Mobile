package com.movtery.zalithlauncher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.ui.components.*
import com.movtery.zalithlauncher.ui.theme.*

data class MockMod(val id: String, val name: String, val author: String, val downloads: String, val isInstalled: Boolean, val iconUrl: String)

@Composable
fun ModsScreen(navController: NavHostController) {
    val mods = listOf(
        MockMod("1", "Sodium", "jellysquid3", "45M", true, "https://mc-heads.net/avatar/MHF_Chest"),
        MockMod("2", "Iris Shaders", "coderbot", "20M", false, "https://mc-heads.net/avatar/MHF_Chest"),
        MockMod("3", "ModMenu", "Prospector", "30M", true, "https://mc-heads.net/avatar/MHF_Chest")
    )

    Column(Modifier.fillMaxSize()) {
        FluxTopBar(title = "MODS")
        
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FluxChip("Modrinth", selected = true)
            FluxChip("CurseForge")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("TRENDING NOW", style = LabelLG, color = TextSecondary, fontFamily = OrbitronFamily)
                    Text("O", color = FluxGold)
                }
            }
            items(mods.size) { index ->
                val mod = mods[index]
                FluxCard(variant = GlassVariant.DARK, padding = 16.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = mod.iconUrl,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(mod.name, style = TitleLG, color = TextPrimary)
                            Text("By ${mod.author}", style = BodySM, color = TextMuted)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Text("O", color = FluxGold, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(mod.downloads, style = LabelSM, color = TextSecondary)
                            }
                        }
                        if (mod.isInstalled) {
                            Text("O", color = StateSuccess)
                        } else {
                            Text("X", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
