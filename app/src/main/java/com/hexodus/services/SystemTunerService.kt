package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.core.ThemeCompiler
import com.hexodus.utils.SecurityUtils

/**
 * SystemTunerService - Service for accessing and modifying hidden system settings
 * Inspired by System UI Tuner project from awesome-shizuku
 */
class SystemTunerService : Service() {
    
    companion object {
        private const val TAG = "SystemTunerService"
        private const val ACTION_MODIFY_SETTING = "com.hexodus.MODIFY_SETTING"
        private const val ACTION_GET_SETTING = "com.hexodus.GET_SETTING"
        private const val ACTION_TOGGLE_IMMERSIVE = "com.hexodus.TOGGLE_IMMERSIVE"
        
        // Intent extras
        const val EXTRA_SETTING_KEY = "setting_key"
        const val EXTRA_SETTING_VALUE = "setting_value"
        const val EXTRA_SETTING_TYPE = "setting_type"
    }
    
    private lateinit var shizukuBridgeService: ShizukuBridgeService
    
    override fun onCreate() {
        super.onCreate()
        shizukuBridgeService = ShizukuBridgeService()
        Log.d(TAG, "SystemTunerService created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
        
        return START_STICKY
    }
    
    /**
     * Modifies a system setting using Shizuku
     */
    private fun modifySystemSetting(key: String, value: String, type: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (SecurityUtils.containsDangerousChars(key) || SecurityUtils.containsDangerousChars(value)) {
                Log.e(TAG, "Dangerous characters detected in input")
                return
            }
            
            val command = when (type.uppercase()) {
                "STRING" -> "settings put system $key \"$value\""
                "INTEGER" -> "settings put system $key $value"
                "FLOAT" -> "settings put system $key $value"
                "LONG" -> "settings put system $key $value"
                else -> "settings put system $key \"$value\""
            }
            
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "System setting modified: $key = $value")
                
                // Broadcast success
                val successIntent = Intent("SYSTEM_SETTING_MODIFIED")
                successIntent.putExtra("setting_key", key)
                successIntent.putExtra("setting_value", value)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to modify system setting: $key")
                
                // Broadcast failure
                val failureIntent = Intent("SYSTEM_SETTING_MODIFICATION_FAILED")
                failureIntent.putExtra("setting_key", key)
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error modifying system setting: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("SYSTEM_SETTING_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets a system setting value using Shizuku
     */
    private fun getSystemSetting(key: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate input
            if (SecurityUtils.containsDangerousChars(key)) {
                Log.e(TAG, "Dangerous characters detected in input")
                return
            }
            
            val command = "settings get system $key"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "System setting retrieved: $key = $result")
                
                // Broadcast success
                val successIntent = Intent("SYSTEM_SETTING_RETRIEVED")
                successIntent.putExtra("setting_key", key)
                successIntent.putExtra("setting_value", result.trim())
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to get system setting: $key")
                
                // Broadcast failure
                val failureIntent = Intent("SYSTEM_SETTING_RETRIEVAL_FAILED")
                failureIntent.putExtra("setting_key", key)
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting system setting: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("SYSTEM_SETTING_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Toggles immersive mode using Shizuku
     */
    private fun toggleImmersiveMode() {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Get current immersive mode setting
            val getCommand = "settings get system immersive_mode"
            val currentValue = shizukuBridgeService.executeShellCommand(getCommand)
            
            // Toggle immersive mode
            val newValue = if (currentValue?.contains("true") == true) "false" else "true"
            val setCommand = "settings put system immersive_mode $newValue"
            val result = shizukuBridgeService.executeShellCommand(setCommand)
            
            if (result != null) {
                Log.d(TAG, "Immersive mode toggled: $newValue")
                
                // Broadcast success
                val successIntent = Intent("IMMERSIVE_MODE_TOGGLED")
                successIntent.putExtra("immersive_mode", newValue)
                sendBroadcast(successIntent)
                
                // Refresh system UI to apply changes
                val overlayService = OverlayActivationService()
                overlayService.refreshSystemUI()
            } else {
                Log.e(TAG, "Failed to toggle immersive mode")
                
                // Broadcast failure
                val failureIntent = Intent("IMMERSIVE_MODE_TOGGLE_FAILED")
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling immersive mode: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("IMMERSIVE_MODE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SystemTunerService destroyed")
    }
}