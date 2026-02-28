package com.hexodus.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import java.io.IOException
import rikka.shizuku.Shizuku

/**
 * ShizukuBridge - Enhanced bridge for Shizuku communication
 * Handles privileged operations through Shizuku's API with additional security measures.
 * Refactored from Service to Singleton for stability and easier access.
 */
object ShizukuBridge {
    private val context: android.content.Context get() = com.hexodus.HexodusApplication.context
    private val packageName: String get() = context.packageName
    private val cacheDir: java.io.File get() = context.cacheDir
    private val filesDir: java.io.File get() = context.filesDir
    private val contentResolver: android.content.ContentResolver get() = context.contentResolver
    private val packageManager: android.content.pm.PackageManager get() = context.packageManager
    private val applicationContext: android.content.Context get() = context

    

    
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
            // Execute the command using Shizuku's privileged shell access.
            // Using the public API in Shizuku 13+
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val output = process.inputStream.bufferedReader().readText()
            val errorOutput = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            Log.d(TAG, "Privileged command executed: $command, Exit code: $exitCode")
            
            if (exitCode == 0) output else {
                Log.w(TAG, "Command failed with exit code $exitCode: $errorOutput")
                if (output.isNotEmpty()) output else errorOutput
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception executing shell command: ${e.message}", e)
            null
        } catch (e: IOException) {
            Log.e(TAG, "IO exception executing shell command: ${e.message}", e)
            null
        } catch (e: InterruptedException) {
            Log.e(TAG, "Command execution interrupted: ${e.message}", e)
            Thread.currentThread().interrupt()
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error executing shell command: ${e.message}", e)
            null
        }
    }

    /**
     * Executes overlay-related commands through Shizuku
     */
    fun executeOverlayCommand(packageName: String, action: String): Boolean {
        if (!isReady()) return false

        if (!isValidPackageName(packageName)) return false

        val validActions = listOf("enable", "disable", "set-priority")
        if (action !in validActions) return false

        val escapedPackage = packageName.replace("'", "'\\''")

        return try {
            val command = when (action) {
                "enable" -> "cmd overlay enable $escapedPackage"
                "disable" -> "cmd overlay disable $escapedPackage"
                "set-priority" -> "cmd overlay set-priority $escapedPackage 100"
                else -> return false
            }

            val result = executeShellCommand(command)
            result != null
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

        val escapedPath = apkPath.replace("'", "'\\''")

        return try {
            val command = "pm install -r -d -t '$escapedPath'"
            val result = executeShellCommand(command)
            result?.contains("Success", ignoreCase = true) == true
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

        val escapedPackage = packageName.replace("'", "'\\''")

        return try {
            val command = "pm uninstall '$escapedPackage'"
            val result = executeShellCommand(command)
            result?.contains("Success", ignoreCase = true) == true
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
            val command = "cmd overlay list"
            val output = executeShellCommand(command) ?: ""

            output.lines()
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
