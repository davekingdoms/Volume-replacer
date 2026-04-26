package com.davekingdoms.pixelvolumehelper.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.davekingdoms.pixelvolumehelper.ui.home.HomeScreen
import com.davekingdoms.pixelvolumehelper.settings.SettingsScreen

/** Top-level destinations used in navigation. */
object Destinations {
    const val HOME = "home"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME,
        modifier = modifier,
    ) {
        composable(Destinations.HOME) {
            HomeScreen(onOpenSettings = { navController.navigate(Destinations.SETTINGS) })
        }
        composable(Destinations.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

