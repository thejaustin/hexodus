package com.hexodus.services
import com.hexodus.HexodusApplication

import android.app.Service

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.content.Context
import com.hexodus.utils.AccessibilityUtils

/**
 * AccessibilityCheckerService - Service for verifying accessibility compliance
 * Performs checks to ensure the app meets Android 16 accessibility standards
 */
object AccessibilityCheckerService {
    private const val TAG = "AccessibilityCheckerService"
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

    private val accessibilityManager by lazy { HexodusApplication.context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager }

    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "com.hexodus.RUN_ACCESSIBILITY_CHECKS") {
            runChecks()
        }
        return android.app.Service.START_STICKY
    }

    private fun runChecks() {
        scope.launch {
            try {
                val results = mutableMapOf<String, Any>()
                results["accessibility_enabled"] = accessibilityManager.isEnabled
                results["high_contrast_enabled"] = getHighTextContrastEnabled()
                results["touch_exploration_enabled"] = accessibilityManager.isTouchExplorationEnabled
                results["font_scale"] = HexodusApplication.context.resources.configuration.fontScale
                
                Log.d(TAG, "Accessibility checks completed")
                val resultIntent = Intent("ACCESSIBILITY_CHECKS_COMPLETED")
                resultIntent.putExtra("results", HashMap(results))
                HexodusApplication.context.sendBroadcast(resultIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error running accessibility checks", e)
            }
        }
    }

    fun getAccessibilityStatus(): Map<String, Any> {
        val status = mutableMapOf<String, Any>()
        try {
            status["is_accessibility_enabled"] = accessibilityManager.isEnabled
            status["is_high_contrast_enabled"] = getHighTextContrastEnabled()
            status["is_touch_exploration_enabled"] = accessibilityManager.isTouchExplorationEnabled
            status["font_scale"] = HexodusApplication.context.resources.configuration.fontScale
            status["should_reduce_animations"] = AccessibilityUtils.shouldReduceAnimations(HexodusApplication.context)
            
            Log.d(TAG, "Accessibility status retrieved")
            HexodusApplication.context.sendBroadcast(Intent("ACCESSIBILITY_STATUS_RETRIEVED").putExtra("status", HashMap(status)))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting accessibility status", e)
        }
        return status
    }

    private fun getHighTextContrastEnabled(): Boolean {
        return try {
            val method = accessibilityManager.javaClass.getMethod("isHighTextContrastEnabled")
            method.invoke(accessibilityManager) as Boolean
        } catch (e: Exception) {
            false
        }
    }
}
