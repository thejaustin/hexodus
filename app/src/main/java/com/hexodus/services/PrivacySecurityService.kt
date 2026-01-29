package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.utils.SecurityUtils
import java.io.File

/**
 * PrivacySecurityService - Service for privacy and security features
 * Inspired by AppLock, Amarok-Hider, and PrivacyFlip projects from awesome-shizuku
 */
class PrivacySecurityService : Service() {
    
    companion object {
        private const val TAG = "PrivacySecurityService"
        private const val ACTION_LOCK_APP = "com.hexodus.LOCK_APP"
        private const val ACTION_UNLOCK_APP = "com.hexodus.UNLOCK_APP"
        private const val ACTION_HIDE_FILE = "com.hexodus.HIDE_FILE"
        private const val ACTION_UNHIDE_FILE = "com.hexodus.UNHIDE_FILE"
        private const val ACTION_HIDE_APP = "com.hexodus.HIDE_APP_PRIVACY"
        private const val ACTION_UNHIDE_APP = "com.hexodus.UNHIDE_APP_PRIVACY"
        private const val ACTION_MANAGE_PRIVACY = "com.hexodus.MANAGE_PRIVACY"
        private const val ACTION_SCAN_PRIVACY = "com.hexodus.SCAN_PRIVACY"
        
        // Intent extras
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_FILE_PATH = "file_path"
        const val EXTRA_LOCK_METHOD = "lock_method"
        const val EXTRA_PRIVACY_RULES = "privacy_rules"
        const val EXTRA_DEVICE_LOCK_STATE = "device_lock_state"
    }
    
    private lateinit var shizukuBridgeService: ShizukuBridgeService
    private val hiddenFiles = mutableSetOf<String>()
    private val lockedApps = mutableSetOf<String>()
    
    override fun onCreate() {
        super.onCreate()
        shizukuBridgeService = ShizukuBridgeService()
        Log.d(TAG, "PrivacySecurityService created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_LOCK_APP -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val lockMethod = intent.getStringExtra(EXTRA_LOCK_METHOD) ?: "pin"
                
                if (!packageName.isNullOrEmpty()) {
                    lockApp(packageName, lockMethod)
                }
            }
            ACTION_UNLOCK_APP -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                
                if (!packageName.isNullOrEmpty()) {
                    unlockApp(packageName)
                }
            }
            ACTION_HIDE_FILE -> {
                val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
                
                if (!filePath.isNullOrEmpty()) {
                    hideFile(filePath)
                }
            }
            ACTION_UNHIDE_FILE -> {
                val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
                
                if (!filePath.isNullOrEmpty()) {
                    unhideFile(filePath)
                }
            }
            ACTION_HIDE_APP -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                
                if (!packageName.isNullOrEmpty()) {
                    hideApp(packageName)
                }
            }
            ACTION_UNHIDE_APP -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                
                if (!packageName.isNullOrEmpty()) {
                    unhideApp(packageName)
                }
            }
            ACTION_MANAGE_PRIVACY -> {
                val rules = intent.getStringExtra(EXTRA_PRIVACY_RULES)
                val deviceLocked = intent.getBooleanExtra(EXTRA_DEVICE_LOCK_STATE, false)
                
                managePrivacy(rules, deviceLocked)
            }
            ACTION_SCAN_PRIVACY -> {
                scanForPrivacyIssues()
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Locks an app with PIN/biometric
     */
    private fun lockApp(packageName: String, lockMethod: String) {
        try {
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            // Add to locked apps list
            lockedApps.add(sanitizedPackageName)
            
            Log.d(TAG, "App locked: $sanitizedPackageName using method: $lockMethod")
            
            // Store in preferences
            val prefs = getSharedPreferences("app_locks", 0)
            val editor = prefs.edit()
            val currentLocked = prefs.getStringSet("locked_apps", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            currentLocked.add(sanitizedPackageName)
            editor.putStringSet("locked_apps", currentLocked)
            editor.apply()
            
            // Broadcast success
            val successIntent = Intent("APP_LOCKED")
            successIntent.putExtra("package_name", sanitizedPackageName)
            successIntent.putExtra("lock_method", lockMethod)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error locking app: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_LOCK_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Unlocks an app
     */
    private fun unlockApp(packageName: String) {
        try {
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            // Remove from locked apps list
            lockedApps.remove(sanitizedPackageName)
            
            Log.d(TAG, "App unlocked: $sanitizedPackageName")
            
            // Update preferences
            val prefs = getSharedPreferences("app_locks", 0)
            val editor = prefs.edit()
            val currentLocked = prefs.getStringSet("locked_apps", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            currentLocked.remove(sanitizedPackageName)
            editor.putStringSet("locked_apps", currentLocked)
            editor.apply()
            
            // Broadcast success
            val successIntent = Intent("APP_UNLOCKED")
            successIntent.putExtra("package_name", sanitizedPackageName)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error unlocking app: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_UNLOCK_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Hides a file by changing its attributes
     */
    private fun hideFile(filePath: String) {
        try {
            // Validate file path
            if (!SecurityUtils.isValidFilePath(filePath, listOf(filesDir.parent, cacheDir.parent))) {
                Log.e(TAG, "Invalid file path: $filePath")
                return
            }
            
            if (SecurityUtils.containsDangerousChars(filePath)) {
                Log.e(TAG, "Dangerous characters detected in file path: $filePath")
                return
            }
            
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "File does not exist: $filePath")
                return
            }
            
            // In a real implementation, this would use Shizuku to change file attributes
            // For this example, we'll add to our hidden list
            hiddenFiles.add(filePath)
            
            // Also try to change file attributes to hidden
            if (file.renameTo(File("${filePath}.hidden"))) {
                Log.d(TAG, "File hidden by renaming: $filePath")
            } else {
                Log.d(TAG, "File marked as hidden: $filePath")
            }
            
            // Store in preferences
            val prefs = getSharedPreferences("hidden_files", 0)
            val editor = prefs.edit()
            val currentHidden = prefs.getStringSet("hidden_files", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            currentHidden.add(filePath)
            editor.putStringSet("hidden_files", currentHidden)
            editor.apply()
            
            // Broadcast success
            val successIntent = Intent("FILE_HIDDEN")
            successIntent.putExtra("file_path", filePath)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding file: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("FILE_HIDE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Unhides a file
     */
    private fun unhideFile(filePath: String) {
        try {
            // Validate file path
            if (!SecurityUtils.isValidFilePath(filePath, listOf(filesDir.parent, cacheDir.parent))) {
                Log.e(TAG, "Invalid file path: $filePath")
                return
            }
            
            if (SecurityUtils.containsDangerousChars(filePath)) {
                Log.e(TAG, "Dangerous characters detected in file path: $filePath")
                return
            }
            
            val hiddenFile = File("${filePath}.hidden")
            if (hiddenFile.exists()) {
                // Rename back to original
                if (hiddenFile.renameTo(File(filePath))) {
                    Log.d(TAG, "File unhidden by renaming: $filePath")
                }
            }
            
            // Remove from hidden list
            hiddenFiles.remove(filePath)
            
            // Update preferences
            val prefs = getSharedPreferences("hidden_files", 0)
            val editor = prefs.edit()
            val currentHidden = prefs.getStringSet("hidden_files", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            currentHidden.remove(filePath)
            editor.putStringSet("hidden_files", currentHidden)
            editor.apply()
            
            // Broadcast success
            val successIntent = Intent("FILE_UNHIDDEN")
            successIntent.putExtra("file_path", filePath)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error unhiding file: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("FILE_UNHIDE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Hides an app icon from launcher
     */
    private fun hideApp(packageName: String) {
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
            
            // Use pm to hide the app
            val command = "pm hide $sanitizedPackageName"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "App hidden from launcher: $sanitizedPackageName")
                
                // Broadcast success
                val successIntent = Intent("APP_HIDDEN_FROM_LAUNCHER")
                successIntent.putExtra("package_name", sanitizedPackageName)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to hide app from launcher: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("APP_HIDE_FROM_LAUNCHER_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding app: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_HIDE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Unhides an app icon from launcher
     */
    private fun unhideApp(packageName: String) {
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
            
            // Use pm to unhide the app
            val command = "pm unhide $sanitizedPackageName"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "App unhidden from launcher: $sanitizedPackageName")
                
                // Broadcast success
                val successIntent = Intent("APP_UNHIDDEN_FROM_LAUNCHER")
                successIntent.putExtra("package_name", sanitizedPackageName)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to unhide app from launcher: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("APP_UNHIDE_FROM_LAUNCHER_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unhiding app: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_UNHIDE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Manages privacy based on device lock state
     */
    private fun managePrivacy(rules: String?, deviceLocked: Boolean) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            Log.d(TAG, "Managing privacy with device locked: $deviceLocked")
            
            // In a real implementation, this would apply privacy rules based on lock state
            // For this example, we'll just log the action
            if (deviceLocked) {
                // Apply privacy rules when device is locked
                Log.d(TAG, "Applying privacy rules for locked state")
            } else {
                // Apply different rules when device is unlocked
                Log.d(TAG, "Applying privacy rules for unlocked state")
            }
            
            // Broadcast success
            val successIntent = Intent("PRIVACY_MANAGED")
            successIntent.putExtra("device_locked", deviceLocked)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error managing privacy: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("PRIVACY_MANAGE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Scans for privacy issues
     */
    private fun scanForPrivacyIssues() {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // In a real implementation, this would scan for privacy issues
            // For this example, we'll return mock results
            val privacyIssues = listOf(
                mapOf("type" to "location_access", "app" to "com.example.tracking_app", "severity" to "high"),
                mapOf("type" to "camera_access", "app" to "com.example.camera_app", "severity" to "medium"),
                mapOf("type" to "microphone_access", "app" to "com.example.recording_app", "severity" to "high")
            )
            
            Log.d(TAG, "Privacy scan completed, found ${privacyIssues.size} issues")
            
            // Broadcast results
            val resultsIntent = Intent("PRIVACY_SCAN_COMPLETED")
            resultsIntent.putExtra("issue_count", privacyIssues.size)
            sendBroadcast(resultsIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning for privacy issues: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("PRIVACY_SCAN_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets list of locked apps
     */
    fun getLockedApps(): Set<String> {
        val prefs = getSharedPreferences("app_locks", 0)
        return prefs.getStringSet("locked_apps", mutableSetOf()) ?: mutableSetOf()
    }
    
    /**
     * Gets list of hidden files
     */
    fun getHiddenFiles(): Set<String> {
        val prefs = getSharedPreferences("hidden_files", 0)
        return prefs.getStringSet("hidden_files", mutableSetOf()) ?: mutableSetOf()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "PrivacySecurityService destroyed")
    }
}