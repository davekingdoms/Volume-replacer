package com.davekingdoms.pixelvolumehelper.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.davekingdoms.pixelvolumehelper.data.PreferencesRepository
import com.davekingdoms.pixelvolumehelper.settings.SettingsScreen
import com.davekingdoms.pixelvolumehelper.settings.SettingsViewModel
import com.davekingdoms.pixelvolumehelper.ui.home.HomeScreen

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
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Destinations.HOME,
        modifier = modifier,
    ) {
        composable(Destinations.HOME) {
            HomeScreen(onOpenSettings = { navController.navigate(Destinations.SETTINGS) })
        }
        composable(Destinations.SETTINGS) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(
                    context = context,
                    preferencesRepository = PreferencesRepository(context),
                ),
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

