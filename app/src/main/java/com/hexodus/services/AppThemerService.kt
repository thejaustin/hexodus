package com.hexodus.services
import com.hexodus.HexodusApplication

import android.app.Service

import android.content.Intent
import android.util.Log
import com.hexodus.utils.SecurityUtils

/**
 * AppThemerService - Service for per-app theming features
 * Inspired by DarQ project from awesome-shizuku for per-app dark mode
 */
object AppThemerService {
    private val appContext get() = com.hexodus.HexodusApplication.context

    
    
    
    
    
    
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

    
    
    
    
    
    
    
    

    
    
    
    
    
    
    

    

    
    private const val TAG = "AppThemerService"
    private const val ACTION_FORCE_DARK_MODE = "com.hexodus.FORCE_DARK_MODE"
    private const val ACTION_SET_APP_THEME = "com.hexodus.SET_APP_THEME"
    private const val ACTION_GET_APP_THEME = "com.hexodus.GET_APP_THEME"
    
    // Intent extras
    const val EXTRA_PACKAGE_NAME = "package_name"
    const val EXTRA_FORCE_DARK = "force_dark"
    const val EXTRA_THEME_CONFIG = "theme_config"
    
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_FORCE_DARK_MODE -> {
                val targetPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val forceDark = intent.getBooleanExtra(EXTRA_FORCE_DARK, false)
                
                if (!targetPackageName.isNullOrEmpty()) {
                    setAppDarkMode(targetPackageName, forceDark)
                }
            }
            ACTION_SET_APP_THEME -> {
                val targetPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val themeConfig = intent.getStringExtra(EXTRA_THEME_CONFIG)
                
                if (!targetPackageName.isNullOrEmpty() && !themeConfig.isNullOrEmpty()) {
                    setAppTheme(targetPackageName, themeConfig)
                }
            }
            ACTION_GET_APP_THEME -> {
                val targetPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                
                if (!targetPackageName.isNullOrEmpty()) {
                    getAppTheme(targetPackageName)
                }
            }
        }
        
        return android.app.Service.START_STICKY
    }
    
    /**
     * Sets force dark mode for a specific app using Shizuku
     */
    private fun setAppDarkMode(targetPackageName: String, forceDark: Boolean) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            // Use Android's force dark API via shell command
            val command = if (forceDark) {
                "cmd overlay enable --package $sanitizedPackageName --category android.theme.customization.force_dark"
            } else {
                "cmd overlay disable --package $sanitizedPackageName --category android.theme.customization.force_dark"
            }
            
            val result = ShizukuBridge.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Force dark mode ${if(forceDark) "enabled" else "disabled"} for: $sanitizedPackageName")
                
                // Broadcast success
                val successIntent = Intent("APP_FORCE_DARK_SET")
                successIntent.putExtra("package_name", sanitizedPackageName)
                successIntent.putExtra("force_dark", forceDark)
                appContext.sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to set force dark mode for: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("APP_FORCE_DARK_SET_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to execute command")
                appContext.sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting app dark mode: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_FORCE_DARK_ERROR")
            errorIntent.putExtra("error_message", e.message)
            appContext.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Sets a custom theme for a specific app using Shizuku
     */
    private fun setAppTheme(targetPackageName: String, themeConfig: String) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            // In a real implementation, appContext would create and apply an app-specific overlay
            // For appContext example, we'll just log the action
            Log.d(TAG, "Setting custom theme for: $sanitizedPackageName with config: $themeConfig")
            
            // Broadcast success
            val successIntent = Intent("APP_THEME_SET")
            successIntent.putExtra("package_name", sanitizedPackageName)
            successIntent.putExtra("theme_config", themeConfig)
            appContext.sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting app theme: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_THEME_ERROR")
            errorIntent.putExtra("error_message", e.message)
            appContext.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets the current theme for a specific app
     */
    private fun getAppTheme(targetPackageName: String) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            // In a real implementation, appContext would query the system for the app's current theme
            // For appContext example, we'll just return a default value
            val currentTheme = "default"
            
            Log.d(TAG, "Retrieved theme for: $sanitizedPackageName - $currentTheme")
            
            // Broadcast success
            val successIntent = Intent("APP_THEME_RETRIEVED")
            successIntent.putExtra("package_name", sanitizedPackageName)
            successIntent.putExtra("theme_config", currentTheme)
            appContext.sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app theme: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_THEME_ERROR")
            errorIntent.putExtra("error_message", e.message)
            appContext.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets a list of apps with forced dark mode enabled
     */
    fun getAppsForcedDark(): List<String> {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return emptyList()
            }
            
            // Query system for apps with force dark enabled
            val command = "cmd overlay list | grep force_dark"
            val result = ShizukuBridge.executeShellCommand(command)
            
            if (!result.isNullOrEmpty()) {
                return result.lines()
                    .filter { it.contains("ENABLED") }
                    .map { line -> 
                        line.substringAfterLast(":").trim() 
                    }
                    .filter { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting apps with forced dark: ${e.message}", e)
        }
        
        return emptyList()
    }
}