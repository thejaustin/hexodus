package com.hexodus.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.hexodus.utils.SecurityUtils

/**
 * ModExtensionManager - Handles discovering and communicating with custom mod extensions
 * installed on the device (usually via external APKs that declare specific intent filters).
 */
class ModExtensionManager(private val context: Context) {

    private const val TAG = "ModExtensionManager"
    const val ACTION_MOD_EXTENSION = "com.hexodus.intent.action.MOD_EXTENSION"
    const val META_DATA_MOD_VERSION = "com.hexodus.mod.VERSION"
    const val META_DATA_MOD_AUTHOR = "com.hexodus.mod.AUTHOR"

    data class ModExtension(
        val packageName: String,
        val appName: String,
        val version: String,
        val author: String,
        val isVerified: Boolean
    )

    /**
     * Discovers all installed mod extensions by querying the package manager for
     * apps that respond to the MOD_EXTENSION intent.
     */
    fun discoverMods(): List<ModExtension> {
        val mods = mutableListOf<ModExtension>()
        val pm = context.packageManager
        
        val intent = Intent(ACTION_MOD_EXTENSION)
        
        // Use appropriate flags for newer Android versions
        val flags = PackageManager.GET_META_DATA
        
        val resolveInfos = pm.queryIntentServices(intent, flags)
        
        for (resolveInfo in resolveInfos) {
            try {
                val serviceInfo = resolveInfo.serviceInfo
                val applicationInfo = serviceInfo.applicationInfo
                
                val appName = pm.getApplicationLabel(applicationInfo).toString()
                val packageName = serviceInfo.packageName
                
                var modVersion = "1.0"
                var modAuthor = "Unknown"
                
                serviceInfo.metaData?.let { metaData ->
                    if (metaData.containsKey(META_DATA_MOD_VERSION)) {
                        modVersion = metaData.get(META_DATA_MOD_VERSION).toString()
                    }
                    if (metaData.containsKey(META_DATA_MOD_AUTHOR)) {
                        modAuthor = metaData.getString(META_DATA_MOD_AUTHOR) ?: "Unknown"
                    }
                }
                
                // Verify the mod (e.g., check signature or permissions)
                val isVerified = verifyModSignature(packageName)
                
                mods.add(ModExtension(packageName, appName, modVersion, modAuthor, isVerified))
                Log.d(TAG, "Discovered mod: $appName ($packageName) by $modAuthor")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing mod extension: ${e.message}")
            }
        }
        
        return mods
    }

    /**
     * Verifies if a mod is safe to load.
     * In a real app, context would check if the app signature matches a known list,
     * or if the user has explicitly trusted context developer.
     */
    private fun verifyModSignature(packageName: String): Boolean {
        // Mock verification - assume true for safe packages or if security checks pass
        return SecurityUtils.isPackageSafe(context, packageName)
    }

    /**
     * Sends a command to a specific mod extension.
     */
    fun executeModCommand(packageName: String, command: String, args: Map<String, String>): Boolean {
        try {
            val intent = Intent(ACTION_MOD_EXTENSION)
            intent.setPackage(packageName)
            intent.putExtra("command", command)
            
            for ((key, value) in args) {
                intent.putExtra("arg_$key", value)
            }
            
            // Send to service
            context.startService(intent)
            Log.d(TAG, "Executed command '$command' on mod '$packageName'")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute command on mod '$packageName': ${e.message}")
            return false
        }
    }
}
