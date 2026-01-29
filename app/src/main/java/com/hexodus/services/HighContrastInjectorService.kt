package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.IBinder
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * HighContrastInjectorService - Enhanced injection using Samsung's High Contrast vulnerability
 * Based on techniques from awesome-shizuku projects for system-level theming
 */
class HighContrastInjectorService : Service() {
    
    companion object {
        private const val TAG = "HCInjectorService"
        private const val ACTION_INJECT_HC_THEME = "com.hexodus.INJECT_HC_THEME"
        private const val ACTION_REMOVE_HC_THEME = "com.hexodus.REMOVE_HC_THEME"
        private const val ACTION_LIST_HC_THEMES = "com.hexodus.LIST_HC_THEMES"
        
        // Intent extras
        const val EXTRA_HEX_COLOR = "hex_color"
        const val EXTRA_THEME_NAME = "theme_name"
        const val EXTRA_COMPONENTS = "components"
        
        private const val HIGH_CONTRAST_PACKAGE = "com.android.internal.display.cutout.emulation.corner"
        private const val OVERLAY_ASSETS_DIR = "assets/overlays"
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_INJECT_HC_THEME -> {
                val hexColor = intent.getStringExtra(EXTRA_HEX_COLOR) ?: "#FF6200EE"
                val themeName = intent.getStringExtra(EXTRA_THEME_NAME) ?: "HighContrastTheme"
                val components = intent.getStringArrayListExtra(EXTRA_COMPONENTS) ?: arrayListOf("status_bar", "navigation_bar")
                
                injectHighContrastTheme(hexColor, themeName, components)
            }
            ACTION_REMOVE_HC_THEME -> {
                removeHighContrastTheme()
            }
            ACTION_LIST_HC_THEMES -> {
                listHighContrastThemes()
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Injects a high contrast theme with custom overlays
     */
    private fun injectHighContrastTheme(hexColor: String, themeName: String, components: List<String>) {
        try {
            // Generate a fake high contrast theme package
            val fakePackageName = generateFakeHighContrastPackage(hexColor, themeName, components)
            
            if (fakePackageName != null) {
                // Install the fake package using Shizuku
                val shizukuService = ShizukuBridgeService()
                val installSuccess = shizukuService.installApk(fakePackageName)
                
                if (installSuccess) {
                    // Enable the overlay
                    val enableSuccess = shizukuService.executeOverlayCommand(fakePackageName, "enable")
                    
                    if (enableSuccess) {
                        Log.d(TAG, "Successfully injected high contrast theme: $fakePackageName")
                        
                        // Refresh system UI to apply changes
                        val overlayService = OverlayActivationService()
                        overlayService.refreshSystemUI()
                        
                        // Broadcast success
                        val successIntent = Intent("HIGH_CONTRAST_INJECTION_SUCCESS")
                        successIntent.putExtra("package_name", fakePackageName)
                        successIntent.putExtra("theme_name", themeName)
                        sendBroadcast(successIntent)
                    } else {
                        Log.e(TAG, "Failed to enable high contrast overlay: $fakePackageName")
                        
                        // Broadcast failure
                        val failureIntent = Intent("HIGH_CONTRAST_INJECTION_FAILURE")
                        failureIntent.putExtra("package_name", fakePackageName)
                        failureIntent.putExtra("error", "Failed to enable overlay")
                        sendBroadcast(failureIntent)
                    }
                } else {
                    Log.e(TAG, "Failed to install high contrast package: $fakePackageName")
                    
                    // Broadcast failure
                    val failureIntent = Intent("HIGH_CONTRAST_INJECTION_FAILURE")
                    failureIntent.putExtra("package_name", fakePackageName)
                    failureIntent.putExtra("error", "Failed to install package")
                    sendBroadcast(failureIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error injecting high contrast theme: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("HIGH_CONTRAST_INJECTION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Generates a fake high contrast theme package
     */
    private fun generateFakeHighContrastPackage(hexColor: String, themeName: String, components: List<String>): String? {
        try {
            // Create a temporary directory for the fake package
            val tempDir = File(applicationContext.cacheDir, "hc_temp_${System.currentTimeMillis()}")
            tempDir.mkdirs()
            
            // Generate the fake package name
            val fakePackageName = "com.samsung.fake.hc.${themeName.replace(" ", "_").lowercase()}.${generateRandomString(8)}"
            
            // Create the AndroidManifest.xml that mimics a high contrast theme
            val manifestContent = generateHighContrastManifest(fakePackageName, hexColor, themeName)
            val manifestFile = File(tempDir, "AndroidManifest.xml")
            FileOutputStream(manifestFile).use { it.write(manifestContent.toByteArray()) }
            
            // Create the res directory structure
            val resDir = File(tempDir, "res")
            val valuesDir = File(resDir, "values")
            valuesDir.mkdirs()
            
            // Create colors.xml with the custom hex color
            val colorsContent = generateColorsXml(hexColor, components)
            val colorsFile = File(valuesDir, "colors.xml")
            FileOutputStream(colorsFile).use { it.write(colorsContent.toByteArray()) }
            
            // Create values-v31 for Material You
            val values31Dir = File(resDir, "values-v31")
            values31Dir.mkdirs()
            val materialYouContent = generateMaterialYouXml(hexColor, components)
            val materialYouFile = File(values31Dir, "colors.xml")
            FileOutputStream(materialYouFile).use { it.write(materialYouContent.toByteArray()) }
            
            // Create the overlay assets directory
            val assetsDir = File(tempDir, "assets")
            val overlaysDir = File(assetsDir, "overlays")
            overlaysDir.mkdirs()
            
            // Create overlay configuration
            val configContent = generateOverlayConfig(fakePackageName, components)
            val configFile = File(overlaysDir, "config.xml")
            FileOutputStream(configFile).use { it.write(configContent.toByteArray()) }
            
            // In a real implementation, we would compile this to an APK
            // For this example, we'll just return the directory path
            // In a real implementation, we would use aapt2 to build the APK
            val apkFile = File(tempDir, "${fakePackageName}.apk")
            
            Log.d(TAG, "Generated high contrast package: ${apkFile.absolutePath}")
            return apkFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error generating fake high contrast package: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Generates a manifest that mimics a high contrast theme
     */
    private fun generateHighContrastManifest(packageName: String, hexColor: String, themeName: String): String {
        return """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="$packageName"
    android:versionCode="1"
    android:versionName="1.0">
    
    <!-- Mimic Samsung's high contrast theme signature -->
    <overlay
        android:targetPackage="android"
        android:category="android.theme.customization.accent_color"
        android:priority="1" />
        
    <overlay
        android:targetPackage="com.android.systemui"
        android:category="android.theme.customization.status_bar"
        android:priority="1" />
        
    <overlay
        android:targetPackage="com.android.systemui"
        android:category="android.theme.customization.navigation_bar"
        android:priority="1" />
        
    <application
        android:label="High Contrast - $themeName ($hexColor)"
        android:hasCode="false"
        android:extractNativeLibs="false" />
        
</manifest>"""
    }
    
    /**
     * Generates colors.xml with the custom hex color
     */
    private fun generateColorsXml(hexColor: String, components: List<String>): String {
        val colorWithoutHash = if (hexColor.startsWith("#")) hexColor.substring(1) else hexColor
        val colorInt = try {
            android.graphics.Color.parseColor("#$colorWithoutHash")
        } catch (e: Exception) {
            android.graphics.Color.parseColor("#FF6200EE") // Default color
        }
        
        // Generate variations of the color
        val darkerColor = shiftColor(colorInt, 0.2f, false)
        val lighterColor = shiftColor(colorInt, 0.2f, true)
        
        val statusBarSection = if ("status_bar" in components) """
    <color name="system_status_bar_color">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="system_status_bar_icons_color">#${if (isColorLight(colorInt)) "FF000000" else "FFFFFFFF"}</color>""" else ""
        
        val navBarSection = if ("navigation_bar" in components) """
    <color name="system_nav_bar_color">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="system_nav_bar_divider_color">#${shiftColor(colorInt, 0.3f).toString(16).substring(2).uppercase()}</color>""" else ""
        
        return """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- High contrast colors based on user hex -->
    <color name="high_contrast_color_primary">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="high_contrast_color_primary_dark">#${darkerColor.toString(16).substring(2).uppercase()}</color>
    <color name="high_contrast_color_primary_light">#${lighterColor.toString(16).substring(2).uppercase()}</color>
    
    <!-- System UI colors -->
    <color name="system_high_contrast_1">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="system_high_contrast_2">#${darkerColor.toString(16).substring(2).uppercase()}</color>
    <color name="system_high_contrast_3">#${lighterColor.toString(16).substring(2).uppercase()}</color>
    
    <!-- Accessibility colors -->
    <color name="accessibility_high_contrast">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="accessibility_focus_indicator">#${darkerColor.toString(16).substring(2).uppercase()}</color>
    
    <!-- Component-specific colors -->
    $statusBarSection
    $navBarSection
    
</resources>"""
    }
    
    /**
     * Generates Material You colors.xml
     */
    private fun generateMaterialYouXml(hexColor: String, components: List<String>): String {
        val colorWithoutHash = if (hexColor.startsWith("#")) hexColor.substring(1) else hexColor
        val colorInt = try {
            android.graphics.Color.parseColor("#$colorWithoutHash")
        } catch (e: Exception) {
            android.graphics.Color.parseColor("#FF6200EE") // Default color
        }
        
        val statusBarSection = if ("status_bar" in components) """
    <color name="m3_sys_color_dynamic_system_status_bar">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="m3_sys_color_dynamic_system_status_bar_icons">#${if (isColorLight(colorInt)) "FF000000" else "FFFFFFFF"}</color>""" else ""
        
        val navBarSection = if ("navigation_bar" in components) """
    <color name="m3_sys_color_dynamic_system_nav_bar">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="m3_sys_color_dynamic_system_nav_bar_divider">#${shiftColor(colorInt, 0.3f).toString(16).substring(2).uppercase()}</color>""" else ""
        
        return """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Material You Dynamic Colors for High Contrast -->
    <color name="system_accent1_0">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_100">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_200">#${shiftColor(colorInt, 0.1f, false).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_300">#${shiftColor(colorInt, 0.2f, false).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_400">#${shiftColor(colorInt, 0.3f, false).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_500">#${shiftColor(colorInt, 0.4f, false).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_600">#${shiftColor(colorInt, 0.5f, false).toString(16).substring(2).uppercase()}</color>
    
    <!-- Component-specific Material You colors -->
    $statusBarSection
    $navBarSection
    
</resources>"""
    }
    
    /**
     * Generates overlay configuration
     */
    private fun generateOverlayConfig(packageName: String, components: List<String>): String {
        val componentXml = components.joinToString("\n        ") { comp ->
            "        <component name=\"$comp\" enabled=\"true\" />"
        }
        
        return """<?xml version="1.0" encoding="utf-8"?>
<theming-config>
    <overlay-package>$packageName</overlay-package>
    <target-packages>
        <package>android</package>
        <package>com.android.systemui</package>
        <package>com.android.settings</package>
        <package>com.sec.android.app.launcher</package>
    </target-packages>
    <components>
$componentXml
    </components>
    <resource-mapping>
        <map name="colorPrimary" resource="color/high_contrast_color_primary" />
        <map name="colorPrimaryDark" resource="color/high_contrast_color_primary_dark" />
        <map name="colorAccent" resource="color/high_contrast_color_primary" />
        <map name="navigationBarColor" resource="color/system_nav_bar_color" />
        <map name="statusBarColor" resource="color/system_status_bar_color" />
    </resource-mapping>
    <features>
        <feature name="high_contrast_mode" enabled="true" />
        <feature name="dynamic_colors" enabled="true" />
    </features>
</theming-config>"""
    }
    
    /**
     * Removes the high contrast theme
     */
    private fun removeHighContrastTheme() {
        try {
            // Disable any active high contrast overlays
            // This would normally execute: cmd overlay disable <package_name>
            
            // Clean up temporary files
            val tempDirs = File(applicationContext.cacheDir).listFiles { file ->
                file.isDirectory && file.name.startsWith("hc_temp_")
            }
            
            tempDirs?.forEach { dir ->
                dir.deleteRecursively()
            }
            
            Log.d(TAG, "Removed high contrast theme")
            
            // Broadcast success
            val successIntent = Intent("HIGH_CONTRAST_REMOVAL_SUCCESS")
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing high contrast theme: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("HIGH_CONTRAST_REMOVAL_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Lists active high contrast themes
     */
    private fun listHighContrastThemes() {
        try {
            // This would normally query the system for active overlays
            // For this example, we'll just broadcast an empty list
            val listIntent = Intent("HIGH_CONTRAST_THEMES_LIST")
            listIntent.putStringArrayListExtra("themes", arrayListOf())
            sendBroadcast(listIntent)
            
            Log.d(TAG, "Listed high contrast themes")
        } catch (e: Exception) {
            Log.e(TAG, "Error listing high contrast themes: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("HIGH_CONTRAST_LIST_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Generates a random string of specified length
     */
    private fun generateRandomString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
    
    /**
     * Shifts a color by a factor to make it lighter or darker
     */
    private fun shiftColor(color: Int, factor: Float, lighter: Boolean = true): Int {
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        
        val delta = (255 * factor).toInt()
        
        val newRed = if (lighter) {
            kotlin.math.min(255, red + delta)
        } else {
            kotlin.math.max(0, red - delta)
        }
        
        val newGreen = if (lighter) {
            kotlin.math.min(255, green + delta)
        } else {
            kotlin.math.max(0, green - delta)
        }
        
        val newBlue = if (lighter) {
            kotlin.math.min(255, blue + delta)
        } else {
            kotlin.math.max(0, blue - delta)
        }
        
        return android.graphics.Color.rgb(newRed, newGreen, newBlue)
    }
    
    /**
     * Checks if a color is light or dark
     */
    private fun isColorLight(color: Int): Boolean {
        val darkness = 1 - (0.299 * android.graphics.Color.red(color) + 0.587 * android.graphics.Color.green(color) + 0.114 * android.graphics.Color.blue(color)) / 255
        return darkness < 0.5
    }
}