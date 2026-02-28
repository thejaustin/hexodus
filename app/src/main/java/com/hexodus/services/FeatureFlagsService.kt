package com.hexodus.services

import android.content.Intent
import android.util.Log
import com.hexodus.utils.SecurityUtils
import com.hexodus.utils.PrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.shizuku.plus.ShizukuPlusAPI

/**
 * FeatureFlagsService - Manages discovering and toggling system/Samsung feature flags
 * using Shizuku. Enables workarounds for specific devices (e.g., S22 Ultra).
 */
object FeatureFlagsService {

    companion object {
        private const val TAG = "FeatureFlagsService"
        
        const val ACTION_TOGGLE_FLAG = "com.hexodus.TOGGLE_FEATURE_FLAG"
        const val ACTION_ENABLE_NOW_BRIEF = "com.hexodus.ENABLE_NOW_BRIEF"
        const val ACTION_BYPASS_SCOPED_STORAGE = "com.hexodus.BYPASS_SCOPED_STORAGE"
        const val ACTION_GET_BATTERY_HEALTH = "com.hexodus.GET_BATTERY_HEALTH"
        const val ACTION_SET_PERFORMANCE_PROFILE = "com.hexodus.SET_PERFORMANCE_PROFILE"
        const val ACTION_TOGGLE_ENHANCED_PROCESSING = "com.hexodus.TOGGLE_ENHANCED_PROCESSING"
        const val ACTION_TOGGLE_VERTICAL_DRAWER = "com.hexodus.TOGGLE_VERTICAL_DRAWER"
        const val ACTION_TOGGLE_CIRCLE_TO_SEARCH = "com.hexodus.TOGGLE_CIRCLE_TO_SEARCH"
        const val ACTION_TOGGLE_NOW_BAR = "com.hexodus.TOGGLE_NOW_BAR"
        const val ACTION_TOGGLE_NOTIFICATION_COOLDOWN = "com.hexodus.TOGGLE_NOTIFICATION_COOLDOWN"
        const val ACTION_TOGGLE_DESKTOP_MODE = "com.hexodus.TOGGLE_DESKTOP_MODE"
        const val ACTION_TOGGLE_SCREEN_OFF_FOD = "com.hexodus.TOGGLE_SCREEN_OFF_FOD"
        const val ACTION_TOGGLE_SENSITIVE_REDACTION = "com.hexodus.TOGGLE_SENSITIVE_REDACTION"
        const val ACTION_TOGGLE_NEW_QS = "com.hexodus.TOGGLE_NEW_QS"
        const val ACTION_RESTORE_DEFAULTS = "com.hexodus.RESTORE_DEFAULTS"

        const val EXTRA_FLAG_NAME = "flag_name"
        const val EXTRA_FLAG_STATE = "flag_state"
    }

    private lateinit var prefsManager: PrefsManager
    private val scope = CoroutineScope(Dispatchers.IO)

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
            ACTION_ENABLE_NOW_BRIEF -> {
                enableNowBrief()
            }
            ACTION_BYPASS_SCOPED_STORAGE -> {
                val appPackage = intent.getStringExtra("target_package") ?: ""
                if (appPackage.isNotEmpty()) {
                    bypassScopedStorage(appPackage)
                }
            }
            ACTION_GET_BATTERY_HEALTH -> {
                getAdvancedBatteryHealth()
            }
            ACTION_SET_PERFORMANCE_PROFILE -> {
                val profile = intent.getStringExtra("profile") ?: "standard"
                setPerformanceProfile(profile)
            }
            ACTION_TOGGLE_ENHANCED_PROCESSING -> {
                val enabled = intent.getBooleanExtra("enabled", false)
                toggleGlobalSetting("sem_enhanced_cpu_responsiveness", enabled)
            }
            ACTION_TOGGLE_VERTICAL_DRAWER -> {
                val enabled = intent.getBooleanExtra("enabled", false)
                toggleVerticalDrawer(enabled)
            }
            ACTION_TOGGLE_CIRCLE_TO_SEARCH -> {
                val enabled = intent.getBooleanExtra("enabled", false)
                toggleSecureSetting("google_circle_to_search_enabled", enabled)
            }
            ACTION_TOGGLE_NOW_BAR -> {
                val enabled = intent.getBooleanExtra("enabled", false)
                toggleSystemSetting("now_bar_enabled", enabled)
            }
            ACTION_TOGGLE_NOTIFICATION_COOLDOWN -> {
                val enabled = intent.getBooleanExtra("enabled", false)
                toggleGlobalSetting("notification_cooldown_enabled", enabled)
            }
            ACTION_TOGGLE_DESKTOP_MODE -> {
                val enabled = intent.getBooleanExtra("enabled", false)
                toggleGlobalSetting("enable_freeform_support", enabled)
                toggleGlobalSetting("force_desktop_mode_on_external_displays", enabled)
            }
            ACTION_TOGGLE_SCREEN_OFF_FOD -> {
                val enabled = intent.getBooleanExtra("enabled", false)
                toggleSecureSetting("screen_off_fingerprint_unlock", enabled)
            }
            ACTION_TOGGLE_SENSITIVE_REDACTION -> {
                val enabled = intent.getBooleanExtra("enabled", false)
                toggleSecureSetting("sensitive_notifications_redaction", enabled)
            }
            ACTION_TOGGLE_NEW_QS -> {
                val enabled = intent.getBooleanExtra("enabled", false)
                toggleSystemSetting("status_bar_show_new_qs", enabled)
            }
            ACTION_RESTORE_DEFAULTS -> {
                restoreSystemDefaults()
            }
        }
        return START_STICKY
    }

    private fun useEnhancedApi(): Boolean {
        return prefsManager.preferShizukuPlus && ShizukuPlusAPI.isEnhancedApiSupported()
    }

    private fun toggleFlag(flagName: String, state: Boolean) {
        if (!ShizukuBridge.isReady()) return
        if (SecurityUtils.containsDangerousChars(flagName)) return

        scope.launch {
            val value = if (state) "1" else "0"
            if (useEnhancedApi()) {
                ShizukuPlusAPI.Settings.putGlobal(flagName, value)
            } else {
                ShizukuBridge.executeShellCommand("settings put global $flagName $value")
            }
            sendBroadcast(Intent("FEATURE_FLAG_TOGGLED").putExtra(EXTRA_FLAG_NAME, flagName).putExtra("success", true))
        }
    }

    private fun enableNowBrief() {
        if (!ShizukuBridge.isReady()) return
        scope.launch {
            val command = "am start -n com.samsung.android.smartsuggestions/.settings.about.developermode.DeveloperModeActivity"
            if (useEnhancedApi()) {
                ShizukuPlusAPI.Shell.executeCommand(command)
            } else {
                ShizukuBridge.executeShellCommand(command)
            }
            sendBroadcast(Intent("NOW_BRIEF_ENABLED").putExtra("success", true))
        }
    }

    private fun bypassScopedStorage(targetPackage: String) {
        if (!ShizukuBridge.isReady() || !SecurityUtils.isValidPackageName(targetPackage)) return
        scope.launch {
            val command = "appops set $targetPackage MANAGE_EXTERNAL_STORAGE allow"
            if (useEnhancedApi()) {
                ShizukuPlusAPI.Shell.executeCommand(command)
            } else {
                ShizukuBridge.executeShellCommand(command)
            }
            sendBroadcast(Intent("SCOPED_STORAGE_BYPASSED").putExtra("target_package", targetPackage).putExtra("success", true))
        }
    }

    private fun getAdvancedBatteryHealth() {
        if (!ShizukuBridge.isReady()) return
        scope.launch {
            val command = "dumpsys battery"
            val result = if (useEnhancedApi()) {
                ShizukuPlusAPI.Shell.executeCommand(command).output
            } else {
                ShizukuBridge.executeShellCommand(command) ?: ""
            }
            
            var cycleCount = -1
            var savedCapacity = -1
            result.lines().forEach { line ->
                if (line.contains("mSavedBatteryAsoc") || line.contains("battery_health")) {
                    val digits = line.replace(Regex("[^0-9]"), "")
                    if (digits.isNotEmpty()) savedCapacity = digits.toInt()
                }
                if (line.contains("mSavedBatteryUsage") || line.contains("cycle_count")) {
                    val digits = line.replace(Regex("[^0-9]"), "")
                    if (digits.isNotEmpty()) cycleCount = digits.toInt() / 100
                }
            }
            sendBroadcast(Intent("BATTERY_HEALTH_RETRIEVED").putExtra("cycle_count", cycleCount).putExtra("capacity_percent", savedCapacity))
        }
    }

    private fun toggleVerticalDrawer(enabled: Boolean) {
        if (!ShizukuBridge.isReady()) return
        scope.launch {
            val value = if (enabled) "1" else "0"
            if (useEnhancedApi()) {
                ShizukuPlusAPI.Settings.putSystem("home_up_vertical_app_drawer", value)
                ShizukuPlusAPI.Settings.putSystem("accelerator_app_list_sort_type", value)
                ShizukuPlusAPI.Shell.executeCommand("am force-stop com.sec.android.app.launcher")
            } else {
                ShizukuBridge.executeShellCommand("settings put system home_up_vertical_app_drawer $value")
                ShizukuBridge.executeShellCommand("settings put system accelerator_app_list_sort_type $value")
                ShizukuBridge.executeShellCommand("am force-stop com.sec.android.app.launcher")
            }
            sendBroadcast(Intent("VERTICAL_DRAWER_TOGGLED").putExtra("enabled", enabled))
        }
    }

    private fun toggleGlobalSetting(key: String, enabled: Boolean) {
        if (!ShizukuBridge.isReady()) return
        scope.launch {
            val value = if (enabled) "1" else "0"
            if (useEnhancedApi()) {
                ShizukuPlusAPI.Settings.putGlobal(key, value)
            } else {
                ShizukuBridge.executeShellCommand("settings put global $key $value")
            }
            syncSystemState()
            sendBroadcast(Intent("GLOBAL_SETTING_TOGGLED").putExtra(EXTRA_FLAG_NAME, key).putExtra("enabled", enabled))
        }
    }

    private fun toggleSecureSetting(key: String, enabled: Boolean) {
        if (!ShizukuBridge.isReady()) return
        scope.launch {
            val value = if (enabled) "1" else "0"
            if (useEnhancedApi()) {
                ShizukuPlusAPI.Settings.putSecure(key, value)
            } else {
                ShizukuBridge.executeShellCommand("settings put secure $key $value")
            }
            syncSystemState()
            sendBroadcast(Intent("SECURE_SETTING_TOGGLED").putExtra(EXTRA_FLAG_NAME, key).putExtra("enabled", enabled))
        }
    }

    private fun toggleSystemSetting(key: String, enabled: Boolean) {
        if (!ShizukuBridge.isReady()) return
        scope.launch {
            val value = if (enabled) "1" else "0"
            if (useEnhancedApi()) {
                ShizukuPlusAPI.Settings.putSystem(key, value)
            } else {
                ShizukuBridge.executeShellCommand("settings put system $key $value")
            }
            syncSystemState()
            sendBroadcast(Intent("SYSTEM_SETTING_TOGGLED").putExtra(EXTRA_FLAG_NAME, key).putExtra("enabled", enabled))
        }
    }

    /**
     * Prevents "Split-Brain" on Samsung devices where the underlying Android Setting
     * is changed but the One UI interface or proprietary database doesn't immediately reflect it.
     */
    private fun syncSystemState() {
        if (useEnhancedApi()) {
            // Broadcast standard config changes to force UI reconciliation
            ShizukuPlusAPI.Shell.executeCommand("am broadcast -a android.intent.action.CONFIGURATION_CHANGED")
            // Poke the Samsung Display service which often caches these values
            ShizukuPlusAPI.Shell.executeCommand("dumpsys display | grep -q 'state'") 
        } else {
            ShizukuBridge.executeShellCommand("am broadcast -a android.intent.action.CONFIGURATION_CHANGED")
        }
    }

    private fun setPerformanceProfile(profile: String) {
        if (!ShizukuBridge.isReady()) return
        scope.launch {
            val value = if (profile.lowercase() == "light") "1" else "0"
            if (useEnhancedApi()) {
                ShizukuPlusAPI.Settings.putGlobal("sem_low_power_mode_v2", value)
            } else {
                ShizukuBridge.executeShellCommand("settings put global sem_low_power_mode_v2 $value")
            }
            sendBroadcast(Intent("PERFORMANCE_PROFILE_SET").putExtra("profile", profile).putExtra("success", true))
        }
    }

    fun restoreSystemDefaults() {
        if (!ShizukuBridge.isReady()) return
        scope.launch {
            try {
                Log.d(TAG, "Restoring system defaults to recover from crash loops...")
                val globalKeys = listOf("sem_enhanced_cpu_responsiveness", "notification_cooldown_enabled", "enable_freeform_support", "force_desktop_mode_on_external_displays", "sem_low_power_mode_v2")
                val secureKeys = listOf("google_circle_to_search_enabled", "screen_off_fingerprint_unlock", "sensitive_notifications_redaction")
                val systemKeys = listOf("home_up_vertical_app_drawer", "accelerator_app_list_sort_type", "now_bar_enabled", "status_bar_show_new_qs")

                if (useEnhancedApi()) {
                    globalKeys.forEach { ShizukuPlusAPI.Shell.executeCommand("settings delete global $it") }
                    secureKeys.forEach { ShizukuPlusAPI.Shell.executeCommand("settings delete secure $it") }
                    systemKeys.forEach { ShizukuPlusAPI.Shell.executeCommand("settings delete system $it") }
                    
                    // Disable all Hexodus overlays to prevent RRO crash loops
                    val overlays = ShizukuPlusAPI.Shell.executeCommand("cmd overlay list | grep com.hexodus.theme")
                    overlays.output.lines().filter { it.contains(":") }.map { it.substringAfterLast(":").trim() }.forEach {
                        if (it.isNotEmpty()) ShizukuPlusAPI.OverlayManager.disableOverlay(it)
                    }

                    // Force restart UI processes
                    ShizukuPlusAPI.Shell.executeCommand("am force-stop com.sec.android.app.launcher")
                    ShizukuPlusAPI.Shell.executeCommand("killall com.android.systemui")
                } else {
                    globalKeys.forEach { ShizukuBridge.executeShellCommand("settings delete global $it") }
                    secureKeys.forEach { ShizukuBridge.executeShellCommand("settings delete secure $it") }
                    systemKeys.forEach { ShizukuBridge.executeShellCommand("settings delete system $it") }
                    
                    // Disable all Hexodus overlays (legacy method)
                    val output = ShizukuBridge.executeShellCommand("cmd overlay list | grep com.hexodus.theme") ?: ""
                    output.lines().filter { it.contains(":") }.map { it.substringAfterLast(":").trim() }.forEach {
                        if (it.isNotEmpty()) ShizukuBridge.executeShellCommand("cmd overlay disable $it")
                    }

                    // Force restart UI processes
                    ShizukuBridge.executeShellCommand("am force-stop com.sec.android.app.launcher")
                    ShizukuBridge.executeShellCommand("killall com.android.systemui")
                }

                sendBroadcast(Intent("SYSTEM_DEFAULTS_RESTORED").putExtra("success", true))
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring defaults", e)
            }
        }
    }
}
