package com.hexodus.services
import com.hexodus.HexodusApplication

import android.app.Service

import android.content.Intent
import android.util.Log
import com.hexodus.utils.SecurityUtils

/**
 * AppManagerService - Service for advanced app management
 * Inspired by Hail, Ice Box, and Inure App Manager projects from awesome-shizuku
 */
object AppManagerService {
    

    
    
    
    
    
    
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

    
    
    
    
    
    
    
    

    
    
    
    
    
    
    

    

    
    private const val TAG = "AppManagerService"
    private const val ACTION_FREEZE_APP = "com.hexodus.FREEZE_APP"
    private const val ACTION_UNFREEZE_APP = "com.hexodus.UNFREEZE_APP"
    private const val ACTION_HIDE_APP = "com.hexodus.HIDE_APP"
    private const val ACTION_UNHIDE_APP = "com.hexodus.UNHIDE_APP"
    private const val ACTION_FORCE_STOP_APP = "com.hexodus.FORCE_STOP_APP"
    private const val ACTION_BATCH_OPERATION = "com.hexodus.BATCH_OPERATION"
    private const val ACTION_GET_APP_INFO = "com.hexodus.GET_APP_INFO"
    
    // Intent extras
    const val EXTRA_PACKAGE_NAME = "package_name"
    const val EXTRA_PACKAGE_NAMES = "package_names"
    const val EXTRA_OPERATION_TYPE = "operation_type"
    const val EXTRA_INCLUDE_SYSTEM_APPS = "include_system_apps"
    
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_FREEZE_APP -> {
                val targetPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                if (!targetPackageName.isNullOrEmpty()) {
                    freezeApp(targetPackageName)
                }
            }
            ACTION_UNFREEZE_APP -> {
                val targetPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                if (!targetPackageName.isNullOrEmpty()) {
                    unfreezeApp(targetPackageName)
                }
            }
            ACTION_HIDE_APP -> {
                val targetPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                if (!targetPackageName.isNullOrEmpty()) {
                    hideApp(targetPackageName)
                }
            }
            ACTION_UNHIDE_APP -> {
                val targetPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                if (!targetPackageName.isNullOrEmpty()) {
                    unhideApp(targetPackageName)
                }
            }
            ACTION_FORCE_STOP_APP -> {
                val targetPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                if (!targetPackageName.isNullOrEmpty()) {
                    forceStopApp(targetPackageName)
                }
            }
            ACTION_BATCH_OPERATION -> {
                val packageNames = intent.getStringArrayListExtra(EXTRA_PACKAGE_NAMES)
                val operationType = intent.getStringExtra(EXTRA_OPERATION_TYPE)
                
                if (!operationType.isNullOrEmpty() && packageNames != null) {
                    performBatchOperation(operationType, packageNames)
                }
            }
            ACTION_GET_APP_INFO -> {
                val targetPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                if (!targetPackageName.isNullOrEmpty()) {
                    getAppInfo(targetPackageName)
                }
            }
        }
        
        return android.app.Service.START_STICKY
    }
    
    /**
     * Freezes an app using Shizuku
     */
    private fun freezeApp(targetPackageName: String) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            val command = "pm disable-user --user 0 $sanitizedPackageName"
            val result = ShizukuBridge.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "App frozen: $sanitizedPackageName")
                
                // Broadcast success
                val successIntent = Intent("APP_FROZEN")
                successIntent.putExtra("package_name", sanitizedPackageName)
                HexodusApplication.context.sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to freeze app: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("APP_FREEZE_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to execute command")
                HexodusApplication.context.sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error freezing app: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_FREEZE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            HexodusApplication.context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Unfreezes an app using Shizuku
     */
    private fun unfreezeApp(targetPackageName: String) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            val command = "pm enable $sanitizedPackageName"
            val result = ShizukuBridge.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "App unfrozen: $sanitizedPackageName")
                
                // Broadcast success
                val successIntent = Intent("APP_UNFROZEN")
                successIntent.putExtra("package_name", sanitizedPackageName)
                HexodusApplication.context.sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to unfreeze app: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("APP_UNFREEZE_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to execute command")
                HexodusApplication.context.sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unfreezing app: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_UNFREEZE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            HexodusApplication.context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Hides an app using Shizuku
     */
    private fun hideApp(targetPackageName: String) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            val command = "pm hide $sanitizedPackageName"
            val result = ShizukuBridge.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "App hidden: $sanitizedPackageName")
                
                // Broadcast success
                val successIntent = Intent("APP_HIDDEN")
                successIntent.putExtra("package_name", sanitizedPackageName)
                HexodusApplication.context.sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to hide app: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("APP_HIDE_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to execute command")
                HexodusApplication.context.sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding app: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_HIDE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            HexodusApplication.context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Unhides an app using Shizuku
     */
    private fun unhideApp(targetPackageName: String) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            val command = "pm unhide $sanitizedPackageName"
            val result = ShizukuBridge.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "App unhidden: $sanitizedPackageName")
                
                // Broadcast success
                val successIntent = Intent("APP_UNHIDDEN")
                successIntent.putExtra("package_name", sanitizedPackageName)
                HexodusApplication.context.sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to unhide app: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("APP_UNHIDE_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to execute command")
                HexodusApplication.context.sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unhiding app: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_UNHIDE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            HexodusApplication.context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Force stops an app using Shizuku
     */
    private fun forceStopApp(targetPackageName: String) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            val command = "am force-stop $sanitizedPackageName"
            val result = ShizukuBridge.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "App force stopped: $sanitizedPackageName")
                
                // Broadcast success
                val successIntent = Intent("APP_FORCE_STOPPED")
                successIntent.putExtra("package_name", sanitizedPackageName)
                HexodusApplication.context.sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to force stop app: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("APP_FORCE_STOP_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to execute command")
                HexodusApplication.context.sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error force stopping app: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_FORCE_STOP_ERROR")
            errorIntent.putExtra("error_message", e.message)
            HexodusApplication.context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Performs a batch operation on multiple apps
     */
    private fun performBatchOperation(operationType: String, packageNames: ArrayList<String>) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            val sanitizedPackageNames = packageNames.map { SecurityUtils.sanitizePackageName(it) }
            
            // Validate inputs
            if (sanitizedPackageNames.any { SecurityUtils.containsDangerousChars(it) }) {
                Log.e(TAG, "Dangerous characters detected in package names")
                return
            }
            
            var successCount = 0
            val failedPackages = mutableListOf<String>()
            
            for (targetPackageName in sanitizedPackageNames) {
                val command = when (operationType.lowercase()) {
                    "freeze" -> "pm disable-user --user 0 $targetPackageName"
                    "unfreeze" -> "pm enable $targetPackageName"
                    "hide" -> "pm hide $targetPackageName"
                    "unhide" -> "pm unhide $targetPackageName"
                    "force_stop" -> "am force-stop $targetPackageName"
                    else -> {
                        Log.w(TAG, "Unknown operation type: $operationType")
                        failedPackages.add(targetPackageName)
                        continue
                    }
                }
                
                val result = ShizukuBridge.executeShellCommand(command)
                
                if (result != null) {
                    successCount++
                } else {
                    failedPackages.add(targetPackageName)
                }
            }
            
            Log.d(TAG, "Batch operation completed: $successCount/$packageNames.size succeeded")
            
            // Broadcast success
            val successIntent = Intent("BATCH_OPERATION_COMPLETED")
            successIntent.putExtra("operation_type", operationType)
            successIntent.putExtra("success_count", successCount)
            successIntent.putExtra("total_count", packageNames.size)
            successIntent.putStringArrayListExtra("failed_packages", ArrayList(failedPackages))
            HexodusApplication.context.sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing batch operation: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("BATCH_OPERATION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            HexodusApplication.context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets app information using Shizuku
     */
    private fun getAppInfo(targetPackageName: String) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            val command = "dumpsys package $sanitizedPackageName"
            val result = ShizukuBridge.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "App info retrieved for: $sanitizedPackageName")
                
                // Parse basic info from dumpsys output
                val appInfo = parseAppInfo(result)
                
                // Broadcast success
                val successIntent = Intent("APP_INFO_RETRIEVED")
                successIntent.putExtra("package_name", sanitizedPackageName)
                successIntent.putExtra("app_info", HashMap(appInfo))
                HexodusApplication.context.sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to get app info: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("APP_INFO_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to execute command")
                HexodusApplication.context.sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app info: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_INFO_ERROR")
            errorIntent.putExtra("error_message", e.message)
            HexodusApplication.context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Parses app information from dumpsys output
     */
    private fun parseAppInfo(dumpsysOutput: String): Map<String, String> {
        val info = mutableMapOf<String, String>()
        
        // Extract basic information from dumpsys output
        dumpsysOutput.lines().forEach { line ->
            if (line.contains("versionName=")) {
                val version = line.substringAfter("versionName=").substringBefore(" ")
                info["version"] = version
            } else if (line.contains("firstInstallTime=")) {
                val installTime = line.substringAfter("firstInstallTime=").substringBefore(" ")
                info["install_time"] = installTime
            } else if (line.contains("lastUpdateTime=")) {
                val updateTime = line.substringAfter("lastUpdateTime=").substringBefore(" ")
                info["update_time"] = updateTime
            }
        }
        
        return info
    }
    
    /**
     * Gets list of installed apps
     */
    fun getInstalledApps(includeSystemApps: Boolean = false): List<Map<String, String>> {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return emptyList()
            }
            
            val command = if (includeSystemApps) {
                "pm list packages -f"
            } else {
                "pm list packages -3 -f"  // Only third-party apps
            }
            
            val result = ShizukuBridge.executeShellCommand(command)
            
            if (!result.isNullOrEmpty()) {
                return result.lines()
                    .filter { it.startsWith("package:") }
                    .map { line ->
                        val pathAndPackage = line.substringAfter("package:").split("=")
                        if (pathAndPackage.size == 2) {
                            mapOf(
                                "path" to pathAndPackage[0],
                                "package_name" to pathAndPackage[1]
                            )
                        } else {
                            mapOf("package_name" to line.substringAfter("package:"))
                        }
                    }
                    .filter { it.containsKey("package_name") }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed apps: ${e.message}", e)
        }
        
        return emptyList()
    }
}