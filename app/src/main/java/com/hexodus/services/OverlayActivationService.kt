package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.utils.SecurityUtils

/**
 * OverlayActivationService - Enhanced service for overlay management
 * Manages the activation and deactivation of theme overlays with additional security
 */
class OverlayActivationService : Service() {
    
    companion object {
        private const val TAG = "OverlayActivationService"
        private const val ACTION_ACTIVATE_OVERLAY = "com.hexodus.ACTIVATE_OVERLAY"
        private const val ACTION_DEACTIVATE_OVERLAY = "com.hexodus.DEACTIVATE_OVERLAY"
        private const val ACTION_REFRESH_OVERLAYS = "com.hexodus.REFRESH_OVERLAYS"
        
        // Intent extras
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_APK_PATH = "apk_path"
        const val EXTRA_VALIDATE_SIGNATURE = "validate_signature"
    }
    
    private lateinit var shizukuBridgeService: ShizukuBridgeService
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "OverlayActivationService created")
        
        // Initialize dependencies
        shizukuBridgeService = ShizukuBridgeService()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_ACTIVATE_OVERLAY -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val apkPath = intent.getStringExtra(EXTRA_APK_PATH)
                val validateSignature = intent.getBooleanExtra(EXTRA_VALIDATE_SIGNATURE, true)
                
                if (!packageName.isNullOrEmpty() && !apkPath.isNullOrEmpty()) {
                    activateOverlay(packageName, apkPath, validateSignature)
                }
            }
            ACTION_DEACTIVATE_OVERLAY -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                
                if (!packageName.isNullOrEmpty()) {
                    deactivateOverlay(packageName)
                }
            }
            ACTION_REFRESH_OVERLAYS -> {
                refreshSystemUI()
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Activates an overlay using Shizuku with security validation
     */
    private fun activateOverlay(packageName: String, apkPath: String, validateSignature: Boolean = true) {
        try {
            // Validate the APK if required
            if (validateSignature) {
                if (!SecurityUtils.validateApkSignature(apkPath)) {
                    Log.e(TAG, "APK signature validation failed: $apkPath")
                    return
                }
            }
            
            // First install the APK if needed
            val installSuccess = shizukuBridgeService.installApk(apkPath)
            
            if (!installSuccess) {
                Log.e(TAG, "Failed to install overlay APK: $apkPath")
                return
            }
            
            // Then enable the overlay
            val enableSuccess = shizukuBridgeService.executeOverlayCommand(packageName, "enable")
            
            if (enableSuccess) {
                Log.d(TAG, "Successfully activated overlay: $packageName")
                
                // Set priority for the overlay
                shizukuBridgeService.executeOverlayCommand(packageName, "set-priority")
                
                // Refresh system UI to apply changes
                refreshSystemUI()
                
                // Broadcast success
                val successIntent = Intent("OVERLAY_ACTIVATION_SUCCESS")
                successIntent.putExtra("package_name", packageName)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to enable overlay: $packageName")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_ACTIVATION_FAILURE")
                failureIntent.putExtra("package_name", packageName)
                failureIntent.putExtra("error", "Failed to enable overlay")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error activating overlay: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAY_ACTIVATION_ERROR")
            errorIntent.putExtra("package_name", packageName)
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Deactivates an overlay using Shizuku
     */
    private fun deactivateOverlay(packageName: String) {
        try {
            val success = shizukuBridgeService.executeOverlayCommand(packageName, "disable")
            
            if (success) {
                Log.d(TAG, "Successfully deactivated overlay: $packageName")
                
                // Refresh system UI to apply changes
                refreshSystemUI()
                
                // Broadcast success
                val successIntent = Intent("OVERLAY_DEACTIVATION_SUCCESS")
                successIntent.putExtra("package_name", packageName)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to disable overlay: $packageName")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_DEACTIVATION_FAILURE")
                failureIntent.putExtra("package_name", packageName)
                failureIntent.putExtra("error", "Failed to disable overlay")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deactivating overlay: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAY_DEACTIVATION_ERROR")
            errorIntent.putExtra("package_name", packageName)
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Refreshes the system UI to apply theme changes
     */
    private fun refreshSystemUI() {
        try {
            // Use Shizuku to execute system commands
            shizukuBridgeService.executeShellCommand("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS")
            shizukuBridgeService.executeShellCommand("killall com.android.systemui")
            
            Log.d(TAG, "System UI refreshed")
            
            // Broadcast refresh
            val refreshIntent = Intent("SYSTEM_UI_REFRESHED")
            sendBroadcast(refreshIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing system UI: ${e.message}", e)
        }
    }
    
    /**
     * Gets list of currently active overlays
     */
    fun getActiveOverlays(): List<String> {
        return shizukuBridgeService.getOverlayPackages()
    }
    
    /**
     * Uninstalls an overlay package
     */
    fun uninstallOverlay(packageName: String): Boolean {
        return shizukuBridgeService.uninstallPackage(packageName)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "OverlayActivationService destroyed")
    }
}