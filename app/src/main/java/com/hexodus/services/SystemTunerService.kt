package com.hexodus.services
import com.hexodus.HexodusApplication

import android.app.Service

import android.content.Intent
import android.util.Log
import com.hexodus.core.ThemeCompiler
import com.hexodus.utils.SecurityUtils
import com.hexodus.utils.PrefsManager
import moe.shizuku.plus.ShizukuPlusAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * SystemTunerService - Service for accessing and modifying hidden system settings
 * Inspired by System UI Tuner project from awesome-shizuku
 */
object SystemTunerService {
    private val context: android.content.Context get() = com.hexodus.HexodusApplication.context
    private val packageName: String get() = context.packageName
    private val cacheDir: java.io.File get() = context.cacheDir
    private val filesDir: java.io.File get() = context.filesDir
    private val contentResolver: android.content.ContentResolver get() = context.contentResolver
    private val packageManager: android.content.pm.PackageManager get() = context.packageManager
    private val applicationContext: android.content.Context get() = context

    

    
    private const val TAG = "SystemTunerService"
    private const val ACTION_MODIFY_SETTING = "com.hexodus.MODIFY_SETTING"
    private const val ACTION_GET_SETTING = "com.hexodus.GET_SETTING"
    private const val ACTION_TOGGLE_IMMERSIVE = "com.hexodus.TOGGLE_IMMERSIVE"
    
    // Intent extras
    const val EXTRA_SETTING_KEY = "setting_key"
    const val EXTRA_SETTING_VALUE = "setting_value"
    const val EXTRA_SETTING_TYPE = "setting_type"
    
    private val prefsManager by lazy { PrefsManager.getInstance(HexodusApplication.context) }
    private val scope = CoroutineScope(Dispatchers.IO)

    private fun useEnhancedApi(): Boolean {
        return prefsManager.preferShizukuPlus && ShizukuPlusAPI.isEnhancedApiSupported()
    }
    
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_MODIFY_SETTING -> {
                val key = intent.getStringExtra(EXTRA_SETTING_KEY)
                val value = intent.getStringExtra(EXTRA_SETTING_VALUE)
                val type = intent.getStringExtra(EXTRA_SETTING_TYPE) ?: "STRING"
                
                if (!key.isNullOrEmpty() && value != null) {
                    modifySystemSetting(key, value, type)
                }
            }
            ACTION_GET_SETTING -> {
                val key = intent.getStringExtra(EXTRA_SETTING_KEY)
                if (!key.isNullOrEmpty()) {
                    getSystemSetting(key)
                }
            }
            ACTION_TOGGLE_IMMERSIVE -> {
                toggleImmersiveMode()
            }
        }
        
        return android.app.Service.START_STICKY
    }
    
    /**
     * Modifies a system setting using Shizuku
     */
    private fun modifySystemSetting(key: String, value: String, type: String) {
        scope.launch {
            try {
                if (!ShizukuBridge.isReady()) {
                    Log.e(TAG, "Shizuku is not ready")
                    return@launch
                }
                
                // Validate inputs
                if (SecurityUtils.containsDangerousChars(key) || SecurityUtils.containsDangerousChars(value)) {
                    Log.e(TAG, "Dangerous characters detected in input")
                    return@launch
                }
                
                if (useEnhancedApi()) {
                    ShizukuPlusAPI.Settings.putSystem(key, value)
                } else {
                    val command = when (type.uppercase()) {
                        "STRING" -> "settings put system $key \"$value\""
                        "INTEGER" -> "settings put system $key $value"
                        "FLOAT" -> "settings put system $key $value"
                        "LONG" -> "settings put system $key $value"
                        else -> "settings put system $key \"$value\""
                    }
                    ShizukuBridge.executeShellCommand(command)
                }

                syncSystemState()
                Log.d(TAG, "System setting modified: $key = $value")
                
                // Broadcast success
                val successIntent = Intent("SYSTEM_SETTING_MODIFIED")
                successIntent.putExtra("setting_key", key)
                successIntent.putExtra("setting_value", value)
                context.sendBroadcast(successIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error modifying system setting: ${e.message}", e)
                
                // Broadcast error
                val errorIntent = Intent("SYSTEM_SETTING_ERROR")
                errorIntent.putExtra("error_message", e.message)
                context.sendBroadcast(errorIntent)
            }
        }
    }
    
    /**
     * Gets a system setting value using Shizuku
     */
    private fun getSystemSetting(key: String) {
        scope.launch {
            try {
                if (!ShizukuBridge.isReady()) {
                    Log.e(TAG, "Shizuku is not ready")
                    return@launch
                }
                
                // Validate input
                if (SecurityUtils.containsDangerousChars(key)) {
                    Log.e(TAG, "Dangerous characters detected in input")
                    return@launch
                }
                
                val result = if (useEnhancedApi()) {
                    ShizukuPlusAPI.Settings.getSystem(key)
                } else {
                    val command = "settings get system $key"
                    ShizukuBridge.executeShellCommand(command)
                }
                
                if (result != null) {
                    Log.d(TAG, "System setting retrieved: $key = $result")
                    
                    // Broadcast success
                    val successIntent = Intent("SYSTEM_SETTING_RETRIEVED")
                    successIntent.putExtra("setting_key", key)
                    successIntent.putExtra("setting_value", result.trim())
                    context.sendBroadcast(successIntent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting system setting: ${e.message}", e)
            }
        }
    }
    
    /**
     * Toggles immersive mode using Shizuku
     */
    private fun toggleImmersiveMode() {
        scope.launch {
            try {
                if (!ShizukuBridge.isReady()) {
                    Log.e(TAG, "Shizuku is not ready")
                    return@launch
                }
                
                // Get current immersive mode setting
                val currentValue = if (useEnhancedApi()) {
                    ShizukuPlusAPI.Settings.getSystem("immersive_mode")
                } else {
                    ShizukuBridge.executeShellCommand("settings get system immersive_mode")
                }
                
                // Toggle immersive mode
                val newValue = if (currentValue?.contains("true") == true) "false" else "true"
                
                if (useEnhancedApi()) {
                    ShizukuPlusAPI.Settings.putSystem("immersive_mode", newValue)
                } else {
                    ShizukuBridge.executeShellCommand("settings put system immersive_mode $newValue")
                }
                
                syncSystemState()
                Log.d(TAG, "Immersive mode toggled: $newValue")
                
                // Broadcast success
                val successIntent = Intent("IMMERSIVE_MODE_TOGGLED")
                successIntent.putExtra("immersive_mode", newValue)
                context.sendBroadcast(successIntent)
                
                // Refresh system UI to apply changes
                 // Use OverlayManager instead
                overlayService.refreshSystemUI()
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling immersive mode: ${e.message}", e)
            }
        }
    }

    private fun syncSystemState() {
        if (useEnhancedApi()) {
            ShizukuPlusAPI.Shell.executeCommand("am broadcast -a android.intent.action.CONFIGURATION_CHANGED")
        } else {
            ShizukuBridge.executeShellCommand("am broadcast -a android.intent.action.CONFIGURATION_CHANGED")
        }
    }
}