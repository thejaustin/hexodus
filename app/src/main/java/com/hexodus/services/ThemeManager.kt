package com.hexodus.services

import android.content.Context
import android.content.Intent
import android.util.Log
import com.hexodus.core.ThemeCompiler
import com.hexodus.HexodusApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

/**
 * ThemeManager - Central singleton for managing all theme operations
 * Handles theme creation, application, sharing, and persistence
 */
object ThemeManager {
    
    private const val TAG = "ThemeManager"
    
    private val themeCompiler = ThemeCompiler()
    
    /**
     * Creates a theme from hex color and component preferences
     */
    suspend fun createTheme(hexColor: String, themeName: String, themedComponents: Map<String, Boolean>) = withContext(Dispatchers.IO) {
        val context = HexodusApplication.context
        try {
            // Compile the theme to an APK in memory
            val packageName = "com.hexodus.theme.${themeName.replace(" ", "_").lowercase()}.${System.currentTimeMillis()}"
            val themeData = themeCompiler.compileTheme(hexColor, packageName, themeName, themedComponents)
            
            // Save the theme to internal storage
            val themeFile = File(context.filesDir, "${themeName}_${System.currentTimeMillis()}.apk")
            FileOutputStream(themeFile).use { it.write(themeData) }
            
            Log.d(TAG, "Theme created: ${themeFile.absolutePath}")
            
            // We can still use broadcasts for UI if we want to keep it simple, or flow.
            // For now, let's just stick to the existing architecture's broadcast, or return a result.
            val completeIntent = Intent("THEME_CREATION_COMPLETE")
            completeIntent.putExtra("theme_path", themeFile.absolutePath)
            completeIntent.putExtra("theme_name", themeName)
            HexodusApplication.context.sendBroadcast(completeIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating theme: ${e.message}", e)
            
            val errorIntent = Intent("THEME_CREATION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            HexodusApplication.context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Applies a theme to the system using Shizuku
     */
    suspend fun applyTheme(themeFilePath: String) = withContext(Dispatchers.IO) {
        val context = HexodusApplication.context
        try {
            Log.d(TAG, "Applying theme from: $themeFilePath")
            
            // Replace Thread.sleep with Coroutines delay
            delay(1000) 
            
            // Simulate the process
            // 1. Install the APK using Shizuku
            // 2. Enable the overlay using Shizuku
            // 3. Refresh the system UI
            
            val completeIntent = Intent("THEME_APPLICATION_COMPLETE")
            completeIntent.putExtra("theme_path", themeFilePath)
            HexodusApplication.context.sendBroadcast(completeIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error applying theme: ${e.message}", e)
            
            val errorIntent = Intent("THEME_APPLICATION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            HexodusApplication.context.sendBroadcast(errorIntent)
        }
    }
}
