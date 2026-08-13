package com.modulamobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.modulamobile.network.ModrinthMod

import com.modulamobile.ui.theme.*
import com.modulamobile.viewmodel.ModsViewModel
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.modulamobile.ui.glass.*

@Composable
fun ModsScreen(
    onBack: () -> Unit,
    modsViewModel: ModsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Browse", "Installed", "Modpacks")

    val mods by modsViewModel.mods.collectAsState()
    val isLoading by modsViewModel.isLoadingMods.collectAsState()
    val errorMsg by modsViewModel.errorMods.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val currentVersion by VersionsManager.currentVersion.collectAsState()
    var downloadTasks by remember { mutableStateOf<List<TitledTask>>(emptyList()) }
    val context = LocalContext.current
    
    var showVersionDialogForMod by remember { mutableStateOf<ModrinthMod?>(null) }
    val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary

    Box(modifier = Modifier.fillMaxSize().background(ColorBg0)) {

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Top Navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glass(GlassVariant.DARK, 50.dp) // pill shape
                    .padding(4.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (selectedTab == index) themeAccentColor else Color.Transparent, RoundedCornerShape(50.dp))
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            title.uppercase(),
                            style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                            color = if (selectedTab == index) ColorBg0 else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it 
                    modsViewModel.search(it)
                },
                placeholder = { 
                    Text("Search Modrinth...", color = TextMuted, style = ModulaTypography.labelSmall) 
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).glass(GlassVariant.DARK, 12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { 
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = themeAccentColor, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(20.dp)) 
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTab == 0) {
                if (isLoading && mods.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = themeAccentColor)
                    }
                } else if (errorMsg != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMsg!!, color = Color.Red, style = ModulaTypography.labelSmall)
                    }
                } else if (mods.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No mods found.", color = TextMuted, style = ModulaTypography.labelSmall)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(mods) { mod ->
                            RealModCard(
                                mod = mod,
                                isInstalled = false, // TODO: Implement isInstalled check based on installed mods list
                                onInstall = {
                                    showVersionDialogForMod = mod
                                }
                            )
                        }
                    }
                }
            } else if (selectedTab == 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                        .glass(GlassVariant.DARK, 12.dp)
                        .border(1.dp, Color.White.copy(alpha=0.1f), RoundedCornerShape(12.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("NO MODS INSTALLED YET.", style = ModulaTypography.labelSmall.copy(letterSpacing=2.sp), color = TextMuted)
                }
            }
        }
    }
    
    if (downloadTasks.isNotEmpty()) {
        TitleTaskFlowDialog(
            title = "Downloading Mod...",
            tasks = downloadTasks,
            onCancel = {
                downloadTasks.forEach { com.movtery.zalithlauncher.coroutine.TaskSystem.cancelTask(it.task.id) }
                downloadTasks = emptyList()
            }
        )
    }

    if (showVersionDialogForMod != null) {
        VersionSelectionDialog(
            mod = showVersionDialogForMod!!,
            isModpack = false,
            modsViewModel = modsViewModel,
            onDismiss = { showVersionDialogForMod = null },
            onVersionSelected = { selectedVersion ->
                showVersionDialogForMod = null
                val targetGameDir = currentVersion?.getGameDir()
                if (targetGameDir == null) {
                    Toast.makeText(context, "Please select a game version in the Versions screen first.", Toast.LENGTH_SHORT).show()
                    return@VersionSelectionDialog
                }
                try {
                    val task = modsViewModel.downloadMod(showVersionDialogForMod!!, selectedVersion, targetGameDir)
                    val titledTask = com.movtery.zalithlauncher.coroutine.TitledTask(title = showVersionDialogForMod!!.title, task = task)
                    downloadTasks = downloadTasks + titledTask
                    com.movtery.zalithlauncher.coroutine.TaskSystem.submitTask(task)
                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

@Composable
fun VersionSelectionDialog(
    mod: ModrinthMod,
    isModpack: Boolean,
    modsViewModel: ModsViewModel,
    onDismiss: () -> Unit,
    onVersionSelected: (String) -> Unit
) {
    var availableVersions by remember { mutableStateOf<List<String>?>(null) }
    
    LaunchedEffect(mod) {
        availableVersions = if (isModpack) {
            modsViewModel.getAvailableGameVersionsForModpack(mod.projectId)
        } else {
            modsViewModel.getAvailableGameVersionsForMod(mod.projectId)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF15151A),
        title = {
            Text("Select Target Version", color = Color.White, style = ModulaTypography.titleLarge)
        },
        text = {
            Column {
                Text("Which Minecraft version do you want to download ${mod.title} for?", color = TextSecondary, style = ModulaTypography.labelSmall)
                Spacer(modifier = Modifier.height(16.dp))
                
                if (availableVersions == null) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = com.modulamobile.ui.theme.LocalModulaColors.current.primary)
                    }
                } else if (availableVersions!!.isEmpty()) {
                    Text("No versions found.", color = Color.Red, style = ModulaTypography.labelSmall)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableVersions!!) { ver ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .glass(GlassVariant.DARK, 8.dp)
                                    .border(1.dp, Color(0xFF2A2A35), RoundedCornerShape(8.dp))
                                    .clickable { onVersionSelected(ver) }
                                    .padding(12.dp)
                            ) {
                                Text(ver, color = Color.White, style = ModulaTypography.labelLarge)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun RealModCard(mod: ModrinthMod, isInstalled: Boolean, onInstall: () -> Unit) {
    val downloadsFormatted = if (mod.downloads > 1000000) {
        String.format("%.1fM", mod.downloads / 1000000.0)
    } else if (mod.downloads > 1000) {
        String.format("%.1fK", mod.downloads / 1000.0)
    } else {
        mod.downloads.toString()
    }
    
    val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary

    GlassCard(
        variant = GlassVariant.DARK,
        modifier = Modifier.fillMaxWidth().height(180.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            if (mod.iconUrl != null) {
                AsyncImage(
                    model = mod.iconUrl,
                    contentDescription = mod.title,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, Color.White.copy(alpha=0.05f), RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(48.dp).background(ColorBg4, RoundedCornerShape(12.dp)).border(1.dp, Color.White.copy(alpha=0.05f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.GridView, contentDescription = null, tint = themeAccentColor, modifier = Modifier.size(24.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(mod.title, style = ModulaTypography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(2.dp))
            Text("BY ${mod.author.uppercase()}", style = ModulaTypography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha=0.05f)))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Download, contentDescription = "Downloads", tint = TextSecondary, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(downloadsFormatted, style = ModulaTypography.labelSmall.copy(fontSize = 10.sp), color = TextSecondary)
                }
                
                if (isInstalled) {
                    Box(modifier = Modifier.size(32.dp).background(Color(0xFF1B5E20).copy(alpha=0.2f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Check, contentDescription = "Installed", tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .glass(GlassVariant.DARK, 4.dp)
                            .clickable { onInstall() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
