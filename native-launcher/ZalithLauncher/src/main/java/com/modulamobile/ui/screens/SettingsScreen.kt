package com.modulamobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.hilt.navigation.compose.hiltViewModel
import com.modulamobile.ui.settings.SettingsViewModel
import com.modulamobile.ui.theme.*
import com.movtery.zalithlauncher.setting.AllSettings
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import com.modulamobile.ui.glass.*
import kotlinx.coroutines.launch

@Composable
fun GlassSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = ModulaTypography.titleLarge,
        color = Color.White,
        modifier = modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val ramAllocationMb by viewModel.ramAllocation.collectAsState()
    val unlockFps by viewModel.unlockFps.collectAsState()
    val jvmArgs by viewModel.jvmArgs.collectAsState()
    
    var showCacheDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importValue by remember { mutableStateOf("") }
    
    var isEditingJvm by remember { mutableStateOf(false) }
    var tempJvmArgs by remember { mutableStateOf(jvmArgs) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val upgradeViewModel: com.movtery.zalithlauncher.viewmodel.LauncherUpgradeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(context as androidx.activity.ComponentActivity)
    val scope = rememberCoroutineScope()
    val migrationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            android.widget.Toast.makeText(context, "Migration started", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    // Sync JVM args locally when it changes globally
    LaunchedEffect(jvmArgs) { tempJvmArgs = jvmArgs }
    
    val themeSelection by viewModel.themeSelection.collectAsState()
    val particleDensity by viewModel.particleDensity.collectAsState()
    val motionInterpolation by viewModel.motionInterpolation.collectAsState()
    val uiTransparency by viewModel.uiTransparency.collectAsState()
    val uiScaling by viewModel.uiScaling.collectAsState()
    val bloomEffects by viewModel.bloomEffects.collectAsState()
    val dynamicShadows by viewModel.dynamicShadows.collectAsState()
    val richPresence by viewModel.richPresence.collectAsState()
    val showFpsCounter by viewModel.showFpsCounter.collectAsState()
    val enableSnapshots by viewModel.enableSnapshots.collectAsState()
    val advancedDebug by viewModel.advancedDebug.collectAsState()
    val languageSelection by viewModel.languageSelection.collectAsState()
    val lowRamMode by viewModel.lowRamMode.collectAsState()
    val batterySaver by viewModel.batterySaver.collectAsState()
    val autoUpdate by viewModel.autoUpdate.collectAsState()
    val gpuAcceleration by viewModel.gpuAcceleration.collectAsState()
    val sustainedPerformance by viewModel.sustainedPerformance.collectAsState()
    val bigCoreAffinity by viewModel.bigCoreAffinity.collectAsState()
    val useSurfaceView by viewModel.useSurfaceView.collectAsState()

    val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary
    val totalRamMb = com.modulamobile.utils.DeviceRamUtils.getTotalRamMb(context)
    val maxRam = com.modulamobile.utils.DeviceRamUtils.getSafeMaxRamMb(totalRamMb).toFloat().coerceAtLeast(1024f)
    val safeRamAllocation = (ramAllocationMb ?: 512).toFloat().coerceIn(512f, maxRam)
    val totalRamGbStr = String.format(java.util.Locale.US, "%.1f", totalRamMb / 1024f)
    val uriHandler = LocalUriHandler.current

    Box(modifier = Modifier.fillMaxSize().background(ColorBg0)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            
            // Engine Metrics
            item {
                GlassHeroCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GlassSectionHeader("ENGINE METRICS & PERFORMANCE")
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("RAM ALLOCATION", style = ModulaTypography.labelSmall, color = themeAccentColor)
                            val currentGb = String.format(java.util.Locale.US, "%.1f", safeRamAllocation / 1024f)
                            Text("$currentGb GB / $totalRamGbStr GB", style = ModulaTypography.labelSmall, color = themeAccentColor)
                        }
                        
                        GlassSlider(
                            value = safeRamAllocation,
                            onValueChange = { viewModel.setRamAllocation(it.toInt()) },
                            valueRange = 512f..maxRam,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("512MB", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            val recommendedMb = com.movtery.zalithlauncher.setting.findBestRAMAllocation(context)
                            Text("RECOMMENDED: ${recommendedMb}MB", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = themeAccentColor)
                            val maxGbStr = String.format(java.util.Locale.US, "%.1f", maxRam / 1024f)
                            Text("${maxGbStr}GB", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Unlock FPS", style = ModulaTypography.labelSmall, color = Color.White)
                                GlassToggle(checked = unlockFps, onCheckedChange = { viewModel.setUnlockFps(it) })
                            }
                            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Rich Presence", style = ModulaTypography.labelSmall, color = Color.White)
                                GlassToggle(checked = richPresence, onCheckedChange = { viewModel.setRichPresence(it) })
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        GlassDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val isPerformanceMode = sustainedPerformance && bigCoreAffinity && !useSurfaceView
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp)).padding(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Bolt, contentDescription = null, tint = themeAccentColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PERFORMANCE MODE", style = ModulaTypography.labelSmall, color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("REDUCES UI ANIMATION DENSITY AND OPTIMIZES JVM THREAD PRIORITY FOR MINIMUM LAG.", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp, lineHeight = 12.sp), color = TextMuted)
                            }
                            GlassToggle(
                                checked = isPerformanceMode,
                                onCheckedChange = { viewModel.setPerformanceMode(it) }
                            )
                        }
                    }
                }
            }
            
            // Themes
            item {
                GlassCard(variant = GlassVariant.DARK, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GlassSectionHeader("INTERFACE THEMES")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val themes = listOf("DEFAULT", "ONYX", "VOLCANIC", "NEON", "ARCTIC")
                            themes.forEach { theme ->
                                val isSelected = theme == themeSelection
                                Box(
                                    modifier = Modifier
                                        .clickable { viewModel.setThemeSelection(theme) }
                                        .background(Color(0xFF15151A), RoundedCornerShape(4.dp))
                                        .border(1.dp, if (isSelected) themeAccentColor else Color(0xFF2A2A35), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(theme, style = ModulaTypography.labelSmall.copy(fontSize = 9.sp), color = if (isSelected) themeAccentColor else TextMuted)
                                }
                            }
                        }
                    }
                }
            }

            // Visual Fidelity
            item {
                GlassCard(variant = GlassVariant.DARK, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GlassSectionHeader("VISUAL FIDELITY")
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Particle Density ${particleDensity}%", style = ModulaTypography.labelSmall, color = Color.White)
                                GlassSlider(value = particleDensity.toFloat() / 100f, onValueChange = { viewModel.setParticleDensity((it * 100).toInt()) })
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Motion Interpolation ${motionInterpolation}%", style = ModulaTypography.labelSmall, color = Color.White)
                                GlassSlider(value = motionInterpolation.toFloat() / 100f, onValueChange = { viewModel.setMotionInterpolation((it * 100).toInt()) })
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("UI Transparency ${uiTransparency}%", style = ModulaTypography.labelSmall, color = Color.White)
                                GlassSlider(value = uiTransparency.toFloat() / 100f, onValueChange = { viewModel.setUiTransparency((it * 100).toInt()) })
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("UI Scaling ${uiScaling}%", style = ModulaTypography.labelSmall, color = Color.White)
                                GlassSlider(value = uiScaling.toFloat() / 100f, onValueChange = { viewModel.setUiScaling((it * 100).toInt()) }, valueRange = 0.8f..1.2f)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        val toggles = listOf(
                            "Bloom Effects" to Pair(bloomEffects) { b: Boolean -> viewModel.setBloomEffects(b); Unit },
                            "Dynamic Shadows" to Pair(dynamicShadows) { b: Boolean -> viewModel.setDynamicShadows(b); Unit },
                            "Auto-Update" to Pair(autoUpdate) { b: Boolean -> viewModel.setAutoUpdate(b); Unit },
                            "Show FPS Counter" to Pair(showFpsCounter) { b: Boolean -> viewModel.setShowFpsCounter(b); Unit },
                            "Enable Snapshots" to Pair(enableSnapshots) { b: Boolean -> viewModel.setEnableSnapshots(b); Unit },
                            "Advanced Debug" to Pair(advancedDebug) { b: Boolean -> viewModel.setAdvancedDebug(b); Unit },
                            "Battery Saver" to Pair(batterySaver) { b: Boolean -> viewModel.setBatterySaver(b); Unit },
                            "GPU Acceleration" to Pair(gpuAcceleration) { b: Boolean -> viewModel.setGpuAcceleration(b); Unit }
                        )
                        
                        toggles.chunked(2).forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                row.forEach { (name, stateAction) ->
                                    val (state, action) = stateAction
                                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(name, style = ModulaTypography.labelSmall, color = Color.White)
                                        GlassToggle(checked = state, onCheckedChange = action)
                                    }
                                }
                                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            // Language
            item {
                GlassCard(variant = GlassVariant.DARK, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GlassSectionHeader("LANGUAGE PREFERENCES")
                        Text("INTERFACE LANGUAGE SELECTION", style = ModulaTypography.labelSmall.copy(fontSize = 9.sp), color = TextMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        var expanded by remember { mutableStateOf(false) }
                        val languages = listOf("English (US)", "Spanish", "French", "Hindi", "Malayalam (à´®à´²à´¯à´¾à´³à´‚)")
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = true }
                                    .background(Color(0xFF1A1A24), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color(0xFF2A2A35), RoundedCornerShape(4.dp))
                                    .padding(12.dp)
                            ) {
                                Text(languageSelection, style = ModulaTypography.labelSmall, color = Color.White)
                            }
                            androidx.compose.material3.DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color(0xFF1A1A24))
                            ) {
                                languages.forEach { lang ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(lang, style = ModulaTypography.labelSmall, color = Color.White) },
                                        onClick = {
                                            viewModel.setLanguageSelection(lang)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Audio
            item {
                GlassCard(variant = GlassVariant.DARK, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GlassSectionHeader("ADVANCED AUDIO & VOICE COMMUNICATIONS")
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .background(Color(0xFF1A1A24), RoundedCornerShape(4.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("ðŸŽ¤", fontSize = 16.sp, color = themeAccentColor)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("FLUX VOICE ENGINE", style = ModulaTypography.labelSmall, color = Color.White)
                                        Text("NATIVE ALTERNATIVE TO SIMPLE VOICE CHAT", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                                    }
                                }
                                GlassBadge("COMING SOON")
                            }
                        }
                    }
                }
            }

            // Migration Tools
            item {
                GlassCard(variant = GlassVariant.DARK, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("MIGRATION TOOLS", style = ModulaTypography.labelSmall, color = Color.White)
                            Text("Import data from other launchers", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                        }
                        GlassGhostButton("IMPORT", onClick = { 
                            importValue = "My Minecraft Launcher"
                            showImportDialog = true 
                        })
                    }
                }
            }

            // Experimental
            item {
                GlassCard(variant = GlassVariant.DARK, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GlassSectionHeader("SYSTEMS & EXPERIMENTAL")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f).background(Color(0xFF1A1A24), RoundedCornerShape(4.dp)).padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text("Touch Haptics", style = ModulaTypography.labelSmall, color = Color.White)
                                    GlassToggle(checked = AllSettings.touchHaptics.state, onCheckedChange = { AllSettings.touchHaptics.save(it) })
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tactile vibration ripples", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                            Column(modifier = Modifier.weight(1f).background(Color(0xFF1A1A24), RoundedCornerShape(4.dp)).padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text("Secure Boot", style = ModulaTypography.labelSmall, color = Color.White)
                                    GlassToggle(checked = AllSettings.secureBoot.state, onCheckedChange = { AllSettings.secureBoot.save(it) })
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Safeguards file verification", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                        }
                    }
                }
            }

            // JVM
            item {
                GlassCard(variant = GlassVariant.DARK, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GlassSectionHeader("CONSOLE PARAMETERS")
                        if (isEditingJvm) {
                            GlassTextField(
                                value = tempJvmArgs,
                                onValueChange = { tempJvmArgs = it },
                                modifier = Modifier.fillMaxWidth().height(150.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                GlassGhostButton("CANCEL", onClick = { 
                                    tempJvmArgs = jvmArgs
                                    isEditingJvm = false 
                                })
                                Spacer(modifier = Modifier.width(8.dp))
                                GlassButton("SAVE", onClick = {
                                    viewModel.setJvmArgs(tempJvmArgs)
                                    isEditingJvm = false
                                })
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF0A0A0F), RoundedCornerShape(4.dp)).padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = jvmArgs.ifEmpty { "Default JVM Arguments" },
                                        style = ModulaTypography.labelSmall, 
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "EDIT",
                                        style = ModulaTypography.labelSmall,
                                        color = themeAccentColor,
                                        modifier = Modifier.clickable { isEditingJvm = true }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("OPTIMIZED FOR ARM INJECTION", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                    }
                }
            }

            // About
            item {
                GlassCard(variant = GlassVariant.DARK, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GlassSectionHeader("ABOUT MODULA MOBILE")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(com.movtery.zalithlauncher.R.drawable.ic_modula_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("MODULA MOBILE", style = ModulaTypography.titleLarge, color = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    GlassBadge("V1.0.0-GOLDEN")
                                }
                                Text("BUILD 2026-05-22", style = ModulaTypography.labelSmall.copy(fontSize = 9.sp), color = TextMuted)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            GlassGhostButton("WEBSITE", onClick = { uriHandler.openUri("https://www.modulamc.in/") }, modifier = Modifier.weight(1f))
                            GlassGhostButton("DISCORD", onClick = { uriHandler.openUri("https://discord.gg/ZKaDavTxnJ") }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassGhostButton("CLEAR GAME CACHE", onClick = { showCacheDialog = true }, modifier = Modifier.fillMaxWidth())
                    GlassGhostButton("RESET SETTINGS TO DEFAULT", onClick = { showResetDialog = true }, modifier = Modifier.fillMaxWidth())
                    GlassButton("CHECK FOR UPDATES", onClick = {
                        scope.launch {
                            try {
                                upgradeViewModel.checkManually(
                                    onInProgress = {
                                        android.widget.Toast.makeText(context, "Checking for updates...", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    onIsLatest = {
                                        android.widget.Toast.makeText(context, "You are on the latest version!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Check too frequent or failed", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth())
                    GlassButton("LOGOUT SESSION", onClick = { showLogoutDialog = true }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }

    if (showCacheDialog) {
        AlertDialog(
            onDismissRequest = { showCacheDialog = false },
            title = { Text("Clear Game Cache", color = Color.White, style = ModulaTypography.titleLarge) },
            text = { Text("Are you sure you want to clear the game cache? This will NOT delete worlds or screenshots.", color = TextMuted, style = ModulaTypography.labelSmall) },
            confirmButton = {
                GlassButton("CLEAR", onClick = { 
                    coil3.SingletonImageLoader.get(context).diskCache?.clear()
                    coil3.SingletonImageLoader.get(context).memoryCache?.clear()
                    context.cacheDir.deleteRecursively()
                    context.externalCacheDir?.deleteRecursively()
                    android.widget.Toast.makeText(context, "Cache cleared successfully", android.widget.Toast.LENGTH_SHORT).show()
                    showCacheDialog = false 
                })
            },
            dismissButton = {
                GlassGhostButton("CANCEL", onClick = { showCacheDialog = false })
            },
            containerColor = Color(0xFF15151A)
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout Session", color = Color(0xFFFF5252), style = ModulaTypography.titleLarge) },
            text = { Text("Are you sure you want to log out from Minecraft? You will need to re-authenticate.", color = TextMuted, style = ModulaTypography.labelSmall) },
            confirmButton = {
                Button(
                    onClick = { 
                        com.movtery.zalithlauncher.game.account.AccountsManager.currentAccountFlow.value?.let { 
                            com.movtery.zalithlauncher.game.account.AccountsManager.deleteAccount(it) 
                        }
                        coil3.SingletonImageLoader.get(context).memoryCache?.clear()
                        com.modulamobile.discord.DiscordRPCManager.shutdown()
                        onLogout()
                        showLogoutDialog = false 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) { Text("LOGOUT", color = Color.White) }
            },
            dismissButton = {
                GlassGhostButton("CANCEL", onClick = { showLogoutDialog = false })
            },
            containerColor = Color(0xFF15151A)
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings", color = Color(0xFFFF5252), style = ModulaTypography.titleLarge) },
            text = { Text("Are you absolutely sure? This will wipe all your custom configurations.", color = TextMuted, style = ModulaTypography.labelSmall) },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.resetToDefaults()
                        showResetDialog = false 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) { Text("RESET ALL", color = Color.White) }
            },
            dismissButton = {
                GlassGhostButton("CANCEL", onClick = { showResetDialog = false })
            },
            containerColor = Color(0xFF15151A)
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Launcher Data", color = Color.White, style = ModulaTypography.titleLarge) },
            text = {
                Column {
                    Text("Enter the name of the launcher you want to import data from:", color = TextMuted, style = ModulaTypography.labelSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassTextField(
                        value = importValue,
                        onValueChange = { importValue = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                GlassButton("IMPORT", onClick = { 
                    val intent = android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT).apply {
                        type = "application/zip"
                        addCategory(android.content.Intent.CATEGORY_OPENABLE)
                    }
                    migrationLauncher.launch(intent)
                    showImportDialog = false 
                })
            },
            dismissButton = {
                GlassGhostButton("CANCEL", onClick = { showImportDialog = false })
            },
            containerColor = Color(0xFF15151A)
        )
    }
}


