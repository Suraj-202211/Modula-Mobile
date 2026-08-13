package com.movtery.zalithlauncher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.movtery.zalithlauncher.ui.components.*
import com.movtery.zalithlauncher.ui.theme.*

data class MockVersion(val id: String, val type: String, val date: String, val isStable: Boolean, val downloaded: Boolean)

@Composable
fun VersionsScreen(navController: NavHostController) {
    val versions = listOf(
        MockVersion("1.20.1", "Release", "2023-06-12", true, true),
        MockVersion("1.19.4", "Release", "2023-03-14", true, false),
        MockVersion("23w43a", "Snapshot", "2023-10-25", false, false)
    )

    Column(Modifier.fillMaxSize()) {
        FluxTopBar(title = "VERSIONS")

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FluxChip("All", selected = true)
                    FluxChip("Release")
                    FluxChip("Snapshot")
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FluxChip("Vanilla")
                    FluxChip("Fabric", selected = true)
                    FluxChip("Forge")
                }
            }
            items(versions) { version ->
                FluxCard(variant = GlassVariant.DARK, padding = 12.dp) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("O", color = FluxGold)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = version.id, style = MonoMD, color = TextHero)
                                Spacer(Modifier.width(8.dp))
                                if (version.isStable) {
                                    FluxBadge("STABLE", color = StateSuccess)
                                }
                            }
                            Text(text = "${version.type} • ${version.date}", style = BodySM, color = TextMuted)
                        }
                        if (version.downloaded) {
                            FluxButton(label = "PLAY ▶", onClick = { }, variant = FluxButtonVariant.PRIMARY, modifier = Modifier.height(36.dp).width(100.dp))
                        } else {
                            FluxButton(label = "GET", onClick = { }, variant = FluxButtonVariant.GHOST, modifier = Modifier.height(36.dp).width(100.dp))
                        }
                    }
                }
            }
        }
    }
}
