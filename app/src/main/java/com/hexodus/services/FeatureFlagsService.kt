package com.hexodus.services
import com.hexodus.HexodusApplication

import android.app.Service

import android.content.Intent
import android.util.Log
import com.hexodus.utils.SecurityUtils
import com.hexodus.utils.PrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * FeatureFlagsService - Manages discovering and toggling system/Samsung feature flags
 * using Shizuku. Enables workarounds for specific devices (e.g., S22 Ultra).
 */
object FeatureFlagsService {
    
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
    private val prefsManager by lazy { PrefsManager.getInstance(com.hexodus.HexodusApplication.context) }

    private const val TAG = "FeatureFlagsService"
    
    const val ACTION_TOGGLE_FLAG = "com.hexodus.TOGGLE_FEATURE_FLAG"
    const val ACTION_RESTORE_DEFAULTS = "com.hexodus.RESTORE_DEFAULTS"

    const val EXTRA_FLAG_NAME = "flag_name"
    const val EXTRA_FLAG_STATE = "flag_state"

    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_TOGGLE_FLAG -> {
                val flagName = intent.getStringExtra(EXTRA_FLAG_NAME)
                val state = intent.getBooleanExtra(EXTRA_FLAG_STATE, false)
                if (!flagName.isNullOrEmpty()) {
                    toggleFlag(flagName, state)
                }
            }
            ACTION_RESTORE_DEFAULTS -> {
                scope.launch { restoreSystemDefaults() }
            }
        }
        return android.app.Service.START_STICKY
    }

    // Maps UI feature keys → system/Samsung flag names
    private val featureFlags = mapOf(
        "circle_to_search"        to "sem_circle_to_search",
        "vertical_drawer"         to "sem_vertical_app_drawer",
        "now_brief"               to "sem_now_brief_enabled",
        "battery_stats"           to "sem_advanced_battery_reports",
        "enhanced_processing"     to "sem_enhanced_cpu_responsiveness",
        "notification_cooldown"   to "notification_cooldown_enabled",
        "desktop_windowing"       to "enable_freeform_support",
        "screen_off_fod"          to "fingerprint_always_on_enabled",
        "vertical_qs"             to "sem_vertical_qs_panel",
        "priority_notifs"         to "sem_priority_ai_notifications",
        "glassmorphism"           to "sem_glassmorphism_icons",
        "now_bar"                 to "sem_now_bar_enabled",
        "notification_redaction"  to "sem_sensitive_notif_redaction"
    )

    fun toggleFeature(featureKey: String, enabled: Boolean) {
        val flagName = featureFlags[featureKey] ?: run {
            Log.w(TAG, "Unknown feature key: $featureKey")
            return
        }
        toggleFlag(flagName, enabled)
    }

    private fun toggleFlag(name: String, enabled: Boolean) {
        if (!ShizukuBridge.isReady()) return
        
        scope.launch {
            try {
                val state = if (enabled) "1" else "0"
                ShizukuBridge.Settings.putGlobal(name, state)
                HexodusApplication.context.sendBroadcast(Intent("FEATURE_FLAG_CHANGED").putExtra("name", name).putExtra("state", enabled))
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling flag $name", e)
            }
        }
    }

    suspend fun restoreSystemDefaults() = withContext(Dispatchers.IO) {
        if (!ShizukuBridge.isReady()) return@withContext
        try {
            Log.d(TAG, "Restoring system defaults...")
            val flags = listOf("sem_enhanced_cpu_responsiveness", "notification_cooldown_enabled")
            for (flag in flags) {
                ShizukuBridge.Settings.putGlobal(flag, "0")
            }
            HexodusApplication.context.sendBroadcast(Intent("SYSTEM_DEFAULTS_RESTORED").putExtra("success", true))
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring defaults", e)
        }
    }
}

