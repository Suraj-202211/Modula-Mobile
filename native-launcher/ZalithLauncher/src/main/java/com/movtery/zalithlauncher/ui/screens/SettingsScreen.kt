package com.movtery.zalithlauncher.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import com.movtery.zalithlauncher.ui.components.*
import com.movtery.zalithlauncher.ui.theme.*
import com.movtery.zalithlauncher.setting.AllSettings
import com.modulamobile.ui.update.UpdateViewModel

@Composable
fun SettingsScreen(navController: NavHostController, updateViewModel: UpdateViewModel = hiltViewModel()) {
    var ram by remember { mutableStateOf((AllSettings.ramAllocation.state ?: 4096).toFloat() / 1024f) }
    var useZink by remember { mutableStateOf(AllSettings.renderer.state == "VulkanZinkRenderer") }
    var renderScale by remember { mutableStateOf(AllSettings.resolutionRatio.state.toFloat()) }

    Column(Modifier.fillMaxSize()) {
        FluxTopBar(title = "SETTINGS")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Column {
                FluxSectionHeader(title = "PERFORMANCE")
                Spacer(Modifier.height(12.dp))
                
                FluxCard(variant = GlassVariant.DARK) {
                    Column {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Allocated RAM", style = BodyLG, color = TextHero)
                            Text("${ram.toInt()} GB", style = HeadingSM, color = FluxGold, fontFamily = OrbitronFamily)
                        }
                        Spacer(Modifier.height(16.dp))
                        FluxSlider(
                            value = ram,
                            onValueChange = { 
                                ram = it 
                                AllSettings.ramAllocation.save((it * 1024).toInt())
                            },
                            valueRange = 1f..12f,
                            label = "Allocated RAM",
                            valueLabel = "${ram.toInt()} GB"
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                FluxCard(variant = GlassVariant.DARK) {
                    Column {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Resolution Scale", style = BodyLG, color = TextHero)
                            Text("${renderScale.toInt()}%", style = HeadingSM, color = FluxGold, fontFamily = OrbitronFamily)
                        }
                        Spacer(Modifier.height(16.dp))
                        FluxSlider(
                            value = renderScale,
                            onValueChange = { 
                                renderScale = it 
                                AllSettings.resolutionRatio.save(it.toInt())
                            },
                            valueRange = 50f..100f,
                            label = "Resolution Scale",
                            valueLabel = "${renderScale.toInt()}%"
                        )
                    }
                }
            }

            Column {
                FluxSectionHeader(title = "ENGINE")
                Spacer(Modifier.height(12.dp))
                
                FluxCard(variant = GlassVariant.DARK) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .border(1.dp, FluxGold, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("O", color = FluxGold, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Zink Renderer", style = TitleLG, color = TextHero)
                            Text("Vulkan bridge for high FPS", style = BodySM, color = TextSecondary)
                        }
                        FluxToggle(
                            checked = useZink, 
                            onCheckedChange = { 
                                useZink = it 
                                AllSettings.renderer.save(if (it) "VulkanZinkRenderer" else "")
                            },
                            label = "Zink Renderer",
                            subtitle = "Vulkan bridge for high FPS"
                        )
                    }
                }
            }

            Column {
                FluxSectionHeader(title = "SYSTEM")
                Spacer(Modifier.height(12.dp))
                
                FluxButton(
                    onClick = { updateViewModel.checkManually() },
                    variant = FluxButtonVariant.PRIMARY,
                    label = "CHECK FOR UPDATES",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
