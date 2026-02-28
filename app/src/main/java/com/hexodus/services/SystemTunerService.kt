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
    private val appContext: android.content.Context get() = com.hexodus.HexodusApplication.context
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
    private val prefsManager by lazy { PrefsManager.getInstance(com.hexodus.HexodusApplication.context) }

    private const val TAG = "SystemTunerService"
    private const val ACTION_MODIFY_SETTING = "com.hexodus.MODIFY_SETTING"
    private const val ACTION_GET_SETTING = "com.hexodus.GET_SETTING"
    private const val ACTION_TOGGLE_IMMERSIVE = "com.hexodus.TOGGLE_IMMERSIVE"
    
    // Intent extras
    const val EXTRA_SETTING_KEY = "setting_key"
    const val EXTRA_SETTING_VALUE = "setting_value"
    const val EXTRA_SETTING_TYPE = "setting_type"
    
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
    
    private fun modifySystemSetting(key: String, value: String, type: String) {
        scope.launch {
            try {
                if (!ShizukuBridge.isReady()) return@launch
                if (useEnhancedApi()) {
                    ShizukuPlusAPI.Settings.putSystem(key, value)
                } else {
                    ShizukuBridge.executeShellCommand("settings put system $key \"$value\"")
                }
                Log.d(TAG, "System setting modified: $key = $value")
                val successIntent = Intent("SYSTEM_SETTING_MODIFIED")
                successIntent.putExtra("setting_key", key)
                appContext.sendBroadcast(successIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error modifying system setting: ${e.message}", e)
            }
        }
    }
    
    private fun getSystemSetting(key: String) {
        scope.launch {
            try {
                if (!ShizukuBridge.isReady()) return@launch
                val result = if (useEnhancedApi()) {
                    ShizukuPlusAPI.Settings.getSystem(key)
                } else {
                    ShizukuBridge.executeShellCommand("settings get system $key")
                }
                if (result != null) {
                    val successIntent = Intent("SYSTEM_SETTING_RETRIEVED")
                    successIntent.putExtra("setting_key", key)
                    successIntent.putExtra("setting_value", result.trim())
                    appContext.sendBroadcast(successIntent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting system setting", e)
            }
        }
    }
    
    private fun toggleImmersiveMode() {
        scope.launch {
            try {
                if (!ShizukuBridge.isReady()) return@launch
                val currentValue = if (useEnhancedApi()) {
                    ShizukuPlusAPI.Settings.getSystem("immersive_mode")
                } else {
                    ShizukuBridge.executeShellCommand("settings get system immersive_mode")
                }
                val newValue = if (currentValue?.contains("true") == true) "false" else "true"
                if (useEnhancedApi()) {
                    ShizukuPlusAPI.Settings.putSystem("immersive_mode", newValue)
                } else {
                    ShizukuBridge.executeShellCommand("settings put system immersive_mode $newValue")
                }
                OverlayManager.refreshSystemUI()
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling immersive mode", e)
            }
        }
    }
}
