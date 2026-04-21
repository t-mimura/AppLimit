package com.example.applimit.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.applimit.ui.appselect.AppSelectScreen
import com.example.applimit.ui.appselect.AppSelectViewModel
import com.example.applimit.ui.main.MainScreen
import com.example.applimit.ui.main.MainViewModel
import com.example.applimit.ui.permission.PermissionScreen
import com.example.applimit.ui.settings.SettingsScreen
import com.example.applimit.ui.settings.SettingsViewModel

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
                onAppClick = { appId -> navController.navigate("settings/$appId") }
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
