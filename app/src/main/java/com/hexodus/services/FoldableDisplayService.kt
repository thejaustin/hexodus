package com.hexodus.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        var isFlat = false

        for (feature in layoutInfo.displayFeatures) {
            if (feature is FoldingFeature) {
                hasFoldingFeature = true

                when (feature.state) {
                    FoldingFeature.State.FLAT -> {
                        isFlat = true
                        isCoverScreen = determineIfCoverScreen()
                        isMainScreen = !isCoverScreen
                    }
                    FoldingFeature.State.HALF_OPENED -> {
                        isTabletop = true
                        isCoverScreen = determineIfCoverScreen()
                        isMainScreen = !isCoverScreen
                    }
                    else -> {
                        Log.d(TAG, "Device is in state: ${feature.state}")
                    }
                }

                Log.d(TAG, "Folding feature detected: State=${feature.state}, Bounds=${feature.bounds}")
            }
        }

        if (!hasFoldingFeature) {
            isCoverScreen = false
            isMainScreen = true
            isFlat = true
        }

        isTabletopMode = isTabletop
        isFlatMode = isFlat

        updateResourceMapping()

        Log.d(TAG, "Display state updated - Cover: $isCoverScreen, Main: $isMainScreen, Tabletop: $isTabletopMode, Flat: $isFlatMode")
    }

    /**
     * Determines if the current display context is the cover screen
     */
    private fun determineIfCoverScreen(): Boolean {
        val screenSizeInches = getScreenSizeInches()
        // Cover screens are typically smaller than main screens
        // For Z Flip 5: Cover ~1.9", Main ~6.7"
        return screenSizeInches < 3.0f
    }

    /**
     * Gets the screen diagonal size in inches using modern APIs
     */
    private fun getScreenSizeInches(): Float {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            val resources = resources
            val xdpi = resources.displayMetrics.xdpi
            val ydpi = resources.displayMetrics.ydpi
            if (xdpi <= 0f || ydpi <= 0f) return 6.0f
            val widthInches = bounds.width() / xdpi
            val heightInches = bounds.height() / ydpi
            return kotlin.math.sqrt((widthInches * widthInches + heightInches * heightInches).toDouble()).toFloat()
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            display.getRealMetrics(metrics)
            return calculateScreenSize(metrics)
        }
    }

    /**
     * Calculates screen size in inches from DisplayMetrics
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

        editor.putBoolean("is_cover_screen", isCoverScreen)
        editor.putBoolean("is_main_screen", isMainScreen)
        editor.putBoolean("is_tabletop_mode", isTabletopMode)
        editor.putBoolean("is_flat_mode", isFlatMode)

        val resourceMap = if (isCoverScreen) {
            "cover_screen_resources"
        } else {
            "main_screen_resources"
        }

        editor.putString("current_resource_map", resourceMap)
        editor.apply()

        Log.d(TAG, "Resource mapping updated: $resourceMap")
    }

    /**
     * Stops monitoring display changes
     */
    private fun stopMonitoringDisplays() {
        Log.d(TAG, "Stopped monitoring foldable displays")
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
        return resources.displayMetrics
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "FoldableDisplayService destroyed")
    }
}
