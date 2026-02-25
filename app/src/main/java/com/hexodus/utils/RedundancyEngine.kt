package com.hexodus.utils

object RedundancyEngine {
    
    // Mapping of Redundant App Names to the Hexodus Feature that replaces them
    private val redundancyMap = mapOf(
        "Hail" to "App Manager",
        "Freeze" to "App Manager",
        "Amarok" to "App Manager",
        "GMS Phixit" to "Circle to Search",
        "Canta" to "App Manager",
        "SystemUI Tuner" to "System Tuner",
        "DarQ" to "App Themer",
        "Galaxy Enhancements" to "Next-Gen OS (Android 16+)",
        "Ambient Music Mod" to "Next-Gen OS (Android 16+)",
        "Smartspacer" to "Next-Gen OS (Android 16+)"
    )

    fun getReplacementFeature(appName: String): String? {
        return redundancyMap[appName]
    }

    fun isAppRedundant(appName: String): Boolean {
        return redundancyMap.containsKey(appName)
    }

    // Mapping of Hexodus Features to potentially conflicting apps
    private val conflictMap = mapOf(
        "App Manager" to listOf("com.aistra.hail", "com.samolepszy.canta", "com.catchingnow.icebox"),
        "System Tuner" to listOf("com.zacharee1.systemuituner", "com.paphonb.settings.database.editor"),
        "App Themer" to listOf("com.kieronquinn.app.darq"),
        "Circle to Search" to listOf("com.google.android.googlequicksearchbox"), // Potential conflict with older versions
        "Next-Gen OS (Android 16+)" to listOf("com.feruzbek.galaxyenhancements", "com.kieronquinn.app.ambientmusicmod")
    )

    fun getConflictingApps(featureTitle: String): List<String> {
        return conflictMap[featureTitle] ?: emptyList()
    }
}
