package studio.hazeray.applimit.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.hazeray.applimit.ui.appselect.AppSelectScreen
import studio.hazeray.applimit.ui.appselect.AppSelectViewModel
import studio.hazeray.applimit.ui.debug.DebugScreen
import studio.hazeray.applimit.ui.debug.DebugViewModel
import studio.hazeray.applimit.ui.main.MainScreen
import studio.hazeray.applimit.ui.main.MainViewModel
import studio.hazeray.applimit.ui.permission.PermissionScreen
import studio.hazeray.applimit.ui.settings.AppSettingsScreen
import studio.hazeray.applimit.ui.settings.SettingsScreen
import studio.hazeray.applimit.ui.settings.SettingsViewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "permission") {
        composable("permission") {
            PermissionScreen(
                onAllGranted = {
                    navController.navigate("main") {
                        popUpTo("permission") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            val viewModel = hiltViewModel<MainViewModel>()
            MainScreen(
                viewModel = viewModel,
                onAddApp = { navController.navigate("app_select") },
                onAppClick = { appId -> navController.navigate("settings/$appId") },
                onAppSettings = { navController.navigate("app_settings") }
            )
        }

        composable("app_settings") {
            AppSettingsScreen(
                onBack = { navController.popBackStack() },
                onDebug = { navController.navigate("debug") }
            )
        }

        composable("debug") {
            val viewModel = hiltViewModel<DebugViewModel>()
            DebugScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("app_select") {
            val viewModel = hiltViewModel<AppSelectViewModel>()
            AppSelectScreen(
                viewModel = viewModel,
                onAppAdded = { navController.popBackStack() }
            )
        }

        composable(
            route = "settings/{appId}",
            arguments = listOf(navArgument("appId") { type = NavType.LongType })
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getLong("appId") ?: return@composable
            val viewModel = hiltViewModel<SettingsViewModel, SettingsViewModel.Factory> { factory ->
                factory.create(appId)
            }
            SettingsScreen(
                viewModel = viewModel,
                onDeleted = { navController.popBackStack() }
            )
        }
    }
}
