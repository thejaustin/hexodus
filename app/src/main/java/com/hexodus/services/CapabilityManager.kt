package com.hexodus.services

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.io.File
import rikka.shizuku.Shizuku
import android.content.pm.PackageManager
import moe.shizuku.plus.ShizukuPlusAPI

/**
 * CapabilityManager - Detects system capabilities (Root, Shizuku, Shizuku+, Dhizuku, ADB, LSPatch)
 * and hardware info to filter compatible features.
 */
class CapabilityManager(private val context: Context) {

    companion object {
        private const val TAG = "CapabilityManager"
    }

    data class DeviceCapabilities(
        val isRooted: Boolean,
        val isShizukuReady: Boolean,
        val isShizukuPlusReady: Boolean,
        val isDhizukuReady: Boolean,
        val isADBEnabled: Boolean,
        val isXposedActive: Boolean,
        val isVectorActive: Boolean,
        val isS22Ultra: Boolean,
        val androidVersion: Int
    )

    /**
     * Perform a full sweep of the system to detect what we can do.
     */
    fun detectCapabilities(): DeviceCapabilities {
        val isShizukuReady = try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }

        val isShizukuPlusReady = try {
            isShizukuReady && ShizukuPlusAPI.isEnhancedApiSupported()
        } catch (e: Exception) {
            false
        }

        val caps = DeviceCapabilities(
            isRooted = checkRootMethod1() || checkRootMethod2(),
            isShizukuReady = isShizukuReady,
            isShizukuPlusReady = isShizukuPlusReady,
            isDhizukuReady = checkDhizuku(isShizukuPlusReady),
            isADBEnabled = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) > 0,
            isXposedActive = checkXposed(),
            isVectorActive = checkVector(),
            isS22Ultra = Build.MODEL.uppercase().contains("S908"),
            androidVersion = Build.VERSION.SDK_INT
        )
        
        Log.d(TAG, "System Capabilities Detected: $caps")
        return caps
    }

    private fun checkVector(): Boolean {
        return try {
            // Vector framework detection - checking for the core service class or manager package
            Class.forName("org.jingmatrix.vector.VectorBridge")
            true
        } catch (e: Exception) {
            // Also check for the manager app
            val intent = context.packageManager.getLaunchIntentForPackage("org.jingmatrix.vector")
            intent != null
        }
    }

    private fun checkRootMethod1(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun checkRootMethod2(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = process.inputStream.bufferedReader()
            reader.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    private fun checkDhizuku(isShizukuPlusReady: Boolean): Boolean {
        // First check for native Shizuku+ Dhizuku borrowing
        if (isShizukuPlusReady) {
            try {
                if (ShizukuPlusAPI.Dhizuku.isAvailable()) return true
            } catch (e: Exception) {}
        }

        // Fallback to standalone Dhizuku app
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.iamr0s.dhizuku")
            intent != null
        } catch (e: Exception) {
            false
        }
    }

    private fun checkXposed(): Boolean {
        return try {
            // Standard Xposed detection
            Class.forName("de.robv.android.xposed.XposedBridge")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    /**
     * Determines if an app/feature is compatible with the current capabilities.
     */
    fun isCompatible(requirements: List<String>, current: DeviceCapabilities): Boolean {
        if (requirements.isEmpty()) return true
        
        val reqs = requirements.map { it.uppercase() }
        
        if (reqs.contains("SAMSUNG") && !Build.MANUFACTURER.lowercase().contains("samsung")) return false
        if (reqs.contains("S22ULTRA") && !current.isS22Ultra) return false

        // Check permission levels
        var hasBaseRequirement = false
        if (reqs.contains("ROOT") && current.isRooted) hasBaseRequirement = true
        if (reqs.contains("SHIZUKU") && current.isShizukuReady) hasBaseRequirement = true
        if (reqs.contains("SHIZUKU+") && current.isShizukuPlusReady) hasBaseRequirement = true
        if (reqs.contains("DHIZUKU") && current.isDhizukuReady) hasBaseRequirement = true
        if (reqs.contains("LSPATCH") || reqs.contains("XPOSED")) {
            if (current.isXposedActive || current.isShizukuReady) hasBaseRequirement = true
        }
        if (reqs.contains("VECTOR") && current.isVectorActive) hasBaseRequirement = true
        if (reqs.contains("ADB") && current.isADBEnabled) hasBaseRequirement = true

        // If no permission tags are found, assume it's a general app
        val permissionTags = listOf("ROOT", "SHIZUKU", "SHIZUKU+", "DHIZUKU", "ADB", "LSPATCH", "XPOSED", "VECTOR")
        if (reqs.none { it in permissionTags }) hasBaseRequirement = true

        return hasBaseRequirement
    }
}
