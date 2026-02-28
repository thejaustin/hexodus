package com.hexodus.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.hexodus.HexodusApplication

/**
 * FoldableDisplayService - Enhanced handler for foldable display contexts
 * Optimized for Samsung Z Flip 5 and other foldable devices based on awesome-shizuku insights
 */
object FoldableDisplayService {
    private val context: android.content.Context get() = com.hexodus.HexodusApplication.context
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

    private const val TAG = "FoldableDisplayService"
    private const val ACTION_START_MONITORING = "com.hexodus.START_FOLDABLE_MONITORING"
    private const val ACTION_STOP_MONITORING = "com.hexodus.STOP_FOLDABLE_MONITORING"

    // Intent extras
    const val EXTRA_COVER_SCREEN_LAYOUT = "cover_screen_layout"
    const val EXTRA_MAIN_SCREEN_LAYOUT = "main_screen_layout"

    private var isCoverScreen = false
    private var isMainScreen = true
    private var isTabletopMode = false
    private var isFlatMode = false

    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> {
                startMonitoringDisplays()
            }
            ACTION_STOP_MONITORING -> {
                stopMonitoringDisplays()
            }
        }

        return android.app.Service.START_STICKY
    }

    /**
     * Starts monitoring display changes for foldable devices
     */
    private fun startMonitoringDisplays() {
        Log.d(TAG, "Starting foldable display monitoring")
        // Note: WindowInfoTracker usually requires an Activity context.
        // For a background object, we'll use a simplified detection method.
        updateDisplayState()
    }

    private fun stopMonitoringDisplays() {
        Log.d(TAG, "Stopping foldable display monitoring")
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

        updateDisplayState()
    }

    private fun determineIfCoverScreen(): Boolean {
        return try {
            val metrics = context.resources.displayMetrics
            // Typical cover screen is much smaller than main screen
            metrics.widthPixels < 600 || metrics.heightPixels < 600
        } catch (e: Exception) {
            false
        }
    }

    private fun updateDisplayState() {
        val intent = Intent("FOLDABLE_STATE_CHANGED")
        intent.putExtra("is_cover_screen", isCoverScreen)
        intent.putExtra("is_main_screen", isMainScreen)
        intent.putExtra("is_tabletop_mode", isTabletopMode)
        intent.putExtra("is_flat_mode", isFlatMode)
        context.sendBroadcast(intent)
    }

    fun getDisplayMetrics(): DisplayMetrics {
        return context.resources.displayMetrics
    }
}
