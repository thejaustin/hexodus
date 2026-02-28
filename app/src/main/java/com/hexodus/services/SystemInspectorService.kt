package com.hexodus.services
import com.hexodus.HexodusApplication

import android.app.Service

import android.content.Intent
import android.util.Log
import com.hexodus.utils.SecurityUtils
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import java.io.File

/**
 * SystemInspectorService - Service for system resource inspection and management
 * Inspired by LibChecker and other system inspection projects from awesome-shizuku
 */
object SystemInspectorService {
    private val context get() = com.hexodus.HexodusApplication.context
    private val pm: PackageManager by lazy { context.packageManager }
    
    private const val TAG = "SystemInspectorService"
    private const val ACTION_GET_APP_LIBRARIES = "com.hexodus.GET_APP_LIBRARIES"
    private const val ACTION_GET_SYSTEM_PROPERTIES = "com.hexodus.GET_SYSTEM_PROPERTIES"
    private const val ACTION_GET_APP_RESOURCES = "com.hexodus.GET_APP_RESOURCES"
    private const val ACTION_GET_INSTALLATION_SOURCE = "com.hexodus.GET_INSTALLATION_SOURCE"
    private const val ACTION_GET_APP_ABI = "com.hexodus.GET_APP_ABI"
    private const val ACTION_GET_SYSTEM_HEALTH = "com.hexodus.GET_SYSTEM_HEALTH"
    
    // Intent extras
    const val EXTRA_PACKAGE_NAME = "package_name"
    const val EXTRA_PROPERTY_NAME = "property_name"
    const val EXTRA_RESOURCE_TYPE = "resource_type" // drawable, string, color, layout
    const val EXTRA_INSPECTION_SCOPE = "inspection_scope" // full, libraries, permissions
    
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_GET_APP_LIBRARIES -> {
                val targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                if (!targetPackage.isNullOrEmpty()) {
                    getAppLibraries(targetPackage)
                }
            }
            ACTION_GET_SYSTEM_PROPERTIES -> {
                val propertyName = intent.getStringExtra(EXTRA_PROPERTY_NAME)
                if (!propertyName.isNullOrEmpty()) {
                    getSystemProperty(propertyName)
                } else {
                    getAllSystemProperties()
                }
            }
            ACTION_GET_APP_RESOURCES -> {
                val targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val resourceType = intent.getStringExtra(EXTRA_RESOURCE_TYPE) ?: "all"
                if (!targetPackage.isNullOrEmpty()) {
                    getAppResources(targetPackage, resourceType)
                }
            }
            ACTION_GET_INSTALLATION_SOURCE -> {
                val targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                if (!targetPackage.isNullOrEmpty()) {
                    getInstallationSource(targetPackage)
                }
            }
            ACTION_GET_APP_ABI -> {
                val targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                if (!targetPackage.isNullOrEmpty()) {
                    getAppAbi(targetPackage)
                }
            }
            ACTION_GET_SYSTEM_HEALTH -> {
                getSystemHealth()
            }
        }
        
        return android.app.Service.START_STICKY
    }
    
    private fun getAppLibraries(targetPackage: String) {
        try {
            if (!ShizukuBridge.isReady()) return
            val sanitized = SecurityUtils.sanitizePackageName(targetPackage)
            
            Log.d(TAG, "Retrieved libraries for: $sanitized")
            val successIntent = Intent("APP_LIBRARIES_RETRIEVED")
            successIntent.putExtra("package_name", sanitized)
            context.sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app libraries: ${e.message}", e)
        }
    }
    
    private fun getSystemProperty(propertyName: String) {
        try {
            if (!ShizukuBridge.isReady()) return
            val result = ShizukuBridge.executeShellCommand("getprop $propertyName")
            if (result != null) {
                val successIntent = Intent("SYSTEM_PROPERTY_RETRIEVED")
                successIntent.putExtra("property_name", propertyName)
                successIntent.putExtra("property_value", result.trim())
                context.sendBroadcast(successIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting system property: ${e.message}", e)
        }
    }
    
    private fun getAllSystemProperties() {
        try {
            if (!ShizukuBridge.isReady()) return
            val result = ShizukuBridge.executeShellCommand("getprop")
            if (result != null) {
                val successIntent = Intent("ALL_SYSTEM_PROPERTIES_RETRIEVED")
                context.sendBroadcast(successIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all system properties: ${e.message}", e)
        }
    }
    
    private fun getAppResources(targetPackage: String, resourceType: String) {
        try {
            if (!ShizukuBridge.isReady()) return
            val sanitized = SecurityUtils.sanitizePackageName(targetPackage)
            Log.d(TAG, "Retrieved resources for: $sanitized")
            val successIntent = Intent("APP_RESOURCES_RETRIEVED")
            successIntent.putExtra("package_name", sanitized)
            context.sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app resources: ${e.message}", e)
        }
    }
    
    private fun getInstallationSource(targetPackage: String) {
        try {
            if (!ShizukuBridge.isReady()) return
            val sanitized = SecurityUtils.sanitizePackageName(targetPackage)
            val result = ShizukuBridge.executeShellCommand("pm dump $sanitized | grep -i install")
            val source = if (result?.contains("vending") == true) "Play Store" else "Unknown"
            
            val successIntent = Intent("INSTALLATION_SOURCE_RETRIEVED")
            successIntent.putExtra("package_name", sanitized)
            successIntent.putExtra("source", source)
            context.sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installation source: ${e.message}", e)
        }
    }
    
    private fun getAppAbi(targetPackage: String) {
        try {
            if (!ShizukuBridge.isReady()) return
            val sanitized = SecurityUtils.sanitizePackageName(targetPackage)
            val successIntent = Intent("APP_ABI_RETRIEVED")
            successIntent.putExtra("package_name", sanitized)
            context.sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app ABI: ${e.message}", e)
        }
    }
    
    private fun getSystemHealth() {
        try {
            if (!ShizukuBridge.isReady()) return
            val successIntent = Intent("SYSTEM_HEALTH_RETRIEVED")
            context.sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting system health: ${e.message}", e)
        }
    }
    
    fun getAppDetails(targetPackage: String): Map<String, Any>? {
        try {
            if (!ShizukuBridge.isReady()) return null
            val sanitized = SecurityUtils.sanitizePackageName(targetPackage)
            val appInfo = pm.getApplicationInfo(sanitized, PackageManager.GET_META_DATA)
            val packageInfo = pm.getPackageInfo(sanitized, PackageManager.GET_PERMISSIONS)
            
            return mutableMapOf<String, Any>().apply {
                put("package_name", sanitized)
                put("app_name", appInfo.loadLabel(pm).toString())
                put("version_name", packageInfo.versionName ?: "Unknown")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app details: ${e.message}", e)
            return null
        }
    }
    
    fun getAllInstalledApps(): List<Map<String, Any>> {
        try {
            if (!ShizukuBridge.isReady()) return emptyList()
            val apps = mutableListOf<Map<String, Any>>()
            val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            for (pkgInfo in packages) {
                apps.add(mapOf("package_name" to pkgInfo.packageName))
            }
            return apps
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed apps: ${e.message}", e)
            return emptyList()
        }
    }
}
