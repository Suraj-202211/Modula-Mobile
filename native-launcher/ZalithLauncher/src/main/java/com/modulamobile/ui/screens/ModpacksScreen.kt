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
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.modulamobile.ui.glass.*
import com.movtery.zalithlauncher.game.version.installed.VersionsManager

@Composable
fun ModpacksScreen(
    onBack: () -> Unit,
    modsViewModel: ModsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Browse", "Installed", "Mods")

    val modpacks by modsViewModel.modpacks.collectAsState()
    val isLoading by modsViewModel.isLoadingModpacks.collectAsState()
    val errorMsg by modsViewModel.errorModpacks.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    var downloadTasks by remember { mutableStateOf<List<TitledTask>>(emptyList()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var showVersionDialogForModpack by remember { mutableStateOf<ModrinthMod?>(null) }
    val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary

    Box(modifier = Modifier.fillMaxSize().background(ColorBg0)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Top Navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f) // match max-w-sm roughly
                    .glass(GlassVariant.DARK, 50.dp)
                    .padding(4.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (selectedTab == index) themeAccentColor else Color.Transparent, RoundedCornerShape(50.dp))
                            .clickable { 
                                // In a real app we might navigate, here we just change tab
                                selectedTab = index 
                            }
                            .padding(vertical = 12.dp),
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
            Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it 
                        modsViewModel.search(it)
                    },
                    placeholder = { 
                        Text("DISCOVER CURATED MODPACKS...", color = TextMuted, style = ModulaTypography.labelSmall.copy(fontWeight=FontWeight.Bold, letterSpacing=2.sp)) 
                    },
                    modifier = Modifier.fillMaxSize().glass(GlassVariant.DARK, 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { 
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = themeAccentColor, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Rounded.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(20.dp)) 
                        }
                    }
                )
                
                // Chips inside search bar (right side)
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassChip(text = "FABRIC", selected = false, onClick = {})
                    GlassChip(text = "1.20.1", selected = false, onClick = {})
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (selectedTab == 0) {
                if (isLoading && modpacks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = themeAccentColor)
                    }
                } else if (errorMsg != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMsg!!, color = Color.Red, style = ModulaTypography.labelSmall)
                    }
                } else if (modpacks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No modpacks found.", color = TextMuted, style = ModulaTypography.labelSmall)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2), // Match md:grid-cols-2
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(modpacks) { pack ->
                            RealModpackListCard(
                                pack = pack,
                                isInstalled = false,
                                onInstall = {
                                    showVersionDialogForModpack = pack
                                }
                            )
                        }
                        
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            // Optimization Engine Banner
                            GlassCard(
                                variant = GlassVariant.GOLD,
                                modifier = Modifier.fillMaxWidth().height(160.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                                    Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color.White.copy(alpha=0.1f), modifier = Modifier.size(100.dp).align(Alignment.CenterEnd).offset(x = 20.dp, y = (-20).dp))
                                    Column {
                                        Text("OPTIMIZATION ENGINE", style = ModulaTypography.titleLarge, color = ColorBg0)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Our launcher automatically injects ASM transformations to ensure stable 60FPS even on medium-tier devices.",
                                            style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                                            color = ColorBg0.copy(alpha = 0.7f),
                                            maxLines = 3
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        GlassButton(
                                            text = "TWEAK ENGINE",
                                            variant = GlassVariant.DARK,
                                            onClick = {},
                                            modifier = Modifier.height(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                        .glass(GlassVariant.DARK, 16.dp)
                        .border(1.dp, Color.White.copy(alpha=0.1f), RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Inventory, contentDescription = null, tint = Color.White.copy(alpha=0.05f), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("NO MODPACKS INSTALLED IN YOUR LIBRARY", style = ModulaTypography.labelSmall.copy(letterSpacing=2.sp, fontWeight=FontWeight.Bold), color = TextMuted)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("BROWSE MODPACKS >", style = ModulaTypography.labelSmall.copy(letterSpacing=3.sp, fontWeight=FontWeight.Bold), color = themeAccentColor, modifier = Modifier.clickable { selectedTab = 0 })
                    }
                }
            }
        }
    }
    
    if (downloadTasks.isNotEmpty()) {
        TitleTaskFlowDialog(
            title = "Downloading Modpack...",
            tasks = downloadTasks,
            onCancel = {
                downloadTasks.forEach { com.movtery.zalithlauncher.coroutine.TaskSystem.cancelTask(it.task.id) }
                downloadTasks = emptyList()
            }
        )
    }
    
    if (showVersionDialogForModpack != null) {
        VersionSelectionDialog(
            mod = showVersionDialogForModpack!!,
            isModpack = true,
            modsViewModel = modsViewModel,
            onDismiss = { showVersionDialogForModpack = null },
            onVersionSelected = { selectedVersion ->
                val pack = showVersionDialogForModpack!!
                showVersionDialogForModpack = null
                
                modsViewModel.downloadModpack(
                    pack = pack,
                    targetGameVersion = selectedVersion,
                    context = context,
                    scope = coroutineScope,
                    onTasksUpdate = { tasks ->
                        downloadTasks = tasks
                    }
                )
            }
        )
    }
}

@Composable
fun RealModpackListCard(pack: ModrinthMod, isInstalled: Boolean, onInstall: () -> Unit) {
    val downloadsFormatted = if (pack.downloads > 1000) {
        String.format("%.0fK", pack.downloads / 1000.0)
    } else {
        pack.downloads.toString()
    }
    
    val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary

    GlassCard(
        variant = GlassVariant.DARK,
        modifier = Modifier.fillMaxWidth().height(260.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(Color(0xFF202028))) {
                if (pack.iconUrl != null) {
                    AsyncImage(
                        model = pack.iconUrl,
                        contentDescription = pack.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Rounded.Inventory, contentDescription = null, tint = Color.White.copy(alpha=0.1f), modifier = Modifier.size(40.dp).align(Alignment.Center))
                }
                
                Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(ColorBg1.copy(alpha=0.9f), ColorBg1.copy(alpha=0.2f), Color.Transparent))))
                
                Box(modifier = Modifier.padding(12.dp).align(Alignment.TopEnd).glass(GlassVariant.DARK, 4.dp).padding(horizontal = 6.dp, vertical = 3.dp)) {
                    Text(if (pack.categories.isNotEmpty()) pack.categories.first().uppercase() else "GENERAL", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = Color.White)
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(pack.title, style = ModulaTypography.titleLarge.copy(fontSize = 16.sp), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(2.dp))
                Text("BY ${pack.author.uppercase()}", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp), color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(pack.description, style = ModulaTypography.labelSmall.copy(fontSize = 11.sp), color = Color.White.copy(alpha=0.5f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha=0.05f)))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("DOWNLOADS", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp, fontWeight=FontWeight.Black, letterSpacing = 2.sp), color = TextMuted)
                            Text(downloadsFormatted, style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                        Column {
                            Text("FOLLOWERS", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp, fontWeight=FontWeight.Black, letterSpacing = 2.sp), color = TextMuted)
                            Text(pack.follows.toString(), style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }
                    
                    if (isInstalled) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.Check, contentDescription = "Installed", tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                            Text("INSTALLED", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp), color = Color(0xFF4CAF50))
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .glass(GlassVariant.GOLD, 8.dp)
                                .clickable { onInstall() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = "Install", tint = ColorBg0, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
