package com.hexodus.services

import android.content.Context
import android.content.Intent
import android.util.Log
import com.hexodus.utils.SecurityUtils
import com.hexodus.utils.PrefsManager
import moe.shizuku.plus.ShizukuPlusAPI
import java.io.File

/**
 * OverlayManager - Singleton utility for managing overlays via Shizuku
 */
object OverlayManager {
    private val context: android.content.Context get() = com.hexodus.HexodusApplication.context
    private val packageName_: String get() = context.packageName
    private val cacheDir_: java.io.File get() = context.cacheDir
    private val filesDir_: java.io.File get() = context.filesDir
    private val resources_: android.content.res.Resources get() = context.resources
    
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

    
    
    
    
    
    
    
    

    
    
    
    
    
    
    

    private const val TAG = "OverlayManager"

    private fun useEnhancedApi(): Boolean {
        val prefsManager = PrefsManager.getInstance(context)
        return prefsManager.preferShizukuPlus && ShizukuPlusAPI.isEnhancedApiSupported()
    }

    fun activateOverlay(packageName: String, apkPath: String, validateSignature: Boolean = true): Boolean {
        try {
            if (validateSignature && !SecurityUtils.validateApkSignature(apkPath)) {
                Log.e(TAG, "APK signature validation failed: $apkPath")
                return false
            }

            val installSuccess = if (useEnhancedApi()) {
                ShizukuPlusAPI.PackageManager.installPackage(apkPath)
            } else {
                ShizukuBridge.installApk(apkPath)
            }

            if (!installSuccess) return false

            val enableSuccess = if (useEnhancedApi()) {
                ShizukuPlusAPI.OverlayManager.enableOverlay(packageName)
            } else {
                ShizukuBridge.executeOverlayCommand(packageName, "enable")
            }

            if (enableSuccess) {
                if (useEnhancedApi()) {
                    ShizukuPlusAPI.Shell.executeCommand("cmd overlay set-priority $packageName 100")
                } else {
                    ShizukuBridge.executeOverlayCommand(packageName, "set-priority")
                }
                refreshSystemUI()
                return true
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error activating overlay", e)
            return false
        }
    }

    fun deactivateOverlay(packageName: String): Boolean {
        try {
            val success = if (useEnhancedApi()) {
                ShizukuPlusAPI.OverlayManager.disableOverlay(packageName)
            } else {
                ShizukuBridge.executeOverlayCommand(packageName, "disable")
            }
            if (success) refreshSystemUI()
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Error deactivating overlay", e)
            return false
        }
    }

    fun refreshSystemUI() {
        try {
            if (useEnhancedApi()) {
                ShizukuPlusAPI.Shell.executeCommand("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS")
                ShizukuPlusAPI.Shell.executeCommand("killall com.android.systemui")
            } else {
                ShizukuBridge.executeShellCommand("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS")
                ShizukuBridge.executeShellCommand("killall com.android.systemui")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing system UI", e)
        }
    }

    fun applyTheme(themeData: ByteArray, themeName: String) {
        try {
            val tempFile = File(context.cacheDir, "$themeName.apk")
            tempFile.writeBytes(themeData)
            activateOverlay("com.hexodus.theme.$themeName", tempFile.absolutePath, false)
        } catch (e: Exception) {
            Log.e(TAG, "Error applying theme", e)
        }
    }
}
