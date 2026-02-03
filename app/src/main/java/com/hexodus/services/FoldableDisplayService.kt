package com.hexodus.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

/**
 * FoldableDisplayService - Enhanced handler for foldable display contexts
 * Optimized for Samsung Z Flip 5 and other foldable devices based on awesome-shizuku insights
 */
class FoldableDisplayService : Service() {
    
    companion object {
        private const val TAG = "FoldableDisplayService"
        private const val ACTION_START_MONITORING = "com.hexodus.START_FOLDABLE_MONITORING"
        private const val ACTION_STOP_MONITORING = "com.hexodus.STOP_FOLDABLE_MONITORING"
        
        // Intent extras
        const val EXTRA_COVER_SCREEN_LAYOUT = "cover_screen_layout"
        const val EXTRA_MAIN_SCREEN_LAYOUT = "main_screen_layout"
    }
    
    private lateinit var windowInfoTracker: WindowInfoTracker
    private var isCoverScreen = false
    private var isMainScreen = true
    private var isTabletopMode = false
    private var isFlatMode = false
    
    override fun onCreate() {
        super.onCreate()
        windowInfoTracker = WindowInfoTracker.getOrCreate(this)
        Log.d(TAG, "FoldableDisplayService created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> {
                startMonitoringDisplays()
            }
            ACTION_STOP_MONITORING -> {
                stopMonitoringDisplays()
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Starts monitoring display changes for foldable devices
     */
    private fun startMonitoringDisplays() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                windowInfoTracker.windowLayoutInfo(this@FoldableDisplayService).collect { layoutInfo ->
                    handleWindowLayoutInfo(layoutInfo)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error monitoring window layout: ${e.message}", e)
            }
        }
    }
    
    /**
     * Handles window layout information to detect foldable states
     */
    private fun handleWindowLayoutInfo(layoutInfo: WindowLayoutInfo) {
        var hasFoldingFeature = false
        var isTabletop = false
        var isSeparating = false
        var isFlat = false
        
        for (feature in layoutInfo.displayFeatures) {
            if (feature is FoldingFeature) {
                hasFoldingFeature = true
                
                when (feature.state) {
                    FoldingFeature.State.FLAT -> {
                        // Device is flat (folded or unfolded)
                        isFlat = true
                        isCoverScreen = determineIfCoverScreen()
                        isMainScreen = !isCoverScreen
                    }
                    FoldingFeature.State.HALF_OPENED -> {
                        // Device is half-opened (tabletop mode)
                        isTabletop = true
                        isCoverScreen = determineIfCoverScreen()
                        isMainScreen = !isCoverScreen
                    }
                    else -> {
                        // Handle other states (in transition or unknown)
                        Log.d(TAG, "Device is in state: ${feature.state}")
                    }
                }
                
                Log.d(TAG, "Folding feature detected: State=${feature.state}, Bounds=${feature.bounds}")
            }
        }
        
        if (!hasFoldingFeature) {
            // Not a foldable device, treat as main screen
            isCoverScreen = false
            isMainScreen = true
            isFlat = true
        }
        
        // Update internal state
        isTabletopMode = isTabletop
        isFlatMode = isFlat
        
        // Update resource mapping based on current display
        updateResourceMapping()
        
        Log.d(TAG, "Display state updated - Cover: $isCoverScreen, Main: $isMainScreen, Tabletop: $isTabletopMode, Flat: $isFlatMode")
    }
    
    /**
     * Determines if the current display context is the cover screen
     */
    private fun determineIfCoverScreen(): Boolean {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        
        // Get display metrics
        val metrics = DisplayMetrics()
        display.getRealMetrics(metrics)
        
        // Cover screens are typically smaller than main screens
        // For Z Flip 5: Cover ~1.9", Main ~6.7"
        val screenSizeInches = calculateScreenSize(metrics)
        
        // Threshold for considering it a cover screen (adjust as needed)
        return screenSizeInches < 3.0f  // Less than 3 inches likely means cover screen
    }
    
    /**
     * Calculates screen size in inches
     */
    private fun calculateScreenSize(metrics: DisplayMetrics): Float {
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        return kotlin.math.sqrt((widthInches * widthInches + heightInches * heightInches).toDouble()).toFloat()
    }
    
    /**
     * Updates resource mapping based on current display context
     */
    private fun updateResourceMapping() {
        val prefs = applicationContext.getSharedPreferences("display_context", 0)
        val editor = prefs.edit()
        
        // Store current display context
        editor.putBoolean("is_cover_screen", isCoverScreen)
        editor.putBoolean("is_main_screen", isMainScreen)
        editor.putBoolean("is_tabletop_mode", isTabletopMode)
        editor.putBoolean("is_flat_mode", isFlatMode)
        
        // Determine which resource map to use
        val resourceMap = if (isCoverScreen) {
            // Use simplified, high-visibility layout for cover screen
            "cover_screen_resources"
        } else {
            // Use normal layout for main screen
            "main_screen_resources"
        }
        
        editor.putString("current_resource_map", resourceMap)
        editor.apply()
        
        // Broadcast an intent to notify other components
        val intent = Intent("DISPLAY_CONTEXT_CHANGED")
        intent.putExtra("is_cover_screen", isCoverScreen)
        intent.putExtra("is_main_screen", isMainScreen)
        intent.putExtra("is_tabletop_mode", isTabletopMode)
        intent.putExtra("is_flat_mode", isFlatMode)
        intent.putExtra("resource_map", resourceMap)
        sendBroadcast(intent)
        
        Log.d(TAG, "Resource mapping updated: $resourceMap")
    }
    
    /**
     * Stops monitoring display changes
     */
    private fun stopMonitoringDisplays() {
        // In a real implementation, we would unsubscribe from the flow
        // For this example, we'll just log the action
        Log.d(TAG, "Stopped monitoring foldable displays")
        
        // Broadcast stop event
        val intent = Intent("FOLDABLE_MONITORING_STOPPED")
        sendBroadcast(intent)
    }
    
    /**
     * Gets the current display context
     */
    fun getCurrentDisplayContext(): Triple<Boolean, Boolean, Boolean> {
        return Triple(isCoverScreen, isMainScreen, isTabletopMode)
    }
    
    /**
     * Gets the appropriate resource map for the current display
     */
    fun getCurrentResourceMap(): String {
        return if (isCoverScreen) {
            "cover_screen_resources"
        } else {
            "main_screen_resources"
        }
    }
    
    /**
     * Gets display metrics for the current context
     */
    fun getCurrentDisplayMetrics(): DisplayMetrics {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val metrics = DisplayMetrics()
        display.getRealMetrics(metrics)
        return metrics
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "FoldableDisplayService destroyed")
    }
}