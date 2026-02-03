package com.hexodus.core

import android.graphics.Color
import android.util.Log
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.random.Random

/**
 * ThemeCompiler - Enhanced compiler for hex colors to system-compatible overlays
 * This is the core engine that converts hex codes to system-compatible overlays
 * with additional features inspired by awesome-shizuku projects
 */
class ThemeCompiler {
    
    companion object {
        private const val TAG = "ThemeCompiler"
        
        // Target directories for overlay APK
        private const val RES_DIR = "res/"
        private const val VALUES_DIR = "res/values/"
        private const val VALUES_NIGHT_DIR = "res/values-night/"
        private const val VALUES_V31_DIR = "res/values-v31/" // Android 12+ Material You
        private const val ANDROID_MANIFEST = "AndroidManifest.xml"
        private const val ASSETS_DIR = "assets/"
        private const val OVERLAYS_DIR = "assets/overlays/"
    }
    
    /**
     * Compiles a hex color theme into an overlay APK structure in memory
     * @param hexColor Primary hex color to use for theming
     * @param packageName Package name for the overlay
     * @param themeName Name of the theme
     * @param themedComponents Map of components to theme
     * @return ByteArray containing the compiled APK
     */
    fun compileTheme(
        hexColor: String, 
        packageName: String, 
        themeName: String = "Custom Theme",
        themedComponents: Map<String, Boolean> = mapOf()
    ): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val zipOutputStream = ZipOutputStream(outputStream)
        
        try {
            // Validate hex color
            val colorInt = parseHexColor(hexColor)
            
            // Generate all required resources
            generateAndroidManifest(zipOutputStream, packageName, hexColor, themeName)
            generateValuesResources(zipOutputStream, colorInt, themedComponents)
            generateNightValuesResources(zipOutputStream, colorInt, themedComponents)
            generateMaterialYouResources(zipOutputStream, colorInt, themedComponents)
            generateOverlayAssets(zipOutputStream, packageName, themedComponents)
            
            // Close the zip stream
            zipOutputStream.close()
            
            Log.d(TAG, "Successfully compiled theme for package: $packageName with color: $hexColor")
            return outputStream.toByteArray()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error compiling theme: ${e.message}", e)
            throw e
        } finally {
            outputStream.close()
        }
    }
    
    /**
     * Parses hex color string to integer
     */
    private fun parseHexColor(hexColor: String): Int {
        var colorStr = hexColor.trim()
        
        // Remove # if present
        if (colorStr.startsWith("#")) {
            colorStr = colorStr.substring(1)
        }
        
        // Ensure it's in the right format (RRGGBB or AARRGGBB)
        if (colorStr.length == 6) {
            colorStr = "FF$colorStr" // Add opaque alpha
        } else if (colorStr.length != 8) {
            throw IllegalArgumentException("Invalid hex color format: $colorStr. Expected RRGGBB or AARRGGBB")
        }
        
        return Color.parseColor("#$colorStr")
    }
    
    /**
     * Generates AndroidManifest.xml for the overlay
     */
    private fun generateAndroidManifest(
        zipOutputStream: ZipOutputStream, 
        packageName: String, 
        hexColor: String,
        themeName: String
    ) {
        val manifestContent = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="$packageName"
    android:versionCode="1"
    android:versionName="1.0">
    
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
        android:label="$themeName - $hexColor"
        android:hasCode="false" />
        
</manifest>"""
        
        addToZip(zipOutputStream, ANDROID_MANIFEST, manifestContent)
    }
    
    /**
     * Generates values/colors.xml with custom colors
     */
    private fun generateValuesResources(
        zipOutputStream: ZipOutputStream, 
        colorInt: Int,
        themedComponents: Map<String, Boolean>
    ) {
        val primaryDark = shiftColor(colorInt, 0.2f)  // Darker version
        val secondary = shiftColor(colorInt, 0.3f, true)  // Lighter version
        val secondaryVariant = shiftColor(colorInt, 0.5f, true)  // Even lighter
        
        val colorsXml = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Generated from hex color -->
    <color name="system_accent_color_0">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="system_accent_color_1">#${shiftColor(colorInt, 0.1f).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent_color_2">#${shiftColor(colorInt, 0.2f).toString(16).substring(2).uppercase()}</color>
    
    <!-- Standard Material colors -->
    <color name="colorPrimary">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="colorPrimaryVariant">#${primaryDark.toString(16).substring(2).uppercase()}</color>
    <color name="colorOnPrimary">#${if (isColorLight(colorInt)) "FF000000" else "FFFFFFFF"}</color>
    
    <color name="colorSecondary">#${secondary.toString(16).substring(2).uppercase()}</color>
    <color name="colorSecondaryVariant">#${secondaryVariant.toString(16).substring(2).uppercase()}</color>
    <color name="colorOnSecondary">#${if (isColorLight(secondary)) "FF000000" else "FFFFFFFF"}</color>
    
    <!-- Samsung-specific colors -->
    <color name="oneui_accent">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="oneui_control_normal">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="oneui_control_pressed">#${shiftColor(colorInt, 0.2f).toString(16).substring(2).uppercase()}</color>
    
    <!-- Component-specific colors if enabled -->
    ${if (themedComponents["status_bar"] == true) """
    <color name="system_status_bar_color">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="system_notification_icon_color">#${if (isColorLight(colorInt)) "FF000000" else "FFFFFFFF"}</color>""" else ""}
    
    ${if (themedComponents["navigation_bar"] == true) """
    <color name="system_navigation_bar_color">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="system_navigation_bar_divider_color">#${shiftColor(colorInt, 0.3f).toString(16).substring(2).uppercase()}</color>""" else ""}
    
    ${if (themedComponents["system_ui"] == true) """
    <color name="system_ui_accent_color">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="system_ui_background_color">#${shiftColor(colorInt, 0.8f, true).toString(16).substring(2).uppercase()}</color>""" else ""}
</resources>"""
        
        addToZip(zipOutputStream, "$VALUES_DIR/colors.xml", colorsXml)
    }
    
    /**
     * Generates night mode values
     */
    private fun generateNightValuesResources(
        zipOutputStream: ZipOutputStream, 
        colorInt: Int,
        themedComponents: Map<String, Boolean>
    ) {
        val nightColorsXml = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Night mode accent colors -->
    <color name="system_accent_color_0">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="system_accent_color_1">#${shiftColor(colorInt, 0.1f).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent_color_2">#${shiftColor(colorInt, 0.2f).toString(16).substring(2).uppercase()}</color>
    
    <!-- Night mode specific colors -->
    <color name="oneui_accent_night">#${colorInt.toString(16).substring(2).uppercase()}</color>
    
    <!-- Component-specific night colors if enabled -->
    ${if (themedComponents["status_bar"] == true) """
    <color name="system_status_bar_color">#${shiftColor(colorInt, 0.1f, false).toString(16).substring(2).uppercase()}</color>""" else ""}
    
    ${if (themedComponents["navigation_bar"] == true) """
    <color name="system_navigation_bar_color">#${shiftColor(colorInt, 0.1f, false).toString(16).substring(2).uppercase()}</color>""" else ""}
</resources>"""
        
        addToZip(zipOutputStream, "$VALUES_NIGHT_DIR/colors.xml", nightColorsXml)
    }
    
    /**
     * Generates Material You (Android 12+) resources
     */
    private fun generateMaterialYouResources(
        zipOutputStream: ZipOutputStream, 
        colorInt: Int,
        themedComponents: Map<String, Boolean>
    ) {
        // Generate dynamic color scheme based on the input color
        val tones = generateTones(colorInt)
        
        val materialYouXml = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Material You Dynamic Colors -->
    <color name="system_accent1_0">#${tones.getValue(50).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_10">#${tones.getValue(100).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_50">#${tones.getValue(200).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_100">#${tones.getValue(300).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_200">#${tones.getValue(400).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_300">#${tones.getValue(500).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_400">#${tones.getValue(600).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_500">#${tones.getValue(700).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_600">#${tones.getValue(800).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_700">#${tones.getValue(900).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent1_800">#${tones.getValue(950).toString(16).substring(2).uppercase()}</color>
    
    <!-- Secondary accent colors -->
    <color name="system_accent2_0">#${shiftColor(colorInt, 0.3f, true).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent2_100">#${shiftColor(colorInt, 0.1f, true).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent2_200">#${colorInt.toString(16).substring(2).uppercase()}</color>
    
    <!-- Tertiary accent colors -->
    <color name="system_accent3_0">#${shiftColor(colorInt, 0.5f, true).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent3_100">#${shiftColor(colorInt, 0.2f, true).toString(16).substring(2).uppercase()}</color>
    <color name="system_accent3_200">#${shiftColor(colorInt, 0.1f, false).toString(16).substring(2).uppercase()}</color>
    
    <!-- Neutral colors based on primary -->
    <color name="system_neutral1_0">#${shiftColor(colorInt, 0.8f, true).toString(16).substring(2).uppercase()}</color>
    <color name="system_neutral1_10">#${shiftColor(colorInt, 0.6f, true).toString(16).substring(2).uppercase()}</color>
    <color name="system_neutral1_50">#${shiftColor(colorInt, 0.4f, true).toString(16).substring(2).uppercase()}</color>
    <color name="system_neutral1_100">#${shiftColor(colorInt, 0.2f, true).toString(16).substring(2).uppercase()}</color>
    <color name="system_neutral1_200">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="system_neutral1_300">#${shiftColor(colorInt, 0.2f, false).toString(16).substring(2).uppercase()}</color>
    <color name="system_neutral1_400">#${shiftColor(colorInt, 0.4f, false).toString(16).substring(2).uppercase()}</color>
    <color name="system_neutral1_500">#${shiftColor(colorInt, 0.6f, false).toString(16).substring(2).uppercase()}</color>
    <color name="system_neutral1_600">#${shiftColor(colorInt, 0.8f, false).toString(16).substring(2).uppercase()}</color>
    <color name="system_neutral1_700">#${shiftColor(colorInt, 0.9f, false).toString(16).substring(2).uppercase()}</color>
    <color name="system_neutral1_800">#${shiftColor(colorInt, 0.95f, false).toString(16).substring(2).uppercase()}</color>
    <color name="system_neutral1_900">#${shiftColor(colorInt, 0.98f, false).toString(16).substring(2).uppercase()}</color>
    <color name="system_neutral1_1000">#${shiftColor(colorInt, 1.0f, false).toString(16).substring(2).uppercase()}</color>
    
    <!-- Component-specific Material You colors if enabled -->
    ${if (themedComponents["status_bar"] == true) """
    <color name="m3_sys_color_dynamic_system_status_bar">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="m3_sys_color_dynamic_system_status_bar_icons">#${if (isColorLight(colorInt)) "FF000000" else "FFFFFFFF"}</color>""" else ""}
    
    ${if (themedComponents["navigation_bar"] == true) """
    <color name="m3_sys_color_dynamic_system_nav_bar">#${colorInt.toString(16).substring(2).uppercase()}</color>
    <color name="m3_sys_color_dynamic_system_nav_bar_divider">#${shiftColor(colorInt, 0.3f).toString(16).substring(2).uppercase()}</color>""" else ""}
</resources>"""
        
        addToZip(zipOutputStream, "$VALUES_V31_DIR/colors.xml", materialYouXml)
    }
    
    /**
     * Generates overlay assets for advanced theming
     */
    private fun generateOverlayAssets(
        zipOutputStream: ZipOutputStream,
        packageName: String,
        themedComponents: Map<String, Boolean>
    ) {
        // Create overlay configuration
        val configContent = """<?xml version="1.0" encoding="utf-8"?>
<theming-config>
    <package>$packageName</package>
    <components>
        <component name="status_bar" enabled="${themedComponents["status_bar"] ?: false}" />
        <component name="navigation_bar" enabled="${themedComponents["navigation_bar"] ?: false}" />
        <component name="system_ui" enabled="${themedComponents["system_ui"] ?: false}" />
        <component name="settings" enabled="${themedComponents["settings"] ?: false}" />
        <component name="launcher" enabled="${themedComponents["launcher"] ?: false}" />
    </components>
    <features>
        <feature name="material_you_override" enabled="true" />
        <feature name="high_contrast_injection" enabled="true" />
        <feature name="dynamic_colors" enabled="true" />
    </features>
</theming-config>"""
        
        addToZip(zipOutputStream, "$OVERLAYS_DIR/config.xml", configContent)
    }
    
    /**
     * Adds content to the ZIP stream
     */
    private fun addToZip(zipOutputStream: ZipOutputStream, path: String, content: String) {
        val entry = ZipEntry(path)
        zipOutputStream.putNextEntry(entry)
        zipOutputStream.write(content.toByteArray())
        zipOutputStream.closeEntry()
    }
    
    /**
     * Shifts a color by a factor to make it lighter or darker
     * @param color Original color
     * @param factor Factor to shift (0.0 to 1.0)
     * @param lighter True to make lighter, false to make darker
     */
    private fun shiftColor(color: Int, factor: Float, lighter: Boolean = true): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        
        val delta = (255 * factor).toInt()
        
        val newRed = if (lighter) {
            minOf(255, red + delta)
        } else {
            maxOf(0, red - delta)
        }
        
        val newGreen = if (lighter) {
            minOf(255, green + delta)
        } else {
            maxOf(0, green - delta)
        }
        
        val newBlue = if (lighter) {
            minOf(255, blue + delta)
        } else {
            maxOf(0, blue - delta)
        }
        
        return Color.rgb(newRed, newGreen, newBlue)
    }
    
    /**
     * Checks if a color is light or dark
     */
    private fun isColorLight(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness < 0.5
    }
    
    /**
     * Generates a range of color tones based on the input color
     */
    private fun generateTones(baseColor: Int): Map<Int, Int> {
        val tones = mutableMapOf<Int, Int>()
        
        // Generate Material You style tones (50, 100, 200...900, 950)
        for (tone in listOf(50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950)) {
            // Simplified tone generation - in reality, Material You uses more sophisticated algorithms
            val factor = when {
                tone <= 100 -> tone / 100f
                tone <= 500 -> 1.0f
                else -> 1.0f - ((tone - 500) / 1000f)
            }
            
            val adjustedColor = if (tone < 500) {
                // Lighter tones
                shiftColor(baseColor, (1.0f - factor) * 0.8f, true)
            } else {
                // Darker tones
                shiftColor(baseColor, (tone - 500) / 1000f, false)
            }
            
            tones[tone] = adjustedColor
        }
        
        return tones
    }
}