package com.hexodus.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.hexodus.utils.SecurityUtils
import com.hexodus.HexodusApplication

/**
 * ModExtensionManager - Handles loading and interaction with third-party Hexodus mods
 */
object ModExtensionManager {
    private val context: android.content.Context get() = com.hexodus.HexodusApplication.context

    private const val TAG = "ModExtensionManager"
    private const val MOD_PERMISSION = "com.hexodus.permission.MOD_EXTENSION"
    private const val ACTION_MOD_SERVICE = "com.hexodus.action.MOD_SERVICE"

    data class ModExtension(
        val packageName: String,
        val name: String,
        val version: String,
        val author: String,
        val isVerified: Boolean
    )

    fun discoverMods(): List<ModExtension> {
        val mods = mutableListOf<ModExtension>()
        val packageManager = context.packageManager
        
        try {
            val intent = Intent(ACTION_MOD_SERVICE)
            val services = packageManager.queryIntentServices(intent, PackageManager.GET_META_DATA)
            
            for (service in services) {
                val serviceInfo = service.serviceInfo
                val packageName = serviceInfo.packageName
                
                if (packageManager.checkPermission(MOD_PERMISSION, packageName) == PackageManager.PERMISSION_GRANTED) {
                    val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                    val appName = appInfo.loadLabel(packageManager).toString()
                    val metaData = serviceInfo.metaData
                    
                    val modVersion = metaData?.getString("mod_version") ?: "1.0.0"
                    val modAuthor = metaData?.getString("mod_author") ?: "Unknown"
                    val isVerified = SecurityUtils.isPackageSafe(context, packageName)
                    
                    mods.add(ModExtension(packageName, appName, modVersion, modAuthor, isVerified))
                    Log.d(TAG, "Discovered mod: $appName ($packageName) by $modAuthor")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error discovering mods: ${e.message}")
        }
        
        return mods
    }

    fun executeModCommand(packageName: String, command: String): Boolean {
        try {
            if (!SecurityUtils.isPackageSafe(context, packageName)) {
                Log.e(TAG, "Blocked command execution for unsafe mod: $packageName")
                return false
            }
            
            val intent = Intent(ACTION_MOD_SERVICE).apply {
                setPackage(packageName)
                putExtra("command", command)
            }
            context.startService(intent)
            Log.d(TAG, "Executed command '$command' on mod '$packageName'")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute command on mod '$packageName': ${e.message}")
            return false
        }
    }
}
