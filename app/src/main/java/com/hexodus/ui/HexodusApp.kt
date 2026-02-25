package com.hexodus.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun HexodusApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            FeatureDashboardScreen(navController)
        }
        composable("main") {
            MainActivityScreen(navController)
        }
        composable("preview/{hexColor}/{themeName}") { backStackEntry ->
            val hexColor = backStackEntry.arguments?.getString("hexColor") ?: "#FF6200EE"
            val themeName = backStackEntry.arguments?.getString("themeName") ?: "Default Theme"
            ThemePreviewScreen(
                hexColor = hexColor,
                themeName = themeName,
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable("features/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "Theming"
            FeatureExplorerScreen(
                category = category,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("awesome_shizuku") {
            AwesomeShizukuScreen(navController)
        }
        composable("shizuku_detail/{appName}") { backStackEntry ->
            val appName = backStackEntry.arguments?.getString("appName") ?: ""
            ShizukuAppDetailScreen(navController, appName)
        }
        composable("custom_mods") {
            CustomModsScreen(navController)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
    }
}