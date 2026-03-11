package com.hexodus.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import java.io.IOException
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import rikka.shizuku.ShizukuPlusAPI

/**
 * ShizukuBridge - Enhanced bridge for Shizuku communication
 * Handles privileged operations through Shizuku's API with additional security measures.
 * Refactored from Service to Singleton for stability and easier access.
 * Now leverages ShizukuPlusAPI for enhanced functionality and Dhizuku support.
 */
object ShizukuBridge {
    
    private const val TAG = "ShizukuBridge"
    private const val REQUEST_CODE_PERMISSION = 1001
    
    /**
     * Checks if Shizuku is ready for use
     */
    fun isReady(): Boolean {
        return try {
            if (Shizuku.isPreV11()) {
                Log.w(TAG, "Shizuku version is too old")
                return false
            }
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED &&
            Shizuku.pingBinder()
        } catch (e: Exception) {
            Log.w(TAG, "Shizuku not ready: ${e.message}")
            false
        }
    }

    /**
     * Checks if Shizuku+ Enhanced API is supported
     */
    fun isEnhancedApiSupported(): Boolean {
        return isReady() && ShizukuPlusAPI.isEnhancedApiSupported()
    }

    /**
     * Checks if Dhizuku (Device Owner) mode is available
     */
    fun isDhizukuAvailable(): Boolean {
        return isReady() && ShizukuPlusAPI.Dhizuku.isAvailable()
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
            val result = ShizukuPlusAPI.Shell.executeCommand(command)
            
            Log.d(TAG, "Privileged command executed: $command, Exit code: ${result.exitCode}")
            
            if (result.isSuccess) {
                result.output
            } else {
                Log.w(TAG, "Command failed with exit code ${result.exitCode}: ${result.error}")
                if (result.output.isNotEmpty()) result.output else result.error
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing shell command: ${e.message}", e)
            null
        }
    }

    /**
     * Executes overlay-related commands through Shizuku
     */
    fun executeOverlayCommand(packageName: String, action: String): Boolean {
        if (!isReady()) return false
        if (!isValidPackageName(packageName)) return false

        return try {
            when (action) {
                "enable" -> ShizukuPlusAPI.OverlayManager.enableOverlay(packageName)
                "disable" -> ShizukuPlusAPI.OverlayManager.disableOverlay(packageName)
                "set-priority" -> ShizukuPlusAPI.OverlayManager.setPriority(packageName, "android")
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing overlay command", e)
            false
        }
    }
    
    /**
     * Installs an APK using Shizuku
     */
    fun installApk(apkPath: String): Boolean {
        if (!isReady()) return false
        if (!isValidFilePath(apkPath)) return false

        return try {
            ShizukuPlusAPI.PackageManager.installPackage(apkPath)
        } catch (e: Exception) {
            Log.e(TAG, "Error installing APK", e)
            false
        }
    }
    
    /**
     * Uninstalls an APK using Shizuku
     */
    fun uninstallPackage(packageName: String): Boolean {
        if (!isReady()) return false
        if (!isValidPackageName(packageName)) return false

        return try {
            ShizukuPlusAPI.PackageManager.uninstallPackage(packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Error uninstalling package", e)
            false
        }
    }
    
    /**
     * Gets list of overlay packages
     */
    fun getOverlayPackages(): List<String> {
        if (!isReady()) return emptyList()

        return try {
            val result = ShizukuPlusAPI.Shell.executeCommand("cmd overlay list")
            if (!result.isSuccess) return emptyList()

            result.output.lines()
                .filter { it.contains("ENABLED") }
                .filter { it.contains(":") }
                .map { line ->
                    line.substringAfterLast(":").trim()
                }
                .filter { it.isNotEmpty() && isValidPackageName(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting overlay packages", e)
            emptyList()
        }
    }

    /**
     * System Settings access via ShizukuPlusAPI
     */
    object Settings {
        fun putSystem(key: String, value: String): Boolean = ShizukuPlusAPI.Settings.putSystem(key, value)
        fun putSecure(key: String, value: String): Boolean = ShizukuPlusAPI.Settings.putSecure(key, value)
        fun putGlobal(key: String, value: String): Boolean = ShizukuPlusAPI.Settings.putGlobal(key, value)
        
        fun getSystem(key: String): String = ShizukuPlusAPI.Settings.getSystem(key)
        fun getSecure(key: String): String = ShizukuPlusAPI.Settings.getSecure(key)
        fun getGlobal(key: String): String = ShizukuPlusAPI.Settings.getGlobal(key)
    }

    /**
     * System Properties access via ShizukuPlusAPI
     */
    object Properties {
        fun get(key: String): String = ShizukuPlusAPI.SystemProperties.get(key)
        fun list(): String = ShizukuPlusAPI.SystemProperties.list()
    }

    /**
     * App Management operations via ShizukuPlusAPI
     */
    object AppManagement {
        fun freeze(packageName: String): Boolean = ShizukuPlusAPI.PackageManager.clearPackageData(packageName) // Placeholder for actual freeze if API supports it
        
        fun forceStop(packageName: String): Boolean {
            return ShizukuPlusAPI.Shell.executeCommand("am force-stop $packageName").isSuccess
        }

        fun hide(packageName: String): Boolean {
            return ShizukuPlusAPI.Shell.executeCommand("pm hide $packageName").isSuccess
        }

        fun unhide(packageName: String): Boolean {
            return ShizukuPlusAPI.Shell.executeCommand("pm unhide $packageName").isSuccess
        }
    }

    private fun isValidCommand(command: String): Boolean {
        val allowedCommands = listOf(
            "cmd overlay", "pm install", "pm uninstall", "cmd settings",
            "settings put", "settings get", "am start", "appops set",
            "dumpsys battery", "setprop", "device_config put"
        )
        val isAllowedPrefix = allowedCommands.any { command.startsWith(it) }
        val hasDangerousChars = command.contains(Regex("""[;&|><\n\r]"""))
        val hasPathTraversal = command.contains("../") || command.contains("..\\")
        val hasCommandSubstitution = command.contains("\${") || command.contains("`")

        return isAllowedPrefix && !hasDangerousChars && !hasPathTraversal && !hasCommandSubstitution
    }

    private fun isValidFilePath(path: String): Boolean {
        if (path.contains("../") || path.contains("..\\") || path.contains("\u0000")) return false
        val allowedPaths = listOf("/data/local/tmp/", "/sdcard/", "/storage/emulated/0/", "/data/user/0/")
        return allowedPaths.any { path.startsWith(it) }
    }

    private fun isValidPackageName(packageName: String): Boolean {
        return Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*$").matches(packageName)
    }
}

