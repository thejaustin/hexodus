package com.hexodus.ui

import kotlinx.serialization.Serializable

sealed interface NavRoutes {
    @Serializable
    data object Dashboard : NavRoutes

    @Serializable
    data object Main : NavRoutes

    @Serializable
    data class Preview(val hexColor: String, val themeName: String) : NavRoutes

    @Serializable
    data class Features(val category: String) : NavRoutes

    @Serializable
    data object AwesomeShizuku : NavRoutes

    @Serializable
    data class ShizukuDetail(val appName: String) : NavRoutes

    @Serializable
    data object CustomMods : NavRoutes

    @Serializable
    data object Settings : NavRoutes
}
