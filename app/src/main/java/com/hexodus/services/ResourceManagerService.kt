package com.hexodus.services
import com.hexodus.HexodusApplication

import android.app.Service

import android.content.Intent
import android.util.Log
import com.hexodus.utils.SecurityUtils
import com.hexodus.core.ThemeCompiler
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
object ResourceManagerService {
    private val context: android.content.Context get() = com.hexodus.HexodusApplication.context
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
    private val themeCompiler = com.hexodus.core.ThemeCompiler()

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
    
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_CREATE_OVERLAY -> {
                val overlayName = intent.getStringExtra(EXTRA_OVERLAY_NAME)
                val overlayPackage = intent.getStringExtra(EXTRA_OVERLAY_PACKAGE)
                val resString = intent.getStringExtra(EXTRA_OVERLAY_RESOURCES)
                val targetPackages = intent.getStringArrayListExtra(EXTRA_TARGET_PACKAGES) ?: arrayListOf("android")
                val priority = intent.getIntExtra(EXTRA_OVERLAY_PRIORITY, 0)
                
                if (!overlayName.isNullOrEmpty() && !overlayPackage.isNullOrEmpty() && !resString.isNullOrEmpty()) {
                    createOverlay(overlayName, overlayPackage, resString, targetPackages, priority)
                }
            }
            ACTION_UPDATE_OVERLAY -> {
                val overlayPackage = intent.getStringExtra(EXTRA_OVERLAY_PACKAGE)
                val resString = intent.getStringExtra(EXTRA_OVERLAY_RESOURCES)
                
                if (!overlayPackage.isNullOrEmpty() && !resString.isNullOrEmpty()) {
                    updateOverlay(overlayPackage, resString)
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
    
    private fun createOverlay(
        name: String, 
        targetPackage: String, 
        resString: String, 
        targetPackages: List<String>, 
        priority: Int
    ) {
        try {
            if (!ShizukuBridge.isReady()) return
            val sanitized = SecurityUtils.sanitizePackageName(targetPackage)
            
            val overlayData = themeCompiler.compileTheme(
                "#FF6200EE",
                sanitized,
                name,
                mapOf("status_bar" to true)
            )
            
            val tempFile = File(context.cacheDir, "${sanitized}.apk")
            FileOutputStream(tempFile).use { it.write(overlayData) }
            
            val installSuccess = ShizukuBridge.installApk(tempFile.absolutePath)
            if (installSuccess) {
                ShizukuBridge.executeOverlayCommand(sanitized, "enable")
                val successIntent = Intent("OVERLAY_CREATED")
                successIntent.putExtra("package_name", sanitized)
                context.sendBroadcast(successIntent)
            }
            tempFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating overlay: ${e.message}", e)
        }
    }
    
    private fun updateOverlay(targetPackage: String, resString: String) {
        try {
            if (!ShizukuBridge.isReady()) return
            val sanitized = SecurityUtils.sanitizePackageName(targetPackage)
            ShizukuBridge.executeOverlayCommand(sanitized, "disable")
            ShizukuBridge.executeOverlayCommand(sanitized, "enable")
            
            val successIntent = Intent("OVERLAY_UPDATED")
            successIntent.putExtra("package_name", sanitized)
            context.sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating overlay: ${e.message}", e)
        }
    }
    
    private fun deleteOverlay(targetPackage: String) {
        try {
            if (!ShizukuBridge.isReady()) return
            val sanitized = SecurityUtils.sanitizePackageName(targetPackage)
            ShizukuBridge.executeOverlayCommand(sanitized, "disable")
            ShizukuBridge.uninstallPackage(sanitized)
            
            val successIntent = Intent("OVERLAY_DELETED")
            successIntent.putExtra("package_name", sanitized)
            context.sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting overlay: ${e.message}", e)
        }
    }
    
    private fun listOverlays() {
        try {
            if (!ShizukuBridge.isReady()) return
            val overlays = ShizukuBridge.getOverlayPackages()
            val intent = Intent("OVERLAYS_LISTED")
            intent.putExtra("overlay_count", overlays.size)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing overlays: ${e.message}", e)
        }
    }
    
    private fun exportOverlay(targetPackage: String, exportPath: String) {
        try {
            if (!SecurityUtils.isValidFilePath(exportPath, listOf(context.filesDir.parent, context.cacheDir.parent))) return
            val successIntent = Intent("OVERLAY_EXPORTED")
            successIntent.putExtra("package_name", targetPackage)
            context.sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting overlay: ${e.message}", e)
        }
    }
    
    private fun importOverlay(importPath: String) {
        try {
            if (!ShizukuBridge.isReady()) return
            if (!SecurityUtils.isValidFilePath(importPath, listOf(context.filesDir.parent, context.cacheDir.parent))) return
            
            val installSuccess = ShizukuBridge.installApk(importPath)
            if (installSuccess) {
                val successIntent = Intent("OVERLAY_IMPORTED")
                successIntent.putExtra("import_path", importPath)
                context.sendBroadcast(successIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error importing overlay: ${e.message}", e)
        }
    }
}
