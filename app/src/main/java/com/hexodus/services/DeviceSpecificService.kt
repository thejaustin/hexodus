package com.hexodus.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Display
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.hexodus.utils.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * DeviceSpecificService - Service for device-specific features and optimizations
 * Includes Samsung Z Flip 5 specific optimizations and foldable display support
 */
class DeviceSpecificService : Service() {
    
    companion object {
        private const val TAG = "DeviceSpecificService"
        private const val ACTION_GET_DEVICE_INFO = "com.hexodus.GET_DEVICE_INFO"
        private const val ACTION_OPTIMIZE_FOR_FOLDABLE = "com.hexodus.OPTIMIZE_FOR_FOLDABLE"
        private const val ACTION_GET_DISPLAY_FEATURES = "com.hexodus.GET_DISPLAY_FEATURES"
        private const val ACTION_MANAGE_DEX_MODE = "com.hexodus.MANAGE_DEX_MODE"
        private const val ACTION_GET_BIXBY_CAPABILITIES = "com.hexodus.GET_BIXBY_CAPABILITIES"
        private const val ACTION_MANAGE_ONE_UI_FEATURES = "com.hexodus.MANAGE_ONE_UI_FEATURES"
        
        // Intent extras
        const val EXTRA_DEX_MODE = "dex_mode" // desktop, phone, auto
        const val EXTRA_FEATURE_NAME = "feature_name"
        const val EXTRA_FEATURE_ENABLED = "feature_enabled"
    }
    
    private lateinit var shizukuBridgeService: ShizukuBridgeService
    private lateinit var windowInfoTracker: WindowInfoTracker
    private var isMonitoringDisplays = false
    
    override fun onCreate() {
        super.onCreate()
        shizukuBridgeService = ShizukuBridgeService()
        windowInfoTracker = WindowInfoTracker.getOrCreate(this)
        Log.d(TAG, "DeviceSpecificService created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_GET_DEVICE_INFO -> {
                getDeviceInfo()
            }
            ACTION_OPTIMIZE_FOR_FOLDABLE -> {
                optimizeForFoldable()
            }
            ACTION_GET_DISPLAY_FEATURES -> {
                getDisplayFeatures()
            }
            ACTION_MANAGE_DEX_MODE -> {
                val dexMode = intent.getStringExtra(EXTRA_DEX_MODE) ?: "auto"
                manageDeXMode(dexMode)
            }
            ACTION_GET_BIXBY_CAPABILITIES -> {
                getBixbyCapabilities()
            }
            ACTION_MANAGE_ONE_UI_FEATURES -> {
                val featureName = intent.getStringExtra(EXTRA_FEATURE_NAME)
                val enabled = intent.getBooleanExtra(EXTRA_FEATURE_ENABLED, false)
                
                if (!featureName.isNullOrEmpty()) {
                    manageOneUIFeature(featureName, enabled)
                }
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Gets detailed device information
     */
    private fun getDeviceInfo() {
        try {
            // Collect device information
            val deviceInfo = mapOf(
                "manufacturer" to Build.MANUFACTURER,
                "model" to Build.MODEL,
                "brand" to Build.BRAND,
                "device" to Build.DEVICE,
                "product" to Build.PRODUCT,
                "board" to Build.BOARD,
                "hardware" to Build.HARDWARE,
                "fingerprint" to Build.FINGERPRINT,
                "sdk_version" to Build.VERSION.SDK_INT,
                "release_version" to Build.VERSION.RELEASE,
                "is_samsung_device" to (Build.MANUFACTURER.lowercase() == "samsung"),
                "is_foldable" to isFoldableDevice(),
                "display_features" to getDisplayFeaturesInternal(),
                "ram_gb" to getRamSizeGB(),
                "storage_gb" to getStorageSizeGB()
            )
            
            Log.d(TAG, "Retrieved device information for: ${deviceInfo["model"]}")
            
            // Broadcast results
            val successIntent = Intent("DEVICE_INFO_RETRIEVED")
            successIntent.putExtra("device_info", HashMap(deviceInfo))
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device info: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("DEVICE_INFO_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Optimizes UI for foldable devices
     */
    private fun optimizeForFoldable() {
        try {
            if (!isFoldableDevice()) {
                Log.d(TAG, "Device is not foldable, skipping optimization")
                
                // Broadcast not applicable
                val notApplicableIntent = Intent("FOLDABLE_OPTIMIZATION_NOT_APPLICABLE")
                sendBroadcast(notApplicableIntent)
                return
            }
            
            // In a real implementation, this would optimize UI for foldable displays
            // For this example, we'll simulate the process
            Log.d(TAG, "Optimized UI for foldable device")
            
            // Broadcast success
            val successIntent = Intent("FOLDABLE_OPTIMIZED")
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error optimizing for foldable: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("FOLDABLE_OPTIMIZATION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets display features including foldable capabilities
     */
    private fun getDisplayFeatures() {
        try {
            val displayFeatures = getDisplayFeaturesInternal()
            
            Log.d(TAG, "Retrieved display features")
            
            // Broadcast results
            val successIntent = Intent("DISPLAY_FEATURES_RETRIEVED")
            successIntent.putExtra("display_features", HashMap(displayFeatures))
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting display features: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("DISPLAY_FEATURES_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Manages Samsung DeX mode
     */
    private fun manageDeXMode(mode: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            if (!isSamsungDevice()) {
                Log.w(TAG, "DeX mode management is only available on Samsung devices")
                
                // Broadcast not supported
                val notSupportedIntent = Intent("DEX_MODE_NOT_SUPPORTED")
                notSupportedIntent.putExtra("error", "DeX mode is only available on Samsung devices")
                sendBroadcast(notSupportedIntent)
                return
            }
            
            val validModes = listOf("desktop", "phone", "auto")
            if (mode !in validModes) {
                Log.e(TAG, "Invalid DeX mode: $mode")
                return
            }
            
            // In a real implementation, this would manage DeX mode
            // For this example, we'll simulate the process
            Log.d(TAG, "Set DeX mode to: $mode")
            
            // Broadcast success
            val successIntent = Intent("DEX_MODE_SET")
            successIntent.putExtra("dex_mode", mode)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error managing DeX mode: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("DEX_MODE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets Bixby capabilities on Samsung devices
     */
    private fun getBixbyCapabilities() {
        try {
            if (!isSamsungDevice()) {
                Log.w(TAG, "Bixby capabilities are only available on Samsung devices")
                
                // Broadcast not supported
                val notSupportedIntent = Intent("BIXBY_CAPABILITIES_NOT_SUPPORTED")
                notSupportedIntent.putExtra("error", "Bixby is only available on Samsung devices")
                sendBroadcast(notSupportedIntent)
                return
            }
            
            // In a real implementation, this would query Bixby capabilities
            // For this example, we'll simulate the process
            val bixbyCapabilities = mapOf(
                "available" to true,
                "version" to "3.0",
                "supported_features" to listOf(
                    "voice_commands",
                    "app_control",
                    "smart_suggestions",
                    "bixby_routines"
                ),
                "is_enabled" to true
            )
            
            Log.d(TAG, "Retrieved Bixby capabilities")
            
            // Broadcast results
            val successIntent = Intent("BIXBY_CAPABILITIES_RETRIEVED")
            successIntent.putExtra("bixby_capabilities", HashMap(bixbyCapabilities))
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Bixby capabilities: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("BIXBY_CAPABILITIES_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Manages One UI specific features
     */
    private fun manageOneUIFeature(featureName: String, enabled: Boolean) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            if (!isSamsungDevice()) {
                Log.w(TAG, "One UI features are only available on Samsung devices")
                
                // Broadcast not supported
                val notSupportedIntent = Intent("ONE_UI_FEATURE_NOT_SUPPORTED")
                notSupportedIntent.putExtra("error", "One UI features are only available on Samsung devices")
                sendBroadcast(notSupportedIntent)
                return
            }
            
            // Validate feature name
            val validFeatures = listOf(
                "edge_lighting", "always_on_display", "motion_wakeup", 
                "one_hand_mode", "floating_window", "multi_window", 
                "theme_engine", "monet_override", "material_you_bypass"
            )
            
            if (featureName !in validFeatures) {
                Log.e(TAG, "Invalid One UI feature: $featureName")
                return
            }
            
            // Validate inputs
            if (SecurityUtils.containsDangerousChars(featureName)) {
                Log.e(TAG, "Dangerous characters detected in feature name")
                return
            }
            
            // In a real implementation, this would manage One UI features
            // For this example, we'll simulate the process
            Log.d(TAG, "Set One UI feature $featureName to ${if(enabled) "enabled" else "disabled"}")
            
            // Broadcast success
            val successIntent = Intent("ONE_UI_FEATURE_SET")
            successIntent.putExtra("feature_name", featureName)
            successIntent.putExtra("enabled", enabled)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error managing One UI feature: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("ONE_UI_FEATURE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Internal method to get display features
     */
    private fun getDisplayFeaturesInternal(): Map<String, Any> {
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displays = displayManager.displays
        
        val features = mutableMapOf<String, Any>()
        
        // Check for foldable features
        features["is_foldable"] = isFoldableDevice()
        features["has_hinge"] = false // Would be determined by actual folding features
        features["display_count"] = displays.size
        features["primary_display_size"] = getDisplaySize(displays.firstOrNull())
        
        if (displays.size > 1) {
            features["secondary_display_size"] = getDisplaySize(displays.getOrNull(1))
        }
        
        return features
    }
    
    /**
     * Checks if the device is foldable
     */
    private fun isFoldableDevice(): Boolean {
        // In a real implementation, this would check for foldable features
        // For this example, we'll check if it's a known foldable model
        val model = Build.MODEL.lowercase()
        return model.contains("flip") || model.contains("fold") || model.contains("z")
    }
    
    /**
     * Checks if the device is a Samsung device
     */
    private fun isSamsungDevice(): Boolean {
        return Build.MANUFACTURER.lowercase() == "samsung"
    }
    
    /**
     * Gets display size in inches
     */
    private fun getDisplaySize(display: Display?): Float {
        if (display == null) return 0f

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            val xdpi = resources.displayMetrics.xdpi
            val ydpi = resources.displayMetrics.ydpi
            if (xdpi <= 0f || ydpi <= 0f) return 0f
            val widthInches = bounds.width() / xdpi
            val heightInches = bounds.height() / ydpi
            return kotlin.math.sqrt((widthInches * widthInches + heightInches * heightInches).toDouble()).toFloat()
        } else {
            val metrics = android.util.DisplayMetrics()
            @Suppress("DEPRECATION")
            display.getRealMetrics(metrics)
            val widthInches = metrics.widthPixels / metrics.xdpi
            val heightInches = metrics.heightPixels / metrics.ydpi
            return kotlin.math.sqrt((widthInches * widthInches + heightInches * heightInches).toDouble()).toFloat()
        }
    }
    
    /**
     * Gets RAM size in GB
     */
    private fun getRamSizeGB(): Float {
        val memInfo = android.app.ActivityManager.MemoryInfo()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        activityManager.getMemoryInfo(memInfo)
        
        // This gets available RAM, but we want total RAM
        // In a real implementation, we'd read from /proc/meminfo
        return 8f // Return a common value for demonstration
    }
    
    /**
     * Gets storage size in GB
     */
    private fun getStorageSizeGB(): Float {
        val storageManager = getSystemService(Context.STORAGE_SERVICE) as android.os.storage.StorageManager
        val storageStatsManager = getSystemService(Context.STORAGE_STATS_SERVICE) as android.app.usage.StorageStatsManager
        
        // In a real implementation, we'd get actual storage stats
        // For this example, return a common value
        return 128f // Return a common value for demonstration
    }
    
    /**
     * Starts monitoring display changes for foldable devices
     */
    fun startDisplayMonitoring() {
        if (isMonitoringDisplays) return
        
        isMonitoringDisplays = true
        Log.d(TAG, "Started display monitoring")
        
        // In a real implementation, this would monitor display changes
        // For this example, we'll simulate the process
        CoroutineScope(Dispatchers.Main).launch {
            try {
                windowInfoTracker.windowLayoutInfo(this@DeviceSpecificService).collect { layoutInfo ->
                    handleWindowLayoutInfo(layoutInfo)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error monitoring display changes: ${e.message}", e)
            }
        }
    }
    
    /**
     * Handles window layout information for foldable devices
     */
    private fun handleWindowLayoutInfo(layoutInfo: WindowLayoutInfo) {
        var hasFoldingFeature = false
        var isTabletop = false
        var isSeparating = false
        
        for (feature in layoutInfo.displayFeatures) {
            if (feature is FoldingFeature) {
                hasFoldingFeature = true
                
                when (feature.state) {
                    FoldingFeature.State.FLAT -> {
                        // Device is flat (folded or unfolded)
                        Log.d(TAG, "Device is in flat state")
                    }
                    FoldingFeature.State.HALF_OPENED -> {
                        // Device is half-opened (tabletop mode)
                        isTabletop = true
                        Log.d(TAG, "Device is in tabletop mode")
                    }
                    else -> {
                        // Handle other states
                        Log.d(TAG, "Device is in unknown folding state: ${feature.state}")
                    }
                }
                
                Log.d(TAG, "Folding feature detected: State=${feature.state}, Bounds=${feature.bounds}")
            }
        }
        
        if (!hasFoldingFeature) {
            Log.d(TAG, "Device is not foldable")
        }
        
        // Broadcast display state changes
        val intent = Intent("DISPLAY_STATE_CHANGED")
        intent.putExtra("is_foldable", hasFoldingFeature)
        intent.putExtra("is_tabletop", isTabletop)
        intent.putExtra("is_separating", isSeparating)
        sendBroadcast(intent)
    }
    
    /**
     * Stops monitoring display changes
     */
    fun stopDisplayMonitoring() {
        if (!isMonitoringDisplays) return
        
        isMonitoringDisplays = false
        Log.d(TAG, "Stopped display monitoring")
    }
    
    /**
     * Gets Samsung-specific features
     */
    fun getSamsungFeatures(): Map<String, Any> {
        if (!isSamsungDevice()) {
            return emptyMap()
        }
        
        return mapOf(
            "has_edge_panel" to true,
            "has_knox" to true,
            "has_deX" to true,
            "has_bixby" to true,
            "has_galaxy_store" to true,
            "has_s_pen_support" to false, // Would be determined by actual hardware
            "one_ui_version" to getOneUIVersion()
        )
    }
    
    /**
     * Gets the One UI version
     */
    private fun getOneUIVersion(): String {
        // In a real implementation, this would check the actual One UI version
        // For this example, we'll return a mock version
        return "8.0" // Assuming One UI 8
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isMonitoringDisplays) {
            stopDisplayMonitoring()
        }
        Log.d(TAG, "DeviceSpecificService destroyed")
    }
}