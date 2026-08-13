package com.modulamobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

import com.modulamobile.ui.theme.*
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.modulamobile.viewmodel.VersionsViewModel
import com.movtery.zalithlauncher.game.download.game.GameInstaller
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.io.File
import com.modulamobile.ui.glass.*

data class DisplayVersion(
    val id: String,
    val type: String,
    val isInstalled: Boolean,
    val originalVersion: Version? = null,
    val remoteUrl: String? = null
)

private fun getReleaseType(version: Version): String {
    return try {
        val jsonFile = File(version.getVersionPath(), "${version.getVersionName()}.json")
        if (jsonFile.exists()) {
            val jsonObject = com.google.gson.JsonParser.parseString(jsonFile.readText()).asJsonObject
            jsonObject.get("type")?.asString?.lowercase() ?: "release"
        } else {
            "release"
        }
    } catch (e: Exception) {
        "release"
    }
}

@Composable
fun VersionsScreen(
    onBack: () -> Unit,
    versionsViewModel: VersionsViewModel = hiltViewModel()
) {
    var installedVersions by remember { mutableStateOf(VersionsManager.versions) }
    val remoteVersions by versionsViewModel.versions.collectAsState()
    val errorMsg by versionsViewModel.error.collectAsState()
    
    DisposableEffect(Unit) {
        val listener: suspend (List<Version>) -> Unit = { versions ->
            installedVersions = versions
        }
        VersionsManager.registerListener(listener)
        onDispose {
            VersionsManager.unregisterListener(listener)
        }
    }
    
    val currentVersion by VersionsManager.currentVersion.collectAsState()
    var selectedType by remember { mutableStateOf("All") }
    val types = listOf("All", "Release", "Snapshot", "Beta", "Alpha")
    
    var selectedLoader by remember { mutableStateOf("Vanilla") }
    val loaders = listOf("Vanilla", "Fabric", "Forge", "Quilt", "NeoForge")

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var installer by remember { mutableStateOf<GameInstaller?>(null) }

    val mergedVersions = remember(installedVersions, remoteVersions, selectedType, selectedLoader) {
        val installedMap = installedVersions.associateBy { it.getVersionName() }
        
        val allDisplay = mutableListOf<DisplayVersion>()
        installedVersions.forEach { v ->
            allDisplay.add(DisplayVersion(v.getVersionName(), getReleaseType(v), true, originalVersion = v))
        }
        
        remoteVersions.forEach { rv ->
            if (!installedMap.containsKey(rv.id)) {
                allDisplay.add(DisplayVersion(rv.id, rv.type, false, remoteUrl = rv.url))
            }
        }
        
        allDisplay.filter { v ->
            val matchesType = when (selectedType) {
                "All" -> true
                "Release" -> v.type == "release"
                "Snapshot" -> v.type == "snapshot"
                "Beta" -> v.type == "old_beta" || v.type == "beta"
                "Alpha" -> v.type == "old_alpha" || v.type == "alpha"
                else -> true
            }

            val versionNameLower = v.id.lowercase()
            val matchesLoader = when (selectedLoader) {
                "Vanilla" -> {
                    if (v.isInstalled) {
                        val hasLoader = v.originalVersion?.getVersionInfo()?.loaderInfo != null
                        !hasLoader && !versionNameLower.contains("fabric") && !versionNameLower.contains("forge") && !versionNameLower.contains("quilt") && !versionNameLower.contains("neoforge")
                    } else true
                }
                "Fabric" -> versionNameLower.contains("fabric") || v.originalVersion?.getVersionInfo()?.loaderInfo?.loader?.name?.contains("FABRIC", ignoreCase = true) == true
                "Forge" -> (versionNameLower.contains("forge") && !versionNameLower.contains("neoforge")) || v.originalVersion?.getVersionInfo()?.loaderInfo?.loader?.name?.equals("FORGE", ignoreCase = true) == true
                "Quilt" -> versionNameLower.contains("quilt") || v.originalVersion?.getVersionInfo()?.loaderInfo?.loader?.name?.equals("QUILT", ignoreCase = true) == true
                "NeoForge" -> versionNameLower.contains("neoforge") || v.originalVersion?.getVersionInfo()?.loaderInfo?.loader?.name?.equals("NEOFORGE", ignoreCase = true) == true
                else -> true
            }

            matchesType && matchesLoader
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(ColorBg0)) {

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Sticky Header for filters
            Column(modifier = Modifier.fillMaxWidth().background(ColorBg0.copy(alpha=0.8f)).padding(vertical = 8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(types) { type ->
                        GlassChip(
                            text = type,
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(loaders) { loader ->
                        GlassChip(
                            text = loader,
                            selected = selectedLoader == loader,
                            onClick = { selectedLoader = loader }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mergedVersions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.SearchOff, contentDescription = "Empty", tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("NO VERSIONS FOUND", style = ModulaTypography.titleLarge, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (errorMsg != null) {
                            Text(errorMsg!!, style = ModulaTypography.labelSmall, color = Color.Red, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        } else {
                            Text("Change your filters or check your connection.", style = ModulaTypography.labelSmall, color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(mergedVersions) { version ->
                        VersionListCard(
                            version = version,
                            isSelected = currentVersion?.getVersionName() == version.id,
                            onSelect = { 
                                if (version.isInstalled) {
                                    VersionsManager.saveCurrentVersion(version.id, refresh = true) 
                                }
                            },
                            onDownload = {
                                if (installer != null) return@VersionListCard // Prevent double click
                                val info = GameDownloadInfo(
                                    gameVersion = version.id,
                                    customVersionName = version.id,
                                    overwrite = true
                                )
                                installer = GameInstaller(context, info, coroutineScope).also {
                                    it.installGame(
                                        onInstalled = { installedVersion ->
                                            installer = null
                                            VersionsManager.refresh("VersionsScreen install", installedVersion)
                                            VersionsManager.saveCurrentVersion(installedVersion, refresh = true)
                                        },
                                        onError = { _ ->
                                            installer = null
                                        },
                                        onGameAlreadyInstalled = {
                                            installer = null
                                        }
                                    )
                                }
                            }
                        )
                    }
                    
                    item {
                        GlassGhostButton(
                            text = "REFRESH VERSIONS",
                            onClick = { versionsViewModel.fetchVersions() }, 
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha=0.2f), RoundedCornerShape(8.dp)).padding(vertical=16.dp)
                        )
                    }
                }
            }
        }
    }

    if (installer != null) {
        val installTasks by installer!!.tasksFlow.collectAsStateWithLifecycle()
        if (installTasks.isNotEmpty()) {
            TitleTaskFlowDialog(
                title = "Downloading Game...",
                tasks = installTasks,
                onCancel = {
                    installer?.cancelInstall()
                    installer = null
                }
            )
        }
    }
}

@Composable
fun VersionListCard(version: DisplayVersion, isSelected: Boolean, onSelect: () -> Unit, onDownload: () -> Unit) {
    val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary
    
    GlassCard(
        variant = if (isSelected) GlassVariant.GOLD else GlassVariant.DARK,
        modifier = Modifier.fillMaxWidth().clickable { if (version.isInstalled) onSelect() else onDownload() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isSelected) Color.White.copy(alpha=0.2f) else Color(0xFF1F1F24), RoundedCornerShape(8.dp))
                    .border(1.dp, if (isSelected) Color.White.copy(alpha=0.2f) else Color.White.copy(alpha=0.05f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (version.isInstalled) {
                    Icon(Icons.Rounded.Inventory, contentDescription = "Package", tint = if (isSelected) Color.White else themeAccentColor, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Rounded.CloudDownload, contentDescription = "Download", tint = TextMuted, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(version.id, style = ModulaTypography.titleLarge.copy(fontSize = 18.sp), color = Color.White)
                    if (version.type.equals("release", ignoreCase = true)) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.background(Color(0xFF1B5E20).copy(alpha=0.2f), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFF4CAF50).copy(alpha=0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("STABLE", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = Color(0xFF4CAF50))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(version.type.uppercase(), style = ModulaTypography.labelSmall.copy(fontSize = 9.sp), color = if (isSelected) Color.White.copy(alpha=0.7f) else TextSecondary)
            }
            
            if (version.isInstalled) {
                if (isSelected) {
                    Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha=0.2f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                    }
                } else {
                    GlassGhostButton(text = "SELECT", onClick = { onSelect() }, modifier = Modifier.height(36.dp))
                }
            } else {
                GlassGhostButton(text = "GET", onClick = { onDownload() }, modifier = Modifier.height(36.dp))
            }
        }
    }
}
