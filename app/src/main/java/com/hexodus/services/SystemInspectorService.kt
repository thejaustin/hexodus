package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.utils.SecurityUtils
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import java.io.File

/**
 * SystemInspectorService - Service for system resource inspection and management
 * Inspired by LibChecker and other system inspection projects from awesome-shizuku
 */
class SystemInspectorService : Service() {
    
    companion object {
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
    }
    
    private lateinit var shizukuBridgeService: ShizukuBridgeService
    private val pm: PackageManager by lazy { applicationContext.packageManager }
    
    override fun onCreate() {
        super.onCreate()
        shizukuBridgeService = ShizukuBridgeService()
        Log.d(TAG, "SystemInspectorService created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_GET_APP_LIBRARIES -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                
                if (!packageName.isNullOrEmpty()) {
                    getAppLibraries(packageName)
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
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val resourceType = intent.getStringExtra(EXTRA_RESOURCE_TYPE) ?: "all"
                
                if (!packageName.isNullOrEmpty()) {
                    getAppResources(packageName, resourceType)
                }
            }
            ACTION_GET_INSTALLATION_SOURCE -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                
                if (!packageName.isNullOrEmpty()) {
                    getInstallationSource(packageName)
                }
            }
            ACTION_GET_APP_ABI -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                
                if (!packageName.isNullOrEmpty()) {
                    getAppAbi(packageName)
                }
            }
            ACTION_GET_SYSTEM_HEALTH -> {
                getSystemHealth()
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Gets libraries used by an app using Shizuku
     */
    private fun getAppLibraries(packageName: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            // In a real implementation, this would inspect the app's libraries
            // For this example, we'll simulate the process
            val libraries = listOf(
                mapOf(
                    "name" to "okhttp",
                    "version" to "4.9.3",
                    "type" to "networking",
                    "license" to "Apache 2.0"
                ),
                mapOf(
                    "name" to "retrofit",
                    "version" to "2.9.0",
                    "type" to "networking",
                    "license" to "Apache 2.0"
                ),
                mapOf(
                    "name" to "gson",
                    "version" to "2.8.9",
                    "type" to "serialization",
                    "license" to "Apache 2.0"
                ),
                mapOf(
                    "name" to "material",
                    "version" to "1.4.0",
                    "type" to "ui",
                    "license" to "Apache 2.0"
                )
            )
            
            Log.d(TAG, "Retrieved libraries for: $sanitizedPackageName (${libraries.size} libraries)")
            
            // Broadcast results
            val successIntent = Intent("APP_LIBRARIES_RETRIEVED")
            successIntent.putExtra("package_name", sanitizedPackageName)
            successIntent.putExtra("library_count", libraries.size)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app libraries: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_LIBRARIES_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets system properties using Shizuku
     */
    private fun getSystemProperty(propertyName: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate property name
            if (SecurityUtils.containsDangerousChars(propertyName)) {
                Log.e(TAG, "Dangerous characters detected in property name")
                return
            }
            
            val command = "getprop $propertyName"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Retrieved system property: $propertyName = $result")
                
                // Broadcast success
                val successIntent = Intent("SYSTEM_PROPERTY_RETRIEVED")
                successIntent.putExtra("property_name", propertyName)
                successIntent.putExtra("property_value", result.trim())
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to get system property: $propertyName")
                
                // Broadcast failure
                val failureIntent = Intent("SYSTEM_PROPERTY_ERROR")
                failureIntent.putExtra("property_name", propertyName)
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting system property: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("SYSTEM_PROPERTY_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets all system properties
     */
    private fun getAllSystemProperties() {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            val command = "getprop"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                // Parse properties
                val properties = result.lines()
                    .filter { line -> line.startsWith("[") && line.contains("]: [") }
                    .mapNotNull { line ->
                        try {
                            val key = line.substringAfter("[").substringBefore("]")
                            val value = line.substringAfter("]: [").substringBefore("]")
                            key to value
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .toMap()
                
                Log.d(TAG, "Retrieved ${properties.size} system properties")
                
                // Broadcast results
                val successIntent = Intent("ALL_SYSTEM_PROPERTIES_RETRIEVED")
                successIntent.putExtra("property_count", properties.size)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to get all system properties")
                
                // Broadcast failure
                val failureIntent = Intent("ALL_SYSTEM_PROPERTIES_ERROR")
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all system properties: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("ALL_SYSTEM_PROPERTIES_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets resources from an app using Shizuku
     */
    private fun getAppResources(packageName: String, resourceType: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            val validResourceTypes = listOf("drawable", "string", "color", "layout", "all")
            if (resourceType !in validResourceTypes) {
                Log.e(TAG, "Invalid resource type: $resourceType")
                return
            }
            
            // In a real implementation, this would inspect the app's resources
            // For this example, we'll simulate the process
            val resources = when (resourceType) {
                "drawable" -> listOf("ic_launcher", "ic_menu", "bg_splash")
                "string" -> listOf("app_name", "title_activity_main", "menu_settings")
                "color" -> listOf("primary_color", "accent_color", "background_color")
                "layout" -> listOf("activity_main", "fragment_settings", "dialog_confirmation")
                else -> listOf("ic_launcher", "app_name", "primary_color", "activity_main")
            }.map { resourceName ->
                mapOf(
                    "name" to resourceName,
                    "type" to when (resourceType) {
                        "drawable" -> "drawable"
                        "string" -> "string"
                        "color" -> "color"
                        "layout" -> "layout"
                        else -> "mixed"
                    },
                    "size" to (1024L..102400L).random() // Random size between 1KB and 100KB
                )
            }
            
            Log.d(TAG, "Retrieved ${resources.size} resources of type '$resourceType' for: $sanitizedPackageName")
            
            // Broadcast results
            val successIntent = Intent("APP_RESOURCES_RETRIEVED")
            successIntent.putExtra("package_name", sanitizedPackageName)
            successIntent.putExtra("resource_type", resourceType)
            successIntent.putExtra("resource_count", resources.size)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app resources: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_RESOURCES_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets the installation source of an app using Shizuku
     */
    private fun getInstallationSource(packageName: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            val command = "pm dump $sanitizedPackageName | grep -i install"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            val source = if (result != null) {
                when {
                    result.contains("com.android.vending") -> "Google Play Store"
                    result.contains("com.amazon.venezia") -> "Amazon Appstore"
                    result.contains("org.fdroid.fdroid") -> "F-Droid"
                    result.contains("com.sec.android.app.samsungapps") -> "Samsung Galaxy Store"
                    else -> "Unknown (${result.trim()})"
                }
            } else {
                "Unknown (command failed)"
            }
            
            Log.d(TAG, "Installation source for $sanitizedPackageName: $source")
            
            // Broadcast results
            val successIntent = Intent("INSTALLATION_SOURCE_RETRIEVED")
            successIntent.putExtra("package_name", sanitizedPackageName)
            successIntent.putExtra("source", source)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installation source: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("INSTALLATION_SOURCE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets the ABI (Application Binary Interface) of an app
     */
    private fun getAppAbi(packageName: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            // In a real implementation, this would query the app's native libraries
            // For this example, we'll simulate the process
            val abiInfo = mapOf(
                "primary_abi" to "arm64-v8a",
                "secondary_abi" to "armeabi-v7a",
                "supported_abis" to listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
            )
            
            Log.d(TAG, "Retrieved ABI info for: $sanitizedPackageName")
            
            // Broadcast results
            val successIntent = Intent("APP_ABI_RETRIEVED")
            successIntent.putExtra("package_name", sanitizedPackageName)
            successIntent.putExtra("abi_info", HashMap(abiInfo))
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app ABI: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("APP_ABI_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets system health information
     */
    private fun getSystemHealth() {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // In a real implementation, this would query various system health metrics
            // For this example, we'll simulate the process
            val systemHealth = mapOf(
                "cpu_usage_percent" to 25.5f,
                "memory_usage_percent" to 45.2f,
                "storage_usage_percent" to 68.7f,
                "battery_level_percent" to 87.0f,
                "temperature_celsius" to 32.5f,
                "system_uptime_minutes" to 1420L,
                "process_count" to 187,
                "running_services_count" to 42
            )
            
            Log.d(TAG, "Retrieved system health information")
            
            // Broadcast results
            val successIntent = Intent("SYSTEM_HEALTH_RETRIEVED")
            successIntent.putExtra("system_health", systemHealth)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting system health: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("SYSTEM_HEALTH_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets detailed app information
     */
    fun getAppDetails(packageName: String): Map<String, Any>? {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return null
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            // Get app info from package manager
            val appInfo = pm.getApplicationInfo(sanitizedPackageName, PackageManager.GET_META_DATA)
            val packageInfo = pm.getPackageInfo(sanitizedPackageName, PackageManager.GET_PERMISSIONS)
            
            val appDetails = mutableMapOf<String, Any>().apply {
                put("package_name", sanitizedPackageName)
                put("app_name", appInfo.loadLabel(pm).toString())
                put("version_name", packageInfo.versionName ?: "Unknown")
                put("version_code", packageInfo.versionCode.toLong())
                put("first_install_time", packageInfo.firstInstallTime)
                put("last_update_time", packageInfo.lastUpdateTime)
                put("target_sdk_version", packageInfo.applicationInfo.targetSdkVersion)
                put("min_sdk_version", packageInfo.applicationInfo.minSdkVersion)
                put("permissions", packageInfo.requestedPermissions?.toList() ?: emptyList<String>())
                put("is_system_app", appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0)
                put("is_user_app", appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0)
                put("data_dir", appInfo.dataDir)
                put("source_dir", appInfo.sourceDir)
            }
            
            // Get additional info using Shizuku
            val sizeInfo = getAppSizeInfo(sanitizedPackageName)
            appDetails.putAll(sizeInfo)
            
            return appDetails
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app details: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Gets app size information using Shizuku
     */
    private fun getAppSizeInfo(packageName: String): Map<String, Long> {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return emptyMap()
            }
            
            // In a real implementation, this would use pm path and du commands to get size
            // For this example, we'll return mock data
            return mapOf(
                "code_size_bytes" to (50 * 1024 * 1024L), // 50 MB
                "data_size_bytes" to (10 * 1024 * 1024L), // 10 MB
                "cache_size_bytes" to (5 * 1024 * 1024L), // 5 MB
                "external_cache_size_bytes" to (2 * 1024 * 1024L) // 2 MB
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app size info: ${e.message}", e)
            return emptyMap()
        }
    }
    
    /**
     * Gets a list of all installed apps with basic information
     */
    fun getAllInstalledApps(): List<Map<String, Any>> {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return emptyList()
            }
            
            val apps = mutableListOf<Map<String, Any>>()
            
            // Get all installed packages
            val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            
            for (pkgInfo in packages) {
                val appInfo = pkgInfo.applicationInfo
                
                val appData = mapOf<String, Any>(
                    "package_name" to pkgInfo.packageName,
                    "app_name" to appInfo.loadLabel(pm).toString(),
                    "version_name" to (pkgInfo.versionName ?: "Unknown"),
                    "version_code" to pkgInfo.versionCode.toLong(),
                    "is_system_app" to (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0),
                    "first_install_time" to pkgInfo.firstInstallTime,
                    "last_update_time" to pkgInfo.lastUpdateTime,
                    "target_sdk" to appInfo.targetSdkVersion,
                    "permissions_count" to (pkgInfo.requestedPermissions?.size ?: 0)
                )
                
                apps.add(appData)
            }
            
            Log.d(TAG, "Retrieved ${apps.size} installed apps")
            return apps
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed apps: ${e.message}", e)
            return emptyList()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SystemInspectorService destroyed")
    }
}