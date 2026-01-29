package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs

/**
 * ShizukuBridgeService - Enhanced bridge for Shizuku communication
 * Handles privileged operations through Shizuku's API with additional security measures
 */
class ShizukuBridgeService : Service() {
    
    companion object {
        private const val TAG = "ShizukuBridgeService"
        private const val REQUEST_CODE_PERMISSION = 1001
    }
    
    private val binder = ShizukuBinder()
    
    inner class ShizukuBinder : Binder() {
        fun getService(): ShizukuBridgeService = this@ShizukuBridgeService
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ShizukuBridgeService created")
        
        // Initialize Shizuku
        initShizuku()
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    /**
     * Initializes Shizuku connection
     */
    private fun initShizuku() {
        try {
            // Set up Shizuku callbacks
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
            
            // Set up Shizuku state listener
            Shizuku.addBinderReceivedListener(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            
            // Check if Shizuku is available
            if (Shizuku.isPreV11()) {
                Log.w(TAG, "Shizuku version is too old")
                return
            }
            
            Log.d(TAG, "Shizuku initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Shizuku: ${e.message}", e)
        }
    }
    
    /**
     * Requests Shizuku permission
     */
    fun requestPermission() {
        if (!Shizuku.isPreV11() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(REQUEST_CODE_PERMISSION)
        }
    }
    
    /**
     * Checks if Shizuku is ready for use
     */
    fun isReady(): Boolean {
        return !Shizuku.isPreV11() && 
               Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED &&
               Shizuku.pingBinder()
    }
    
    /**
     * Executes a shell command through Shizuku
     */
    fun executeShellCommand(command: String): String? {
        if (!isReady()) {
            Log.w(TAG, "Shizuku is not ready or permission not granted")
            return null
        }

        // Validate command to prevent injection
        if (!isValidCommand(command)) {
            Log.e(TAG, "Invalid command blocked: $command")
            return null
        }

        return try {
            // Execute the command using Shizuku's shell access
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            Log.d(TAG, "Command executed: $command, Exit code: $exitCode")
            Log.d(TAG, "Command output: $output")

            if (exitCode == 0) output else null
        } catch (e: Exception) {
            Log.e(TAG, "Error executing shell command: ${e.message}", e)
            null
        }
    }

    /**
     * Validates commands to prevent injection attacks
     */
    private fun isValidCommand(command: String): Boolean {
        // Define a whitelist of allowed commands for the app's functionality
        val allowedCommands = listOf(
            "cmd overlay",
            "pm install",
            "pm uninstall",
            "cmd settings",
            "settings put",
            "settings get"
        )

        // Check if the command starts with an allowed prefix
        return allowedCommands.any { command.startsWith(it) }
    }
    
    /**
     * Executes overlay-related commands through Shizuku
     */
    fun executeOverlayCommand(packageName: String, action: String): Boolean {
        if (!isReady()) {
            Log.w(TAG, "Shizuku is not ready or permission not granted")
            return false
        }
        
        return try {
            val command = when (action) {
                "enable" -> "cmd overlay enable $packageName"
                "disable" -> "cmd overlay disable $packageName"
                "set-priority" -> "cmd overlay set-priority $packageName 100"
                else -> return false
            }
            
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val exitCode = process.waitFor()
            
            Log.d(TAG, "Overlay command executed: $command, Exit code: $exitCode")
            
            exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error executing overlay command: ${e.message}", e)
            false
        }
    }
    
    /**
     * Installs an APK using Shizuku
     */
    fun installApk(apkPath: String): Boolean {
        if (!isReady()) {
            Log.w(TAG, "Shizuku is not ready or permission not granted")
            return false
        }
        
        return try {
            val command = "pm install -r -d -t '$apkPath'"
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val exitCode = process.waitFor()
            
            Log.d(TAG, "APK installation attempted: $apkPath, Exit code: $exitCode")
            
            exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error installing APK: ${e.message}", e)
            false
        }
    }
    
    /**
     * Uninstalls an APK using Shizuku
     */
    fun uninstallPackage(packageName: String): Boolean {
        if (!isReady()) {
            Log.w(TAG, "Shizuku is not ready or permission not granted")
            return false
        }
        
        return try {
            val command = "pm uninstall '$packageName'"
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val exitCode = process.waitFor()
            
            Log.d(TAG, "Package uninstall attempted: $packageName, Exit code: $exitCode")
            
            exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error uninstalling package: ${e.message}", e)
            false
        }
    }
    
    /**
     * Gets list of overlay packages
     */
    fun getOverlayPackages(): List<String> {
        if (!isReady()) {
            Log.w(TAG, "Shizuku is not ready or permission not granted")
            return emptyList()
        }
        
        return try {
            val command = "cmd overlay list | grep ENABLED"
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                output.lines()
                    .filter { it.contains(":") }
                    .map { line -> 
                        line.substringAfterLast(":").trim() 
                    }
                    .filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting overlay packages: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Permission result listener for Shizuku
     */
    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Shizuku permission granted")
                
                // Broadcast permission granted
                val intent = Intent("SHIZUKU_PERMISSION_GRANTED")
                sendBroadcast(intent)
            } else {
                Log.w(TAG, "Shizuku permission denied")
                
                // Broadcast permission denied
                val intent = Intent("SHIZUKU_PERMISSION_DENIED")
                sendBroadcast(intent)
            }
        }
    }
    
    /**
     * Binder received listener
     */
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "Shizuku binder received")
        
        // Broadcast binder received
        val intent = Intent("SHIZUKU_BINDER_RECEIVED")
        sendBroadcast(intent)
    }
    
    /**
     * Binder dead listener
     */
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.d(TAG, "Shizuku binder dead")
        
        // Broadcast binder dead
        val intent = Intent("SHIZUKU_BINDER_DEAD")
        sendBroadcast(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Log.d(TAG, "ShizukuBridgeService destroyed")
    }
}