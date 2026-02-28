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
    private const val TAG = "OverlayManager"

    private fun useEnhancedApi(context: Context): Boolean {
        val prefsManager = PrefsManager.getInstance(context)
        return prefsManager.preferShizukuPlus && ShizukuPlusAPI.isEnhancedApiSupported()
    }

    fun activateOverlay(context: Context, packageName: String, apkPath: String, validateSignature: Boolean = true): Boolean {
        try {
            if (validateSignature && !SecurityUtils.validateApkSignature(apkPath)) {
                Log.e(TAG, "APK signature validation failed: $apkPath")
                return false
            }

            val installSuccess = if (useEnhancedApi(context)) {
                try { ShizukuPlusAPI.PackageManager.installPackage(apkPath); true } catch (e: Exception) { false }
            } else {
                ShizukuBridge.installApk(apkPath)
            }

            if (!installSuccess) return false

            val enableSuccess = if (useEnhancedApi(context)) {
                try { ShizukuPlusAPI.OverlayManager.enableOverlay(packageName); true } catch (e: Exception) { false }
            } else {
                ShizukuBridge.executeOverlayCommand(packageName, "enable")
            }

            if (enableSuccess) {
                if (useEnhancedApi(context)) {
                    ShizukuPlusAPI.Shell.executeCommand("cmd overlay set-priority $packageName 100")
                } else {
                    ShizukuBridge.executeOverlayCommand(packageName, "set-priority")
                }
                refreshSystemUI(context)
                return true
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error activating overlay", e)
            return false
        }
    }

    fun deactivateOverlay(context: Context, packageName: String): Boolean {
        try {
            val success = if (useEnhancedApi(context)) {
                try { ShizukuPlusAPI.OverlayManager.disableOverlay(packageName); true } catch (e: Exception) { false }
            } else {
                ShizukuBridge.executeOverlayCommand(packageName, "disable")
            }
            if (success) refreshSystemUI(context)
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Error deactivating overlay", e)
            return false
        }
    }

    fun refreshSystemUI(context: Context) {
        try {
            if (useEnhancedApi(context)) {
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

    fun applyTheme(context: Context, themeData: ByteArray, themeName: String) {
        try {
            val tempFile = File(context.cacheDir, "$themeName.apk")
            tempFile.writeBytes(themeData)
            activateOverlay(context, "com.hexodus.theme.$themeName", tempFile.absolutePath, false)
        } catch (e: Exception) {
            Log.e(TAG, "Error applying theme", e)
        }
    }
}
