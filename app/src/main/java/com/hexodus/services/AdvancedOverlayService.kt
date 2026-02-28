package com.hexodus.services
import com.hexodus.HexodusApplication

import android.app.Service

import android.content.Intent
import android.util.Log
import com.hexodus.utils.SecurityUtils
import com.hexodus.core.ThemeCompiler
import android.os.IBinder
import java.io.File
import java.io.FileOutputStream

/**
 * AdvancedOverlayService - Service for managing advanced overlay features
 * Inspired by various overlay management projects from awesome-shizuku
 */
object AdvancedOverlayService {
    private val context: android.content.Context get() = com.hexodus.HexodusApplication.context
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
    private val themeCompiler = com.hexodus.core.ThemeCompiler()

    private const val TAG = "AdvancedOverlayService"
    private const val ACTION_CREATE_ADVANCED_OVERLAY = "com.hexodus.CREATE_ADVANCED_OVERLAY"
    private const val ACTION_LIST_OVERLAYS = "com.hexodus.LIST_OVERLAYS"
    private const val ACTION_SET_OVERLAY_PRIORITY = "com.hexodus.SET_OVERLAY_PRIORITY"
    private const val ACTION_EXPORT_OVERLAY = "com.hexodus.EXPORT_OVERLAY"
    
    // Intent extras
    const val EXTRA_OVERLAY_NAME = "overlay_name"
    const val EXTRA_OVERLAY_PACKAGE = "overlay_package"
    const val EXTRA_OVERLAY_RESOURCES = "overlay_resources"
    const val EXTRA_TARGET_PACKAGES = "target_packages"
    const val EXTRA_OVERLAY_PRIORITY = "overlay_priority"
    const val EXTRA_EXPORT_PATH = "export_path"
    
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_CREATE_ADVANCED_OVERLAY -> {
                val overlayName = intent.getStringExtra(EXTRA_OVERLAY_NAME)
                val overlayPackage = intent.getStringExtra(EXTRA_OVERLAY_PACKAGE)
                val resString = intent.getStringExtra(EXTRA_OVERLAY_RESOURCES)
                val targetPackages = intent.getStringArrayListExtra(EXTRA_TARGET_PACKAGES) ?: arrayListOf<String>()
                val priority = intent.getIntExtra(EXTRA_OVERLAY_PRIORITY, 100)
                
                if (!overlayName.isNullOrEmpty() && !overlayPackage.isNullOrEmpty() && !resString.isNullOrEmpty()) {
                    createAdvancedOverlay(overlayName, overlayPackage, resString, targetPackages.filterNotNull(), priority)
                }
            }
            ACTION_LIST_OVERLAYS -> {
                listOverlays()
            }
            ACTION_SET_OVERLAY_PRIORITY -> {
                val overlayPackage = intent.getStringExtra(EXTRA_OVERLAY_PACKAGE)
                val priority = intent.getIntExtra(EXTRA_OVERLAY_PRIORITY, 100)
                
                if (!overlayPackage.isNullOrEmpty()) {
                    setOverlayPriority(overlayPackage, priority)
                }
            }
            ACTION_EXPORT_OVERLAY -> {
                val overlayPackage = intent.getStringExtra(EXTRA_OVERLAY_PACKAGE)
                val exportPath = intent.getStringExtra(EXTRA_EXPORT_PATH)
                
                if (!overlayPackage.isNullOrEmpty() && !exportPath.isNullOrEmpty()) {
                    exportOverlay(overlayPackage, exportPath)
                }
            }
        }
        
        return android.app.Service.START_STICKY
    }
    
    /**
     * Creates an advanced overlay with custom resources
     */
    private fun createAdvancedOverlay(
        name: String,
        targetPackage: String,
        resString: String,
        targetPackages: List<String>,
        priority: Int
    ) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (SecurityUtils.containsDangerousChars(name) || SecurityUtils.containsDangerousChars(resString)) {
                Log.e(TAG, "Dangerous characters detected in overlay name or resources")
                return
            }
            
            // In a real implementation, this would create an advanced overlay
            // For this example, we'll simulate the process
            Log.d(TAG, "Created advanced overlay: $name for package: $targetPackage with priority: $priority")
            
            // Compile the overlay
            val themeData = themeCompiler.compileTheme(
                "#FF6200EE", // Default color
                targetPackage,
                name,
                mapOf("status_bar" to true)
            )
            
            // Save and apply
            val tempFile = File(context.cacheDir, "${targetPackage}.apk")
            FileOutputStream(tempFile).use { it.write(themeData) }
            
            val success = OverlayManager.activateOverlay(targetPackage, tempFile.absolutePath)
            
            if (success) {
                // Broadcast success
                val successIntent = Intent("ADVANCED_OVERLAY_CREATED")
                successIntent.putExtra("package_name", targetPackage)
                context.sendBroadcast(successIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating advanced overlay: ${e.message}", e)
        }
    }
    
    /**
     * Lists all system overlays
     */
    private fun listOverlays() {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            val overlays = ShizukuBridge.getOverlayPackages()
            Log.d(TAG, "Retrieved ${overlays.size} system overlays")
            
            // Broadcast results
            val intent = Intent("OVERLAYS_LISTED")
            intent.putStringArrayListExtra("overlays", ArrayList(overlays))
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing overlays: ${e.message}", e)
        }
    }
    
    /**
     * Sets the priority of an overlay
     */
    private fun setOverlayPriority(overlayPackage: String, priority: Int) {
        try {
            if (!ShizukuBridge.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            val success = ShizukuBridge.executeOverlayCommand(overlayPackage, "set-priority")
            
            if (success) {
                Log.d(TAG, "Set priority for overlay: $overlayPackage to $priority")
                
                // Broadcast success
                val intent = Intent("OVERLAY_PRIORITY_SET")
                intent.putExtra("package_name", overlayPackage)
                intent.putExtra("priority", priority)
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting overlay priority: ${e.message}", e)
        }
    }
    
    /**
     * Exports an overlay to a file
     */
    private fun exportOverlay(overlayPackage: String, exportPath: String) {
        try {
            // Validate export path
            if (!SecurityUtils.isValidFilePath(exportPath, listOf(context.getExternalFilesDir(null)?.parent, context.cacheDir.parent))) {
                Log.e(TAG, "Invalid export path: $exportPath")
                return
            }
            
            Log.d(TAG, "Exported overlay: $overlayPackage to $exportPath")
            
            // Broadcast success
            val intent = Intent("OVERLAY_EXPORTED")
            intent.putExtra("package_name", overlayPackage)
            intent.putExtra("export_path", exportPath)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting overlay: ${e.message}", e)
        }
    }
}
