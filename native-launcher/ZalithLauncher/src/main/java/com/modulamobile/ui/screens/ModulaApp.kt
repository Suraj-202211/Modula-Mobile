package com.modulamobile.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.modulamobile.auth.AuthRepository
import com.modulamobile.ui.glass.GlassBottomNav
import com.modulamobile.ui.glass.GlassChip
import com.modulamobile.ui.motion.popBackEnter
import com.modulamobile.ui.motion.popBackExit
import com.modulamobile.ui.motion.pushForwardEnter
import com.modulamobile.ui.motion.pushForwardExit
import com.modulamobile.ui.motion.tabEnter
import com.modulamobile.ui.motion.tabExit

@Composable
fun ModulaApp(
    authRepository: AuthRepository,
    settingsViewModel: com.modulamobile.ui.settings.SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val navController = rememberNavController()
    val currentAccount by authRepository.currentAccountFlow.collectAsState(initial = null)
    
    val startDestination = if (currentAccount == null) "login" else "home"
    var currentRoute by remember { mutableStateOf(startDestination) }

    val themeSelection by settingsViewModel.themeSelection.collectAsState()
    val particleDensity by settingsViewModel.particleDensity.collectAsState()
    val motionInterpolation by settingsViewModel.motionInterpolation.collectAsState()
    val uiTransparency by settingsViewModel.uiTransparency.collectAsState()
    val uiScaling by settingsViewModel.uiScaling.collectAsState()
    val bloomEffects by settingsViewModel.bloomEffects.collectAsState()
    val dynamicShadows by settingsViewModel.dynamicShadows.collectAsState()
    val performanceMode by settingsViewModel.sustainedPerformance.collectAsState()

    val currentColors = com.modulamobile.ui.theme.ThemeManager.getColors(themeSelection)
    val currentUiSettings = com.modulamobile.ui.state.ModulaUiSettings(
        particleDensity = particleDensity / 100f,
        motionInterpolation = motionInterpolation / 100f,
        uiTransparency = uiTransparency / 100f,
        uiScaling = uiScaling / 100f,
        bloomEnabled = bloomEffects,
        shadowsEnabled = dynamicShadows,
        performanceMode = performanceMode
    )

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route ?: "home"
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        com.modulamobile.ui.theme.LocalModulaColors provides currentColors,
        com.modulamobile.ui.state.LocalUiSettings provides currentUiSettings
    ) {
        Scaffold(
            bottomBar = {
                if (currentRoute in listOf("home", "profile", "settings")) {
                    GlassBottomNav {
                        GlassChip(text = "HOME", selected = currentRoute == "home")
                        GlassChip(text = "PROFILE", selected = currentRoute == "profile")
                        GlassChip(text = "SETTINGS", selected = currentRoute == "settings")
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    enterTransition = { pushForwardEnter() },
                    exitTransition = { pushForwardExit() },
                    popEnterTransition = { popBackEnter() },
                    popExitTransition = { popBackExit() }
                ) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable("home") {
                        HomeScreen(
                            onNavigateToVersions = { navController.navigate("versions") },
                            onNavigateToSettings = { navController.navigate("settings") },
                            onNavigateToMods = { navController.navigate("mods") }
                        )
                    }
                    
                    composable("profile") {
                        ProfileScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable("settings") {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable("versions") {
                        VersionsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable("mods") {
                        ModsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
