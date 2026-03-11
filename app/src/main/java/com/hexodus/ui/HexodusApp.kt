package com.hexodus.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

@Composable
fun HexodusApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Dashboard,
        enterTransition = { 
            slideInHorizontally(initialOffsetX = { 400 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) 
        },
        exitTransition = { 
            slideOutHorizontally(targetOffsetX = { -400 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) 
        },
        popEnterTransition = { 
            slideInHorizontally(initialOffsetX = { -400 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) 
        },
        popExitTransition = { 
            slideOutHorizontally(targetOffsetX = { 400 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) 
        }
    ) {
        composable<NavRoutes.Dashboard> {
            FeatureDashboardScreen(navController)
        }
        composable<NavRoutes.Main> {
            MainActivityScreen(navController)
        }
        composable<NavRoutes.Preview> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoutes.Preview>()
            ThemePreviewScreen(
                hexColor = route.hexColor,
                themeName = route.themeName,
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable<NavRoutes.Features> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoutes.Features>()
            FeatureExplorerScreen(
                category = route.category,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable<NavRoutes.AwesomeShizuku> {
            AwesomeShizukuScreen(navController)
        }
        composable<NavRoutes.ShizukuDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoutes.ShizukuDetail>()
            ShizukuAppDetailScreen(navController, route.appName)
        }
        composable<NavRoutes.CustomMods> {
            CustomModsScreen(navController)
        }
        composable<NavRoutes.Settings> {
            SettingsScreen(navController)
        }
    }
}
