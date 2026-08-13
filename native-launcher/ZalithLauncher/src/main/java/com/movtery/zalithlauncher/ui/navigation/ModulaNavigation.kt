package com.movtery.zalithlauncher.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.movtery.zalithlauncher.ui.theme.*

sealed class ModulaScreen(val route: String, val title: String, val icon: ImageVector) {
    object Home : ModulaScreen("home", "Home", Icons.Rounded.Home)
    object Versions : ModulaScreen("versions", "Versions", Icons.Rounded.Inventory)
    object Mods : ModulaScreen("mods", "Mods", Icons.Rounded.Extension)
    object Modpacks : ModulaScreen("modpacks", "Modpacks", Icons.Rounded.ViewModule)
    object Settings : ModulaScreen("settings", "Settings", Icons.Rounded.Settings)
    object Profile : ModulaScreen("profile", "Profile", Icons.Rounded.Person)
}

val bottomNavItems = listOf(
    ModulaScreen.Home,
    ModulaScreen.Versions,
    ModulaScreen.Mods,
    ModulaScreen.Modpacks,
    ModulaScreen.Settings,
    ModulaScreen.Profile
)

@Composable
fun ModulaBottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Column(modifier = Modifier.fillMaxWidth()) {
        // Top glow line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            FluxGold.copy(alpha = 0.4f),
                            FluxGold.copy(alpha = 0.6f),
                            FluxGold.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        NavigationBar(
            containerColor = Bg0.copy(alpha = 0.95f),
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(80.dp) // Slightly taller to accommodate padding + 44dp containers
                .padding(horizontal = 4.dp)
        ) {
            bottomNavItems.forEach { screen ->
                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                NavigationBarItem(
                    icon = {
                        Box(
                            modifier = Modifier.size(44.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(if (selected) 26.dp else 22.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = screen.title.uppercase(),
                            color = if (selected) FluxGold else TextMuted,
                            style = LabelSM.copy(
                                fontSize = 9.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        )
                    },
                    selected = selected,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FluxGold,
                        unselectedIconColor = TextMuted,
                        selectedTextColor = FluxGold,
                        unselectedTextColor = TextMuted,
                        indicatorColor = FluxGold.copy(alpha = 0.12f)
                    ),
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
