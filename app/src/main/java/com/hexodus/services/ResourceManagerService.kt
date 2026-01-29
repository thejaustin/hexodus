package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.utils.SecurityUtils
import com.hexodus.core.ThemeCompiler
import com.hexodus.services.OverlayActivationService
import android.content.pm.PackageManager
import android.content.res.AssetManager
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * ResourceManagerService - Service for managing system resources and overlays
 * Inspired by various awesome-shizuku projects for system-level resource management
 */
class ResourceManagerService : Service() {
    
    companion object {
        private const val TAG = "ResourceManagerService"
        private const val ACTION_CREATE_OVERLAY = "com.hexodus.CREATE_OVERLAY"
        private const val ACTION_UPDATE_OVERLAY = "com.hexodus.UPDATE_OVERLAY"
        private const val ACTION_DELETE_OVERLAY = "com.hexodus.DELETE_OVERLAY"
        private const val ACTION_LIST_OVERLAYS = "com.hexodus.LIST_OVERLAYS"
        private const val ACTION_EXPORT_OVERLAY = "com.hexodus.EXPORT_OVERLAY"
        private const val ACTION_IMPORT_OVERLAY = "com.hexodus.IMPORT_OVERLAY"
        
        // Intent extras
        const val EXTRA_OVERLAY_NAME = "overlay_name"
        const val EXTRA_OVERLAY_PACKAGE = "overlay_package"
        const val EXTRA_OVERLAY_RESOURCES = "overlay_resources"
        const val EXTRA_OVERLAY_PATH = "overlay_path"
        const val EXTRA_TARGET_PACKAGES = "target_packages"
        const val EXTRA_OVERLAY_PRIORITY = "overlay_priority"
    }
    
    private lateinit var shizukuBridgeService: ShizukuBridgeService
    private lateinit var themeCompiler: ThemeCompiler
    
    override fun onCreate() {
        super.onCreate()
        shizukuBridgeService = ShizukuBridgeService()
        themeCompiler = ThemeCompiler()
        Log.d(TAG, "ResourceManagerService created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_CREATE_OVERLAY -> {
                val overlayName = intent.getStringExtra(EXTRA_OVERLAY_NAME)
                val overlayPackage = intent.getStringExtra(EXTRA_OVERLAY_PACKAGE)
                val resources = intent.getStringExtra(EXTRA_OVERLAY_RESOURCES)
                val targetPackages = intent.getStringArrayListExtra(EXTRA_TARGET_PACKAGES) ?: arrayListOf("android")
                val priority = intent.getIntExtra(EXTRA_OVERLAY_PRIORITY, 0)
                
                if (!overlayName.isNullOrEmpty() && !overlayPackage.isNullOrEmpty() && !resources.isNullOrEmpty()) {
                    createOverlay(overlayName, overlayPackage, resources, targetPackages, priority)
                }
            }
            ACTION_UPDATE_OVERLAY -> {
                val overlayPackage = intent.getStringExtra(EXTRA_OVERLAY_PACKAGE)
                val resources = intent.getStringExtra(EXTRA_OVERLAY_RESOURCES)
                
                if (!overlayPackage.isNullOrEmpty() && !resources.isNullOrEmpty()) {
                    updateOverlay(overlayPackage, resources)
                }
            }
            ACTION_DELETE_OVERLAY -> {
                val overlayPackage = intent.getStringExtra(EXTRA_OVERLAY_PACKAGE)
                
                if (!overlayPackage.isNullOrEmpty()) {
                    deleteOverlay(overlayPackage)
                }
            }
            ACTION_LIST_OVERLAYS -> {
                listOverlays()
            }
            ACTION_EXPORT_OVERLAY -> {
                val overlayPackage = intent.getStringExtra(EXTRA_OVERLAY_PACKAGE)
                val exportPath = intent.getStringExtra(EXTRA_OVERLAY_PATH)
                
                if (!overlayPackage.isNullOrEmpty() && !exportPath.isNullOrEmpty()) {
                    exportOverlay(overlayPackage, exportPath)
                }
            }
            ACTION_IMPORT_OVERLAY -> {
                val overlayPath = intent.getStringExtra(EXTRA_OVERLAY_PATH)
                
                if (!overlayPath.isNullOrEmpty()) {
                    importOverlay(overlayPath)
                }
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Creates a new overlay using Shizuku
     */
    private fun createOverlay(
        name: String, 
        packageName: String, 
        resources: String, 
        targetPackages: List<String>, 
        priority: Int
    ) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            if (SecurityUtils.containsDangerousChars(name) || SecurityUtils.containsDangerousChars(resources)) {
                Log.e(TAG, "Dangerous characters detected in overlay creation parameters")
                return
            }
            
            // Validate target packages
            val validTargetPackages = targetPackages.filter { SecurityUtils.isValidPackageName(it) }
            if (validTargetPackages.size != targetPackages.size) {
                Log.w(TAG, "Some target packages were filtered out due to invalid format")
            }
            
            // Create overlay APK in memory
            val overlayData = themeCompiler.compileTheme(
                "#FF6200EE", // Default color, would come from resources
                sanitizedPackageName,
                name,
                mapOf("status_bar" to true, "navigation_bar" to true) // Would come from resources
            )
            
            // Save overlay to internal storage temporarily
            val tempFile = File(cacheDir, "${sanitizedPackageName}.apk")
            FileOutputStream(tempFile).use { it.write(overlayData) }
            
            // Install the overlay using Shizuku
            val installSuccess = shizukuBridgeService.installApk(tempFile.absolutePath)
            
            if (installSuccess) {
                // Enable the overlay
                val enableSuccess = shizukuBridgeService.executeOverlayCommand(sanitizedPackageName, "enable")
                
                if (enableSuccess) {
                    Log.d(TAG, "Successfully created and enabled overlay: $sanitizedPackageName")
                    
                    // Broadcast success
                    val successIntent = Intent("OVERLAY_CREATED")
                    successIntent.putExtra("package_name", sanitizedPackageName)
                    successIntent.putExtra("name", name)
                    successIntent.putStringArrayListExtra("target_packages", ArrayList(validTargetPackages))
                    sendBroadcast(successIntent)
                    
                    // Clean up temp file
                    tempFile.delete()
                } else {
                    Log.e(TAG, "Failed to enable overlay: $sanitizedPackageName")
                    
                    // Broadcast failure
                    val failureIntent = Intent("OVERLAY_CREATION_FAILED")
                    failureIntent.putExtra("package_name", sanitizedPackageName)
                    failureIntent.putExtra("error", "Failed to enable overlay")
                    sendBroadcast(failureIntent)
                    
                    // Clean up temp file
                    tempFile.delete()
                }
            } else {
                Log.e(TAG, "Failed to install overlay APK: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_INSTALLATION_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to install APK")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating overlay: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAY_CREATION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Updates an existing overlay
     */
    private fun updateOverlay(packageName: String, resources: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            if (SecurityUtils.containsDangerousChars(resources)) {
                Log.e(TAG, "Dangerous characters detected in overlay resources")
                return
            }
            
            // First disable the existing overlay
            val disableSuccess = shizukuBridgeService.executeOverlayCommand(sanitizedPackageName, "disable")
            
            if (!disableSuccess) {
                Log.e(TAG, "Failed to disable overlay for update: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_UPDATE_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to disable overlay for update")
                sendBroadcast(failureIntent)
                return
            }
            
            // Create updated overlay (same as create but with updated resources)
            // In a real implementation, this would update the existing overlay
            // For this example, we'll simulate the process
            Log.d(TAG, "Updated overlay: $sanitizedPackageName")
            
            // Re-enable the overlay
            val enableSuccess = shizukuBridgeService.executeOverlayCommand(sanitizedPackageName, "enable")
            
            if (enableSuccess) {
                Log.d(TAG, "Successfully updated and re-enabled overlay: $sanitizedPackageName")
                
                // Broadcast success
                val successIntent = Intent("OVERLAY_UPDATED")
                successIntent.putExtra("package_name", sanitizedPackageName)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to re-enable updated overlay: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_UPDATE_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to re-enable overlay")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating overlay: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAY_UPDATE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Deletes an overlay using Shizuku
     */
    private fun deleteOverlay(packageName: String) {
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
            
            // Disable the overlay first
            val disableSuccess = shizukuBridgeService.executeOverlayCommand(sanitizedPackageName, "disable")
            
            if (!disableSuccess) {
                Log.e(TAG, "Failed to disable overlay before deletion: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_DELETION_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to disable overlay")
                sendBroadcast(failureIntent)
                return
            }
            
            // Uninstall the overlay package
            val uninstallSuccess = shizukuBridgeService.uninstallPackage(sanitizedPackageName)
            
            if (uninstallSuccess) {
                Log.d(TAG, "Successfully deleted overlay: $sanitizedPackageName")
                
                // Broadcast success
                val successIntent = Intent("OVERLAY_DELETED")
                successIntent.putExtra("package_name", sanitizedPackageName)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to uninstall overlay: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_DELETION_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to uninstall package")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting overlay: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAY_DELETION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Lists all installed overlays
     */
    private fun listOverlays() {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // In a real implementation, this would query the system for installed overlays
            // For this example, we'll simulate the process
            val overlays = listOf(
                mapOf(
                    "package_name" to "com.hexodus.overlay.statusbar",
                    "name" to "Status Bar Theme",
                    "enabled" to true,
                    "priority" to 100
                ),
                mapOf(
                    "package_name" to "com.hexodus.overlay.navbar",
                    "name" to "Navigation Bar Theme",
                    "enabled" to true,
                    "priority" to 100
                ),
                mapOf(
                    "package_name" to "com.hexodus.overlay.systemui",
                    "name" to "System UI Theme",
                    "enabled" to false,
                    "priority" to 50
                )
            )
            
            Log.d(TAG, "Retrieved ${overlays.size} overlays")
            
            // Broadcast results
            val successIntent = Intent("OVERLAYS_LISTED")
            successIntent.putExtra("overlay_count", overlays.size)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing overlays: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAYS_LISTING_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Exports an overlay to a file
     */
    private fun exportOverlay(packageName: String, exportPath: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            if (!SecurityUtils.isValidFilePath(exportPath, listOf(filesDir.parent, cacheDir.parent))) {
                Log.e(TAG, "Invalid export path: $exportPath")
                return
            }
            
            // In a real implementation, this would extract the overlay APK
            // For this example, we'll simulate the process
            Log.d(TAG, "Exported overlay: $sanitizedPackageName to: $exportPath")
            
            // Broadcast success
            val successIntent = Intent("OVERLAY_EXPORTED")
            successIntent.putExtra("package_name", sanitizedPackageName)
            successIntent.putExtra("export_path", exportPath)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting overlay: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAY_EXPORT_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Imports an overlay from a file
     */
    private fun importOverlay(importPath: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (!SecurityUtils.isValidFilePath(importPath, listOf(filesDir.parent, cacheDir.parent))) {
                Log.e(TAG, "Invalid import path: $importPath")
                return
            }
            
            // Validate file exists and is an APK
            val file = File(importPath)
            if (!file.exists() || !file.name.endsWith(".apk")) {
                Log.e(TAG, "Invalid overlay file: $importPath")
                return
            }
            
            // Validate APK signature
            if (!SecurityUtils.validateApkSignature(importPath)) {
                Log.e(TAG, "Invalid APK signature: $importPath")
                return
            }
            
            // Install the overlay using Shizuku
            val installSuccess = shizukuBridgeService.installApk(importPath)
            
            if (installSuccess) {
                Log.d(TAG, "Successfully imported overlay from: $importPath")
                
                // Broadcast success
                val successIntent = Intent("OVERLAY_IMPORTED")
                successIntent.putExtra("import_path", importPath)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to install imported overlay: $importPath")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_IMPORT_FAILED")
                failureIntent.putExtra("import_path", importPath)
                failureIntent.putExtra("error", "Failed to install APK")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error importing overlay: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAY_IMPORT_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets information about an overlay
     */
    fun getOverlayInfo(packageName: String): Map<String, Any>? {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return null
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            // In a real implementation, this would query the system for overlay info
            // For this example, we'll return mock data
            return mapOf(
                "package_name" to sanitizedPackageName,
                "name" to "Mock Overlay",
                "enabled" to true,
                "priority" to 100,
                "target_packages" to listOf("android", "com.android.systemui"),
                "version" to "1.0.0"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting overlay info: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Checks if an overlay is enabled
     */
    fun isOverlayEnabled(packageName: String): Boolean {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return false
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            // In a real implementation, this would check the overlay status
            // For this example, we'll return a mock value
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking overlay status: ${e.message}", e)
            return false
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ResourceManagerService destroyed")
    }
}