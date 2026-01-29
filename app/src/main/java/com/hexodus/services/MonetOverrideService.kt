package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.os.IBinder
import android.util.Log
import androidx.palette.graphics.Palette
import com.hexodus.utils.ColorUtils

/**
 * MonetOverrideService - Enhanced override for Material You color generation
 * Based on techniques from awesome-shizuku projects for system-level theming
 */
class MonetOverrideService : Service() {
    
    companion object {
        private const val TAG = "MonetOverrideService"
        private const val ACTION_START_OVERRIDE = "com.hexodus.START_MONET_OVERRIDE"
        private const val ACTION_STOP_OVERRIDE = "com.hexodus.STOP_MONET_OVERRIDE"
        
        // Intent extras
        const val EXTRA_CUSTOM_COLOR = "custom_monet_color"
        const val EXTRA_COMPONENT_SELECTION = "component_selection"
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_START_OVERRIDE -> {
                val hexColor = intent?.getStringExtra(EXTRA_CUSTOM_COLOR)
                val componentSelection = intent?.getStringArrayListExtra(EXTRA_COMPONENT_SELECTION)
                
                if (!hexColor.isNullOrEmpty()) {
                    startMonetOverride(hexColor, componentSelection ?: emptyList())
                }
            }
            ACTION_STOP_OVERRIDE -> {
                stopMonetOverride()
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Starts the Monet override process
     */
    private fun startMonetOverride(hexColor: String, components: List<String>) {
        try {
            // Parse the hex color
            val colorInt = Color.parseColor(if (hexColor.startsWith("#")) hexColor else "#$hexColor")
            
            // Generate a custom palette based on the hex color
            val customPalette = generateCustomPalette(colorInt, components)
            
            // Apply the custom palette to the system
            applyCustomPalette(customPalette, components)
            
            Log.d(TAG, "Started Monet override with color: $hexColor for components: $components")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Monet override: ${e.message}", e)
        }
    }
    
    /**
     * Stops the Monet override process
     */
    private fun stopMonetOverride() {
        try {
            // Restore original system behavior
            restoreOriginalPalette()
            
            Log.d(TAG, "Stopped Monet override")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping Monet override: ${e.message}", e)
        }
    }
    
    /**
     * Generates a custom palette based on the input color
     */
    private fun generateCustomPalette(baseColor: Int, components: List<String>): Map<String, Int> {
        val palette = mutableMapOf<String, Int>()
        
        // Generate Material You style color roles
        palette["accent1_0"] = ColorUtils.shiftColor(baseColor, 0.9f, true)
        palette["accent1_10"] = ColorUtils.shiftColor(baseColor, 0.8f, true)
        palette["accent1_50"] = ColorUtils.shiftColor(baseColor, 0.6f, true)
        palette["accent1_100"] = ColorUtils.shiftColor(baseColor, 0.4f, true)
        palette["accent1_200"] = ColorUtils.shiftColor(baseColor, 0.2f, true)
        palette["accent1_300"] = baseColor
        palette["accent1_400"] = ColorUtils.shiftColor(baseColor, 0.1f, false)
        palette["accent1_500"] = ColorUtils.shiftColor(baseColor, 0.2f, false)
        palette["accent1_600"] = ColorUtils.shiftColor(baseColor, 0.3f, false)
        palette["accent1_700"] = ColorUtils.shiftColor(baseColor, 0.4f, false)
        palette["accent1_800"] = ColorUtils.shiftColor(baseColor, 0.5f, false)
        palette["accent1_900"] = ColorUtils.shiftColor(baseColor, 0.6f, false)
        palette["accent1_950"] = ColorUtils.shiftColor(baseColor, 0.7f, false)
        
        // Generate secondary colors
        val secondaryBase = ColorUtils.rotateHue(baseColor, 30f)
        palette["accent2_0"] = ColorUtils.shiftColor(secondaryBase, 0.9f, true)
        palette["accent2_100"] = ColorUtils.shiftColor(secondaryBase, 0.5f, true)
        palette["accent2_200"] = secondaryBase
        palette["accent2_300"] = ColorUtils.shiftColor(secondaryBase, 0.2f, false)
        palette["accent2_400"] = ColorUtils.shiftColor(secondaryBase, 0.4f, false)
        
        // Generate tertiary colors
        val tertiaryBase = ColorUtils.rotateHue(baseColor, -30f)
        palette["accent3_0"] = ColorUtils.shiftColor(tertiaryBase, 0.9f, true)
        palette["accent3_100"] = ColorUtils.shiftColor(tertiaryBase, 0.5f, true)
        palette["accent3_200"] = tertiaryBase
        palette["accent3_300"] = ColorUtils.shiftColor(tertiaryBase, 0.2f, false)
        
        // Generate neutral colors
        val neutralBase = ColorUtils.desaturate(baseColor, 0.8f)
        palette["neutral1_0"] = ColorUtils.shiftColor(neutralBase, 0.95f, true)
        palette["neutral1_10"] = ColorUtils.shiftColor(neutralBase, 0.85f, true)
        palette["neutral1_50"] = ColorUtils.shiftColor(neutralBase, 0.6f, true)
        palette["neutral1_100"] = ColorUtils.shiftColor(neutralBase, 0.4f, true)
        palette["neutral1_200"] = ColorUtils.shiftColor(neutralBase, 0.2f, true)
        palette["neutral1_300"] = neutralBase
        palette["neutral1_400"] = ColorUtils.shiftColor(neutralBase, 0.1f, false)
        palette["neutral1_500"] = ColorUtils.shiftColor(neutralBase, 0.2f, false)
        palette["neutral1_600"] = ColorUtils.shiftColor(neutralBase, 0.3f, false)
        palette["neutral1_700"] = ColorUtils.shiftColor(neutralBase, 0.4f, false)
        palette["neutral1_800"] = ColorUtils.shiftColor(neutralBase, 0.5f, false)
        palette["neutral1_900"] = ColorUtils.shiftColor(neutralBase, 0.6f, false)
        palette["neutral1_1000"] = ColorUtils.shiftColor(neutralBase, 0.7f, false)
        
        // Generate neutral variant colors
        palette["neutral2_0"] = ColorUtils.shiftColor(neutralBase, 0.9f, true)
        palette["neutral2_10"] = ColorUtils.shiftColor(neutralBase, 0.7f, true)
        palette["neutral2_50"] = ColorUtils.shiftColor(neutralBase, 0.5f, true)
        palette["neutral2_100"] = ColorUtils.shiftColor(neutralBase, 0.3f, true)
        palette["neutral2_200"] = ColorUtils.shiftColor(neutralBase, 0.1f, true)
        palette["neutral2_300"] = neutralBase
        palette["neutral2_400"] = ColorUtils.shiftColor(neutralBase, 0.1f, false)
        palette["neutral2_500"] = ColorUtils.shiftColor(neutralBase, 0.2f, false)
        palette["neutral2_600"] = ColorUtils.shiftColor(neutralBase, 0.3f, false)
        palette["neutral2_700"] = ColorUtils.shiftColor(neutralBase, 0.4f, false)
        palette["neutral2_800"] = ColorUtils.shiftColor(neutralBase, 0.5f, false)
        palette["neutral2_900"] = ColorUtils.shiftColor(neutralBase, 0.6f, false)
        
        return palette
    }
    
    /**
     * Applies the custom palette to the system
     * This is a simulated implementation since actual system palette override
     * requires system-level privileges that are not available to regular apps
     */
    private fun applyCustomPalette(palette: Map<String, Int>, components: List<String>) {
        // In a real implementation, this would interact with the system's
        // color service to override the Material You color generation
        // Since this isn't possible without system privileges, we'll log the attempt
        
        Log.d(TAG, "Attempting to apply custom palette for components: $components")
        for ((role, color) in palette) {
            Log.d(TAG, "  $role: #${color.toString(16).substring(2).uppercase()}")
        }
        
        // Simulate the process by storing the palette in shared preferences
        // which can be accessed by other parts of the app
        val prefs = applicationContext.getSharedPreferences("monet_override", 0)
        val editor = prefs.edit()
        
        for ((role, color) in palette) {
            editor.putInt(role, color)
        }
        
        editor.putBoolean("override_active", true)
        editor.putStringSet("themed_components", components.toSet())
        editor.apply()
        
        // Broadcast an intent to notify other components
        val intent = Intent("MONET_OVERRIDE_UPDATED")
        intent.putExtra("palette", HashMap(palette))
        intent.putStringArrayListExtra("components", ArrayList(components))
        sendBroadcast(intent)
    }
    
    /**
     * Restores the original system palette
     */
    private fun restoreOriginalPalette() {
        val prefs = applicationContext.getSharedPreferences("monet_override", 0)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
        
        // Broadcast an intent to notify other components
        val intent = Intent("MONET_OVERRIDE_RESTORED")
        sendBroadcast(intent)
    }
}