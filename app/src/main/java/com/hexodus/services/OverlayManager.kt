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
    private val context get() = com.hexodus.HexodusApplication.context
    private const val TAG = "OverlayManager"

    private fun useEnhancedApi(): Boolean {
        val prefsManager = PrefsManager.getInstance(context)
        return prefsManager.preferShizukuPlus && ShizukuPlusAPI.isEnhancedApiSupported()
    }

    fun activateOverlay(targetPackage: String, apkPath: String, validateSignature: Boolean = true): Boolean {
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
                ShizukuPlusAPI.OverlayManager.enableOverlay(targetPackage)
            } else {
                ShizukuBridge.executeOverlayCommand(targetPackage, "enable")
            }

            if (enableSuccess) {
                if (useEnhancedApi()) {
                    ShizukuPlusAPI.Shell.executeCommand("cmd overlay set-priority $targetPackage 100")
                } else {
                    ShizukuBridge.executeOverlayCommand(targetPackage, "set-priority")
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

    fun deactivateOverlay(targetPackage: String): Boolean {
        try {
            val success = if (useEnhancedApi()) {
                ShizukuPlusAPI.OverlayManager.disableOverlay(targetPackage)
            } else {
                ShizukuBridge.executeOverlayCommand(targetPackage, "disable")
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
