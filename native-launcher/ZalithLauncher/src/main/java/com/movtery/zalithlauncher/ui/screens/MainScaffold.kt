package com.movtery.zalithlauncher.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.movtery.zalithlauncher.ui.particles.FluxParticleBackground
import com.movtery.zalithlauncher.ui.navigation.ModulaBottomNavigation
import com.movtery.zalithlauncher.ui.theme.Bg0
import com.modulamobile.ui.screens.HomeScreen
import com.modulamobile.ui.screens.ModsScreen
import com.modulamobile.ui.screens.SettingsScreen
import com.modulamobile.ui.screens.ProfileScreen
import com.modulamobile.ui.screens.VersionsScreen
import com.modulamobile.ui.screens.LoginScreen
import com.modulamobile.ui.screens.ModpacksScreen
import com.movtery.zalithlauncher.game.account.AccountsManager

import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.runtime.collectAsState
import com.movtery.zalithlauncher.ui.screens.content.elements.LaunchGameOperation
import com.movtery.zalithlauncher.viewmodel.LaunchGameViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.delay

@Composable
fun MainScaffold(
    navController: NavHostController,
    launchGameViewModel: LaunchGameViewModel,
    errorViewModel: ErrorViewModel
) {
    val activity = LocalContext.current as Activity
    val launchGameOperation = launchGameViewModel.launchGameOperation

    // LaunchGameOperation handles the pre-launch checks, UI dialogs, and actual game launch
    LaunchGameOperation(
        activity = activity,
        launchGameOperation = launchGameOperation,
        updateOperation = launchGameViewModel::updateOperation,
        exitActivity = { activity.moveTaskToBack(false) },
        waitForVulkanChecker = {
            val mainActivity = activity as? com.movtery.zalithlauncher.ui.activities.MainActivity
            if (mainActivity != null) {
                mainActivity.eventViewModel.sendEvent(com.movtery.zalithlauncher.viewmodel.EventViewModel.Event.VulkanCheck)
                delay(100)
            }
        },
        submitError = { errorViewModel.showError(it) },
        toAccountManageScreen = { navController.navigate("login") },
        toVersionManageScreen = { navController.navigate("versions") }
    )

    val navBackStackEntry by
        navController.currentBackStackEntryAsState()
    val currentRoute =
        navBackStackEntry?.destination?.route ?: "home"

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Bg0)
        .systemBarsPadding()
    ) {

        // Layer 1: Particles (behind everything)
        FluxParticleBackground(
            modifier  = Modifier.fillMaxSize(),
            intensity = 0.7f
        )

        // Layer 2: Screen content + bottom nav
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                val currentAccount by AccountsManager.currentAccountFlow.collectAsState()
                
                LaunchedEffect(currentAccount) {
                    if (currentAccount == null && currentRoute != "login") {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                
                NavHost(
                    navController    = navController,
                    startDestination = if (AccountsManager.currentAccountFlow.value == null) "login" else "home",
                    enterTransition  = {
                        slideInHorizontally(
                            initialOffsetX = { (it * 0.3f).toInt() },
                            animationSpec  = tween(300)
                        ) + fadeIn(tween(300))
                    },
                    exitTransition   = {
                        slideOutHorizontally(
                            targetOffsetX = { -(it * 0.15f).toInt() },
                            animationSpec = tween(300)
                        ) + fadeOut(tween(200))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -(it * 0.15f).toInt() },
                            animationSpec  = tween(300)
                        ) + fadeIn(tween(300))
                    },
                    popExitTransition  = {
                        slideOutHorizontally(
                            targetOffsetX = { (it * 0.3f).toInt() },
                            animationSpec = tween(300)
                        ) + fadeOut(tween(200))
                    }
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
                    composable("home")    { 
                        HomeScreen(
                            onNavigateToVersions = { navController.navigate("versions") },
                            onNavigateToSettings = { navController.navigate("settings") },
                            onNavigateToMods = { navController.navigate("mods") },
                            onLaunchGame = { version ->
                                launchGameViewModel.tryLaunch(version)
                            }
                        ) 
                    }
                    composable("versions"){ VersionsScreen(onBack = { navController.popBackStack() }) }
                    composable("mods")    { ModsScreen(onBack = { navController.popBackStack() }) }
                    composable("modpacks"){ ModpacksScreen(onBack = { navController.popBackStack() }) }
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
                    composable("profile") { 
                        ProfileScreen(
                            onBack = { navController.popBackStack() }
                        ) 
                    }
                }
            }

            // Layer 3: Bottom nav (always visible unless on login screen)
            if (currentRoute != "login") {
                ModulaBottomNavigation(navController = navController)
            }
        }
    }
}


