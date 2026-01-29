package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.utils.SecurityUtils
import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import java.util.Timer
import java.util.TimerTask

/**
 * AutomationService - Service for system-level automation features
 * Inspired by automation projects from awesome-shizuku like MacroDroid, Tasker, and AutoJs6
 */
class AutomationService : Service() {
    
    companion object {
        private const val TAG = "AutomationService"
        private const val ACTION_CREATE_AUTOMATION = "com.hexodus.CREATE_AUTOMATION"
        private const val ACTION_EXECUTE_AUTOMATION = "com.hexodus.EXECUTE_AUTOMATION"
        private const val ACTION_GET_AUTOMATIONS = "com.hexodus.GET_AUTOMATIONS"
        private const val ACTION_DELETE_AUTOMATION = "com.hexodus.DELETE_AUTOMATION"
        private const val ACTION_ENABLE_AUTOMATION = "com.hexodus.ENABLE_AUTOMATION"
        private const val ACTION_DISABLE_AUTOMATION = "com.hexodus.DISABLE_AUTOMATION"
        private const val ACTION_SCHEDULE_AUTOMATION = "com.hexodus.SCHEDULE_AUTOMATION"
        
        // Intent extras
        const val EXTRA_AUTOMATION_NAME = "automation_name"
        const val EXTRA_AUTOMATION_TRIGGER = "automation_trigger" // app_launch, time, location, battery, etc.
        const val EXTRA_AUTOMATION_ACTIONS = "automation_actions" // list of actions to perform
        const val EXTRA_AUTOMATION_CONDITIONS = "automation_conditions" // conditions for execution
        const val EXTRA_AUTOMATION_ENABLED = "automation_enabled"
        const val EXTRA_SCHEDULE_TIME = "schedule_time"
        const val EXTRA_REPEAT_INTERVAL = "repeat_interval"
    }
    
    private lateinit var shizukuBridgeService: ShizukuBridgeService
    private val automationExecutor = Executors.newSingleThreadExecutor()
    private val scheduledTasks = mutableMapOf<String, TimerTask>()
    private val activeAutomations = mutableMapOf<String, AutomationConfig>()
    
    data class AutomationConfig(
        val name: String,
        val trigger: String,
        val actions: List<String>,
        val conditions: List<String>,
        val enabled: Boolean,
        val scheduleTime: Long? = null,
        val repeatInterval: Long? = null
    )
    
    override fun onCreate() {
        super.onCreate()
        shizukuBridgeService = ShizukuBridgeService()
        Log.d(TAG, "AutomationService created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_CREATE_AUTOMATION -> {
                val automationName = intent.getStringExtra(EXTRA_AUTOMATION_NAME)
                val trigger = intent.getStringExtra(EXTRA_AUTOMATION_TRIGGER)
                val actions = intent.getStringArrayListExtra(EXTRA_AUTOMATION_ACTIONS) ?: arrayListOf()
                val conditions = intent.getStringArrayListExtra(EXTRA_AUTOMATION_CONDITIONS) ?: arrayListOf()
                val enabled = intent.getBooleanExtra(EXTRA_AUTOMATION_ENABLED, true)
                
                if (!automationName.isNullOrEmpty() && !trigger.isNullOrEmpty()) {
                    createAutomation(automationName, trigger, actions, conditions, enabled)
                }
            }
            ACTION_EXECUTE_AUTOMATION -> {
                val automationName = intent.getStringExtra(EXTRA_AUTOMATION_NAME)
                
                if (!automationName.isNullOrEmpty()) {
                    executeAutomation(automationName)
                }
            }
            ACTION_GET_AUTOMATIONS -> {
                getAutomations()
            }
            ACTION_DELETE_AUTOMATION -> {
                val automationName = intent.getStringExtra(EXTRA_AUTOMATION_NAME)
                
                if (!automationName.isNullOrEmpty()) {
                    deleteAutomation(automationName)
                }
            }
            ACTION_ENABLE_AUTOMATION -> {
                val automationName = intent.getStringExtra(EXTRA_AUTOMATION_NAME)
                
                if (!automationName.isNullOrEmpty()) {
                    enableAutomation(automationName)
                }
            }
            ACTION_DISABLE_AUTOMATION -> {
                val automationName = intent.getStringExtra(EXTRA_AUTOMATION_NAME)
                
                if (!automationName.isNullOrEmpty()) {
                    disableAutomation(automationName)
                }
            }
            ACTION_SCHEDULE_AUTOMATION -> {
                val automationName = intent.getStringExtra(EXTRA_AUTOMATION_NAME)
                val scheduleTime = intent.getLongExtra(EXTRA_SCHEDULE_TIME, 0L)
                val repeatInterval = intent.getLongExtra(EXTRA_REPEAT_INTERVAL, 0L)
                
                if (!automationName.isNullOrEmpty() && scheduleTime > 0) {
                    scheduleAutomation(automationName, scheduleTime, repeatInterval)
                }
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Creates a new automation
     */
    private fun createAutomation(
        name: String,
        trigger: String,
        actions: List<String>,
        conditions: List<String>,
        enabled: Boolean
    ) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (SecurityUtils.containsDangerousChars(name) || SecurityUtils.containsDangerousChars(trigger)) {
                Log.e(TAG, "Dangerous characters detected in automation parameters")
                return
            }
            
            val validTriggers = listOf(
                "app_launch", "time", "location", "battery_level", 
                "charging_state", "wifi_connected", "bluetooth_connected",
                "screen_on", "screen_off", "headphones_connected"
            )
            
            if (trigger !in validTriggers) {
                Log.e(TAG, "Invalid trigger: $trigger")
                return
            }
            
            // Validate actions
            val validActions = listOf(
                "apply_theme", "launch_app", "send_broadcast", "execute_shell",
                "set_brightness", "set_volume", "toggle_airplane", "toggle_wifi",
                "toggle_bluetooth", "take_screenshot", "lock_screen"
            )
            
            val invalidActions = actions.filter { it !in validActions }
            if (invalidActions.isNotEmpty()) {
                Log.e(TAG, "Invalid actions: ${invalidActions.joinToString(", ")}")
                return
            }
            
            // Create automation configuration
            val automationConfig = AutomationConfig(
                name = name,
                trigger = trigger,
                actions = actions,
                conditions = conditions,
                enabled = enabled
            )
            
            // Store automation
            activeAutomations[name] = automationConfig
            
            // If enabled and has a schedule, schedule it
            if (enabled && automationConfig.scheduleTime != null) {
                scheduleAutomation(name, automationConfig.scheduleTime!!, automationConfig.repeatInterval)
            }
            
            Log.d(TAG, "Created automation: $name with trigger: $trigger")
            
            // Broadcast success
            val successIntent = Intent("AUTOMATION_CREATED")
            successIntent.putExtra("automation_name", name)
            successIntent.putExtra("trigger", trigger)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating automation: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("AUTOMATION_CREATION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Executes an automation using Shizuku
     */
    private fun executeAutomation(automationName: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            val automation = activeAutomations[automationName]
            if (automation == null) {
                Log.e(TAG, "Automation not found: $automationName")
                return
            }
            
            if (!automation.enabled) {
                Log.w(TAG, "Automation is disabled: $automationName")
                return
            }
            
            // Validate automation name
            if (SecurityUtils.containsDangerousChars(automationName)) {
                Log.e(TAG, "Dangerous characters detected in automation name")
                return
            }
            
            // Execute each action in the automation
            for (action in automation.actions) {
                executeAction(action, automation.conditions)
            }
            
            Log.d(TAG, "Executed automation: $automationName")
            
            // Broadcast success
            val successIntent = Intent("AUTOMATION_EXECUTED")
            successIntent.putExtra("automation_name", automationName)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing automation: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("AUTOMATION_EXECUTION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets all automations
     */
    private fun getAutomations() {
        try {
            val automationList = activeAutomations.values.map { automation ->
                mapOf(
                    "name" to automation.name,
                    "trigger" to automation.trigger,
                    "actions" to automation.actions,
                    "conditions" to automation.conditions,
                    "enabled" to automation.enabled,
                    "scheduled" to (automation.scheduleTime != null)
                )
            }
            
            Log.d(TAG, "Retrieved ${automationList.size} automations")
            
            // Broadcast results
            val successIntent = Intent("AUTOMATIONS_RETRIEVED")
            successIntent.putExtra("automation_count", automationList.size)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting automations: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("AUTOMATIONS_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Schedules an automation for execution
     */
    private fun scheduleAutomation(name: String, scheduleTime: Long, repeatInterval: Long?) {
        try {
            val automation = activeAutomations[name]
            if (automation == null) {
                Log.e(TAG, "Automation not found: $name")
                return
            }
            
            // Cancel existing schedule if any
            scheduledTasks[name]?.cancel()
            
            val timer = Timer()
            val task = object : TimerTask() {
                override fun run() {
                    // Execute the automation
                    executeAutomation(name)
                    
                    // If it's a repeating task, reschedule
                    if (repeatInterval != null && repeatInterval > 0) {
                        val newTask = object : TimerTask() {
                            override fun run() {
                                executeAutomation(name)
                            }
                        }
                        
                        scheduledTasks[name] = newTask
                        timer.schedule(newTask, repeatInterval, repeatInterval)
                    }
                }
            }
            
            // Schedule the task
            if (repeatInterval != null && repeatInterval > 0) {
                timer.scheduleAtFixedRate(task, scheduleTime - System.currentTimeMillis(), repeatInterval)
            } else {
                timer.schedule(task, scheduleTime - System.currentTimeMillis())
            }
            
            scheduledTasks[name] = task
            
            Log.d(TAG, "Scheduled automation: $name for execution at: $scheduleTime")
            
            // Broadcast success
            val successIntent = Intent("AUTOMATION_SCHEDULED")
            successIntent.putExtra("automation_name", name)
            successIntent.putExtra("schedule_time", scheduleTime)
            successIntent.putExtra("repeat_interval", repeatInterval ?: 0L)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling automation: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("AUTOMATION_SCHEDULING_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Executes a specific action
     */
    private fun executeAction(action: String, conditions: List<String>) {
        try {
            // Check conditions first
            if (!evaluateConditions(conditions)) {
                Log.d(TAG, "Conditions not met for action: $action")
                return
            }
            
            when (action) {
                "apply_theme" -> {
                    // Apply a theme using the theming engine
                    val themeName = conditions.find { it.startsWith("theme:") }?.substringAfter(":")
                    if (!themeName.isNullOrEmpty()) {
                        // In a real implementation, this would apply the theme
                        Log.d(TAG, "Applied theme: $themeName")
                    }
                }
                "launch_app" -> {
                    val packageName = conditions.find { it.startsWith("package:") }?.substringAfter(":")
                    if (!packageName.isNullOrEmpty()) {
                        launchApp(packageName)
                    }
                }
                "set_brightness" -> {
                    val brightnessValue = conditions.find { it.startsWith("brightness:") }?.substringAfter(":")?.toIntOrNull()
                    if (brightnessValue != null) {
                        setBrightness(brightnessValue)
                    }
                }
                "set_volume" -> {
                    val volumeType = conditions.find { it.startsWith("volume_type:") }?.substringAfter(":") ?: "media"
                    val volumeLevel = conditions.find { it.startsWith("volume_level:") }?.substringAfter(":")?.toIntOrNull()
                    
                    if (volumeLevel != null) {
                        setVolume(volumeType, volumeLevel)
                    }
                }
                "toggle_wifi" -> {
                    val enable = conditions.find { it.startsWith("enable:") }?.substringAfter(":")?.toBooleanStrictOrNull() ?: true
                    toggleWifi(enable)
                }
                "toggle_bluetooth" -> {
                    val enable = conditions.find { it.startsWith("enable:") }?.substringAfter(":")?.toBooleanStrictOrNull() ?: true
                    toggleBluetooth(enable)
                }
                "lock_screen" -> {
                    lockScreen()
                }
                "take_screenshot" -> {
                    takeScreenshot()
                }
                else -> {
                    Log.w(TAG, "Unknown action: $action")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing action: ${e.message}", e)
        }
    }
    
    /**
     * Evaluates automation conditions
     */
    private fun evaluateConditions(conditions: List<String>): Boolean {
        // In a real implementation, this would evaluate complex conditions
        // For this example, we'll just return true
        return true
    }
    
    /**
     * Launches an app using Shizuku
     */
    private fun launchApp(packageName: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            val command = "monkey -p $sanitizedPackageName -c android.intent.category.LAUNCHER 1"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Launched app: $sanitizedPackageName")
            } else {
                Log.e(TAG, "Failed to launch app: $sanitizedPackageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: ${e.message}", e)
        }
    }
    
    /**
     * Sets screen brightness using Shizuku
     */
    private fun setBrightness(level: Int) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate brightness level (0-255)
            val clampedLevel = level.coerceIn(0, 255)
            
            val command = "settings put system screen_brightness $clampedLevel"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Set brightness to: $clampedLevel")
            } else {
                Log.e(TAG, "Failed to set brightness to: $clampedLevel")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting brightness: ${e.message}", e)
        }
    }
    
    /**
     * Sets volume level using Shizuku
     */
    private fun setVolume(streamType: String, level: Int) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Map stream type to Android stream index
            val streamIndex = when (streamType.lowercase()) {
                "alarm" -> 4 // STREAM_ALARM
                "music" -> 3 // STREAM_MUSIC
                "ring" -> 2 // STREAM_RING
                "notification" -> 5 // STREAM_NOTIFICATION
                "system" -> 1 // STREAM_SYSTEM
                "voice_call" -> 0 // STREAM_VOICE_CALL
                else -> 3 // Default to music
            }
            
            // Validate volume level (0-15 for most streams)
            val clampedLevel = level.coerceIn(0, 15)
            
            val command = "media volume --show --stream $streamIndex --set $clampedLevel"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Set $streamType volume to: $clampedLevel")
            } else {
                Log.e(TAG, "Failed to set $streamType volume to: $clampedLevel")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume: ${e.message}", e)
        }
    }
    
    /**
     * Toggles WiFi using Shizuku
     */
    private fun toggleWifi(enable: Boolean) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            val command = if (enable) "svc wifi enable" else "svc wifi disable"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "WiFi ${if(enable) "enabled" else "disabled"}")
            } else {
                Log.e(TAG, "Failed to ${if(enable) "enable" else "disable"} WiFi")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling WiFi: ${e.message}", e)
        }
    }
    
    /**
     * Toggles Bluetooth using Shizuku
     */
    private fun toggleBluetooth(enable: Boolean) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            val command = if (enable) "svc bluetooth enable" else "svc bluetooth disable"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Bluetooth ${if(enable) "enabled" else "disabled"}")
            } else {
                Log.e(TAG, "Failed to ${if(enable) "enable" else "disable"} Bluetooth")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling Bluetooth: ${e.message}", e)
        }
    }
    
    /**
     * Locks the screen using Shizuku
     */
    private fun lockScreen() {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            val command = "input keyevent KEYCODE_POWER"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Screen locked")
            } else {
                Log.e(TAG, "Failed to lock screen")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error locking screen: ${e.message}", e)
        }
    }
    
    /**
     * Takes a screenshot using Shizuku
     */
    private fun takeScreenshot() {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            val timestamp = System.currentTimeMillis()
            val command = "screencap -p /sdcard/Pictures/Screenshots/screenshot_$timestamp.png"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Screenshot taken")
            } else {
                Log.e(TAG, "Failed to take screenshot")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error taking screenshot: ${e.message}", e)
        }
    }
    
    /**
     * Enables an automation
     */
    fun enableAutomation(name: String): Boolean {
        val automation = activeAutomations[name] ?: return false
        
        activeAutomations[name] = automation.copy(enabled = true)
        
        // If it has a schedule, schedule it
        if (automation.scheduleTime != null) {
            scheduleAutomation(name, automation.scheduleTime!!, automation.repeatInterval)
        }
        
        Log.d(TAG, "Enabled automation: $name")
        return true
    }
    
    /**
     * Disables an automation
     */
    fun disableAutomation(name: String): Boolean {
        val automation = activeAutomations[name] ?: return false
        
        // Cancel any scheduled tasks
        scheduledTasks[name]?.cancel()
        scheduledTasks.remove(name)
        
        activeAutomations[name] = automation.copy(enabled = false)
        
        Log.d(TAG, "Disabled automation: $name")
        return true
    }
    
    /**
     * Deletes an automation
     */
    fun deleteAutomation(name: String): Boolean {
        // Cancel any scheduled tasks
        scheduledTasks[name]?.cancel()
        scheduledTasks.remove(name)
        
        val removed = activeAutomations.remove(name) != null
        
        if (removed) {
            Log.d(TAG, "Deleted automation: $name")
        }
        
        return removed
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Cancel all scheduled tasks
        scheduledTasks.values.forEach { it.cancel() }
        scheduledTasks.clear()
        
        // Shutdown executor
        automationExecutor.shutdown()
        
        Log.d(TAG, "AutomationService destroyed")
    }
}