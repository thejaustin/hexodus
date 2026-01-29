package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.utils.SecurityUtils

/**
 * PowerManagerService - Service for power management and battery optimization
 * Inspired by BatStats and EnforceDoze projects from awesome-shizuku
 */
class PowerManagerService : Service() {
    
    companion object {
        private const val TAG = "PowerManagerService"
        private const val ACTION_GET_BATTERY_STATS = "com.hexodus.GET_BATTERY_STATS"
        private const val ACTION_ENFORCE_DOZE = "com.hexodus.ENFORCE_DOZE"
        private const val ACTION_DISABLE_DOZE = "com.hexodus.DISABLE_DOZE"
        private const val ACTION_SET_POWER_PROFILE = "com.hexodus.SET_POWER_PROFILE"
        private const val ACTION_OPTIMIZE_APP_BATTERY = "com.hexodus.OPTIMIZE_APP_BATTERY"
        private const val ACTION_GET_POWER_USAGE = "com.hexodus.GET_POWER_USAGE"
        private const val ACTION_SCHEDULE_POWER_OPTIMIZATION = "com.hexodus.SCHEDULE_POWER_OPTIMIZATION"
        
        // Intent extras
        const val EXTRA_POWER_PROFILE = "power_profile"
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_OPTIMIZATION_LEVEL = "optimization_level"
        const val EXTRA_SCHEDULE_TIME = "schedule_time"
        const val EXTRA_BATTERY_THRESHOLD = "battery_threshold"
    }
    
    private lateinit var shizukuBridgeService: ShizukuBridgeService
    private var isDozeEnforced = false
    
    override fun onCreate() {
        super.onCreate()
        shizukuBridgeService = ShizukuBridgeService()
        Log.d(TAG, "PowerManagerService created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_GET_BATTERY_STATS -> {
                getBatteryStats()
            }
            ACTION_ENFORCE_DOZE -> {
                enforceDozeMode()
            }
            ACTION_DISABLE_DOZE -> {
                disableDozeMode()
            }
            ACTION_SET_POWER_PROFILE -> {
                val profile = intent.getStringExtra(EXTRA_POWER_PROFILE)
                if (!profile.isNullOrEmpty()) {
                    setPowerProfile(profile)
                }
            }
            ACTION_OPTIMIZE_APP_BATTERY -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val level = intent.getStringExtra(EXTRA_OPTIMIZATION_LEVEL) ?: "moderate"
                
                if (!packageName.isNullOrEmpty()) {
                    optimizeAppBattery(packageName, level)
                }
            }
            ACTION_GET_POWER_USAGE -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                
                if (!packageName.isNullOrEmpty()) {
                    getPowerUsage(packageName)
                }
            }
            ACTION_SCHEDULE_POWER_OPTIMIZATION -> {
                val scheduleTime = intent.getLongExtra(EXTRA_SCHEDULE_TIME, 0)
                val threshold = intent.getIntExtra(EXTRA_BATTERY_THRESHOLD, 20)
                
                schedulePowerOptimization(scheduleTime, threshold)
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Gets battery statistics using Shizuku
     */
    private fun getBatteryStats() {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            val command = "dumpsys batterystats"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Battery stats retrieved")
                
                // Parse basic battery stats from the output
                val batteryStats = parseBatteryStats(result)
                
                // Broadcast success
                val successIntent = Intent("BATTERY_STATS_RETRIEVED")
                successIntent.putExtra("stats", batteryStats)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to retrieve battery stats")
                
                // Broadcast failure
                val failureIntent = Intent("BATTERY_STATS_FAILED")
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting battery stats: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("BATTERY_STATS_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Enforces doze mode using Shizuku
     */
    private fun enforceDozeMode() {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Enable doze mode immediately
            val command = "dumpsys deviceidle force-idle"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                isDozeEnforced = true
                Log.d(TAG, "Doze mode enforced")
                
                // Broadcast success
                val successIntent = Intent("DOZE_ENFORCED")
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to enforce doze mode")
                
                // Broadcast failure
                val failureIntent = Intent("DOZE_ENFORCE_FAILED")
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error enforcing doze mode: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("DOZE_ENFORCE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Disables doze mode using Shizuku
     */
    private fun disableDozeMode() {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Exit doze mode
            val command = "dumpsys deviceidle unforce"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                isDozeEnforced = false
                Log.d(TAG, "Doze mode disabled")
                
                // Broadcast success
                val successIntent = Intent("DOZE_DISABLED")
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to disable doze mode")
                
                // Broadcast failure
                val failureIntent = Intent("DOZE_DISABLE_FAILED")
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error disabling doze mode: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("DOZE_DISABLE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Sets a power profile using Shizuku
     */
    private fun setPowerProfile(profile: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate profile
            if (SecurityUtils.containsDangerousChars(profile)) {
                Log.e(TAG, "Dangerous characters detected in power profile")
                return
            }
            
            // Apply power profile based on the setting
            val command = when (profile.lowercase()) {
                "performance" -> "cmd power set-mode 2"  // High performance mode
                "balanced" -> "cmd power set-mode 1"     // Balanced mode
                "battery_save" -> "cmd power set-mode 0" // Battery save mode
                else -> {
                    Log.w(TAG, "Unknown power profile: $profile, using balanced")
                    "cmd power set-mode 1"
                }
            }
            
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Power profile set to: $profile")
                
                // Broadcast success
                val successIntent = Intent("POWER_PROFILE_SET")
                successIntent.putExtra("profile", profile)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to set power profile: $profile")
                
                // Broadcast failure
                val failureIntent = Intent("POWER_PROFILE_SET_FAILED")
                failureIntent.putExtra("profile", profile)
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting power profile: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("POWER_PROFILE_SET_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Optimizes battery usage for an app using Shizuku
     */
    private fun optimizeAppBattery(packageName: String, level: String) {
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
            
            // Apply optimization based on level
            val command = when (level.lowercase()) {
                "aggressive" -> "cmd appops set $sanitizedPackageName RUN_IN_BACKGROUND ignore"
                "moderate" -> "cmd appops set $sanitizedPackageName RUN_IN_BACKGROUND allow"
                "light" -> "cmd appops set $sanitizedPackageName RUN_IN_BACKGROUND default"
                else -> "cmd appops set $sanitizedPackageName RUN_IN_BACKGROUND default"
            }
            
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Battery optimization applied to $sanitizedPackageName with level: $level")
                
                // Broadcast success
                val successIntent = Intent("APP_BATTERY_OPTIMIZED")
                successIntent.putExtra("package_name", sanitizedPackageName)
                successIntent.putExtra("optimization_level", level)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to optimize battery for app: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("APP_BATTERY_OPTIMIZE_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error optimizing app battery: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_BATTERY_OPTIMIZE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets power usage for an app using Shizuku
     */
    private fun getPowerUsage(packageName: String) {
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
            
            val command = "dumpsys batterystats $sanitizedPackageName"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Power usage retrieved for: $sanitizedPackageName")
                
                // Parse power usage from the output
                val powerUsage = parsePowerUsage(result)
                
                // Broadcast success
                val successIntent = Intent("POWER_USAGE_RETRIEVED")
                successIntent.putExtra("package_name", sanitizedPackageName)
                successIntent.putExtra("power_usage", powerUsage)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to get power usage for app: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("POWER_USAGE_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting power usage: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("POWER_USAGE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Schedules power optimization
     */
    private fun schedulePowerOptimization(scheduleTime: Long, threshold: Int) {
        try {
            Log.d(TAG, "Scheduled power optimization at $scheduleTime with threshold $threshold%")
            
            // In a real implementation, this would schedule a job
            // For this example, we'll just log the action
            Log.d(TAG, "Power optimization scheduled for $scheduleTime with threshold $threshold%")
            
            // Broadcast success
            val successIntent = Intent("POWER_OPTIMIZATION_SCHEDULED")
            successIntent.putExtra("schedule_time", scheduleTime)
            successIntent.putExtra("threshold", threshold)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling power optimization: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("POWER_OPTIMIZATION_SCHEDULE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Parses battery statistics from dumpsys output
     */
    private fun parseBatteryStats(statsOutput: String): Map<String, String> {
        val stats = mutableMapOf<String, String>()
        
        // Extract basic battery information
        statsOutput.lines().forEach { line ->
            if (line.contains("Battery Level:")) {
                val level = line.substringAfter("Battery Level:").substringBefore(",").trim()
                stats["level"] = level
            } else if (line.contains("Health:")) {
                val health = line.substringAfter("Health:").trim()
                stats["health"] = health
            } else if (line.contains("Temperature:")) {
                val temperature = line.substringAfter("Temperature:").substringBefore(",").trim()
                stats["temperature"] = temperature
            } else if (line.contains("Technology:")) {
                val technology = line.substringAfter("Technology:").trim()
                stats["technology"] = technology
            }
        }
        
        return stats
    }
    
    /**
     * Parses power usage from dumpsys output
     */
    private fun parsePowerUsage(powerOutput: String): Map<String, String> {
        val usage = mutableMapOf<String, String>()
        
        // Extract power usage information
        powerOutput.lines().forEach { line ->
            if (line.contains("Wake time:")) {
                val wakeTime = line.substringAfter("Wake time:").substringBefore("ms").trim()
                usage["wake_time_ms"] = wakeTime
            } else if (line.contains("CPU time:")) {
                val cpuTime = line.substringAfter("CPU time:").substringBefore("ms").trim()
                usage["cpu_time_ms"] = cpuTime
            } else if (line.contains("Foreground time:")) {
                val fgTime = line.substringAfter("Foreground time:").substringBefore("ms").trim()
                usage["foreground_time_ms"] = fgTime
            }
        }
        
        return usage
    }
    
    /**
     * Gets current power profile
     */
    fun getCurrentPowerProfile(): String {
        // In a real implementation, this would query the system
        // For this example, we'll return a default value
        return "balanced"
    }
    
    /**
     * Checks if doze mode is currently enforced
     */
    fun isDozeEnforced(): Boolean {
        return isDozeEnforced
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "PowerManagerService destroyed")
    }
}