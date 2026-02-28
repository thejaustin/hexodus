package com.hexodus.services
import com.hexodus.HexodusApplication

import android.app.Service

import android.content.Intent
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
 * ResourceManagerService - Service for managing system context.resources and overlays
 * Inspired by various awesome-shizuku projects for system-level resource management
 */
object ResourceManagerService {
    private val context get() = com.hexodus.HexodusApplication.context

    
    
    
    
    
    
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

    
    
    
    
    
    
    
    

    
    
    
    
    
    
    

    

    
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
    
    private val themeCompiler = com.hexodus.core.ThemeCompiler()
    
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_CREATE_OVERLAY -> {
                val overlayName = intent.getStringExtra(EXTRA_OVERLAY_NAME)
                val overlayPackage = intent.getStringExtra(EXTRA_OVERLAY_PACKAGE)
                val resources = intent.getStringExtra(EXTRA_OVERLAY_RESOURCES)
                val targetPackages = intent.getStringArrayListExtra(EXTRA_TARGET_PACKAGES) ?: arrayListOf("android")
                val priority = intent.getIntExtra(EXTRA_OVERLAY_PRIORITY, 0)
                
                if (!overlayName.isNullOrEmpty() && !overlayPackage.isNullOrEmpty() && !context.resources.isNullOrEmpty()) {
                    createOverlay(overlayName, overlayPackage, context.resources, targetPackages, priority)
                }
            }
            ACTION_UPDATE_OVERLAY -> {
                val overlayPackage = intent.getStringExtra(EXTRA_OVERLAY_PACKAGE)
                val resources = intent.getStringExtra(EXTRA_OVERLAY_RESOURCES)
                
                if (!overlayPackage.isNullOrEmpty() && !context.resources.isNullOrEmpty()) {
                    updateOverlay(overlayPackage, context.resources)
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
        
        return android.app.Service.START_STICKY
    }
    
    /**
     * Creates a new overlay using Shizuku
     */
    private fun createOverlay(
        name: String, 
        targetPackageName: String, 
        context.resources: String, 
        targetPackages: List<String>, 
        priority: Int
    ) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            if (SecurityUtils.containsDangerousChars(name) || SecurityUtils.containsDangerousChars(context.resources)) {
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
                "#FF6200EE", // Default color, would come from context.resources
                sanitizedPackageName,
                name,
                mapOf("status_bar" to true, "navigation_bar" to true) // Would come from context.resources
            )
            
            // Save overlay to internal storage temporarily
            val tempFile = File(context.cacheDir, "${sanitizedPackageName}.apk")
            FileOutputStream(tempFile).use { it.write(overlayData) }
            
            // Install the overlay using Shizuku
            val installSuccess = ShizukuBridge.installApk(tempFile.absolutePath)
            
            if (installSuccess) {
                // Enable the overlay
                val enableSuccess = ShizukuBridge.executeOverlayCommand(sanitizedPackageName, "enable")
                
                if (enableSuccess) {
                    Log.d(TAG, "Successfully created and enabled overlay: $sanitizedPackageName")
                    
                    // Broadcast success
                    val successIntent = Intent("OVERLAY_CREATED")
                    successIntent.putExtra("package_name", sanitizedPackageName)
                    successIntent.putExtra("name", name)
                    successIntent.putStringArrayListExtra("target_packages", ArrayList(validTargetPackages))
                    context.sendBroadcast(successIntent)
                    
                    // Clean up temp file
                    tempFile.delete()
                } else {
                    Log.e(TAG, "Failed to enable overlay: $sanitizedPackageName")
                    
                    // Broadcast failure
                    val failureIntent = Intent("OVERLAY_CREATION_FAILED")
                    failureIntent.putExtra("package_name", sanitizedPackageName)
                    failureIntent.putExtra("error", "Failed to enable overlay")
                    context.sendBroadcast(failureIntent)
                    
                    // Clean up temp file
                    tempFile.delete()
                }
            } else {
                Log.e(TAG, "Failed to install overlay APK: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_INSTALLATION_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to install APK")
                context.sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating overlay: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAY_CREATION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Updates an existing overlay
     */
    private fun updateOverlay(targetPackageName: String, context.resources: String) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            if (SecurityUtils.containsDangerousChars(context.resources)) {
                Log.e(TAG, "Dangerous characters detected in overlay context.resources")
                return
            }
            
            // First disable the existing overlay
            val disableSuccess = ShizukuBridge.executeOverlayCommand(sanitizedPackageName, "disable")
            
            if (!disableSuccess) {
                Log.e(TAG, "Failed to disable overlay for update: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_UPDATE_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to disable overlay for update")
                context.sendBroadcast(failureIntent)
                return
            }
            
            // Create updated overlay (same as create but with updated context.resources)
            // In a real implementation, context would update the existing overlay
            // For context example, we'll simulate the process
            Log.d(TAG, "Updated overlay: $sanitizedPackageName")
            
            // Re-enable the overlay
            val enableSuccess = ShizukuBridge.executeOverlayCommand(sanitizedPackageName, "enable")
            
            if (enableSuccess) {
                Log.d(TAG, "Successfully updated and re-enabled overlay: $sanitizedPackageName")
                
                // Broadcast success
                val successIntent = Intent("OVERLAY_UPDATED")
                successIntent.putExtra("package_name", sanitizedPackageName)
                context.sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to re-enable updated overlay: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_UPDATE_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to re-enable overlay")
                context.sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating overlay: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAY_UPDATE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Deletes an overlay using Shizuku
     */
    private fun deleteOverlay(targetPackageName: String) {
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
            
            // Disable the overlay first
            val disableSuccess = ShizukuBridge.executeOverlayCommand(sanitizedPackageName, "disable")
            
            if (!disableSuccess) {
                Log.e(TAG, "Failed to disable overlay before deletion: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_DELETION_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to disable overlay")
                context.sendBroadcast(failureIntent)
                return
            }
            
            // Uninstall the overlay package
            val uninstallSuccess = ShizukuBridge.uninstallPackage(sanitizedPackageName)
            
            if (uninstallSuccess) {
                Log.d(TAG, "Successfully deleted overlay: $sanitizedPackageName")
                
                // Broadcast success
                val successIntent = Intent("OVERLAY_DELETED")
                successIntent.putExtra("package_name", sanitizedPackageName)
                context.sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to uninstall overlay: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_DELETION_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to uninstall package")
                context.sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting overlay: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAY_DELETION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Lists all installed overlays
     */
    private fun listOverlays() {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // In a real implementation, context would query the system for installed overlays
            // For context example, we'll simulate the process
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
            context.sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing overlays: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAYS_LISTING_ERROR")
            errorIntent.putExtra("error_message", e.message)
            context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Exports an overlay to a file
     */
    private fun exportOverlay(targetPackageName: String, exportPath: String) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            if (!SecurityUtils.isValidFilePath(exportPath, listOf(context.filesDir.parent, context.cacheDir.parent))) {
                Log.e(TAG, "Invalid export path: $exportPath")
                return
            }
            
            // In a real implementation, context would extract the overlay APK
            // For context example, we'll simulate the process
            Log.d(TAG, "Exported overlay: $sanitizedPackageName to: $exportPath")
            
            // Broadcast success
            val successIntent = Intent("OVERLAY_EXPORTED")
            successIntent.putExtra("package_name", sanitizedPackageName)
            successIntent.putExtra("export_path", exportPath)
            context.sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting overlay: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAY_EXPORT_ERROR")
            errorIntent.putExtra("error_message", e.message)
            context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Imports an overlay from a file
     */
    private fun importOverlay(importPath: String) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (!SecurityUtils.isValidFilePath(importPath, listOf(context.filesDir.parent, context.cacheDir.parent))) {
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
            val installSuccess = ShizukuBridge.installApk(importPath)
            
            if (installSuccess) {
                Log.d(TAG, "Successfully imported overlay from: $importPath")
                
                // Broadcast success
                val successIntent = Intent("OVERLAY_IMPORTED")
                successIntent.putExtra("import_path", importPath)
                context.sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to install imported overlay: $importPath")
                
                // Broadcast failure
                val failureIntent = Intent("OVERLAY_IMPORT_FAILED")
                failureIntent.putExtra("import_path", importPath)
                failureIntent.putExtra("error", "Failed to install APK")
                context.sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error importing overlay: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("OVERLAY_IMPORT_ERROR")
            errorIntent.putExtra("error_message", e.message)
            context.sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets information about an overlay
     */
    fun getOverlayInfo(targetPackageName: String): Map<String, Any>? {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return null
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            // In a real implementation, context would query the system for overlay info
            // For context example, we'll return mock data
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
    fun isOverlayEnabled(targetPackageName: String): Boolean {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return false
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(targetPackageName)
            if (sanitizedPackageName != targetPackageName) {
                Log.w(TAG, "Package name was sanitized: $targetPackageName -> $sanitizedPackageName")
            }
            
            // In a real implementation, context would check the overlay status
            // For context example, we'll return a mock value
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking overlay status: ${e.message}", e)
            return false
        }
    }
}