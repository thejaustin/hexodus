package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.content.Context
import androidx.annotation.RequiresApi
import com.hexodus.utils.AccessibilityUtils

/**
 * AccessibilityCheckerService - Service for verifying accessibility compliance
 * Performs checks to ensure the app meets Android 16 accessibility standards
 */
class AccessibilityCheckerService : Service() {
    
    companion object {
        private const val TAG = "AccessibilityCheckerService"
        private const val ACTION_RUN_ACCESSIBILITY_CHECKS = "com.hexodus.RUN_ACCESSIBILITY_CHECKS"
        private const val ACTION_VALIDATE_COMPONENT = "com.hexodus.VALIDATE_COMPONENT"
        private const val ACTION_GET_ACCESSIBILITY_STATUS = "com.hexodus.GET_ACCESSIBILITY_STATUS"
        
        // Intent extras
        const val EXTRA_COMPONENT_TYPE = "component_type"  // button, text_field, icon, etc.
        const val EXTRA_FOREGROUND_COLOR = "foreground_color"
        const val EXTRA_BACKGROUND_COLOR = "background_color"
        const val EXTRA_TEXT_SIZE = "text_size"
        const val EXTRA_WIDTH_DP = "width_dp"
        const val EXTRA_HEIGHT_DP = "height_dp"
        const val EXTRA_LABEL = "label"
        const val EXTRA_HINT = "hint"
        const val EXTRA_CONTENT_DESCRIPTION = "content_description"
    }
    
    private lateinit var accessibilityManager: AccessibilityManager
    
    override fun onCreate() {
        super.onCreate()
        accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        Log.d(TAG, "AccessibilityCheckerService created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_RUN_ACCESSIBILITY_CHECKS -> {
                runAccessibilityChecks()
            }
            ACTION_VALIDATE_COMPONENT -> {
                val componentType = intent?.getStringExtra(EXTRA_COMPONENT_TYPE)
                val fgColor = intent?.getIntExtra(EXTRA_FOREGROUND_COLOR, -1) ?: -1
                val bgColor = intent?.getIntExtra(EXTRA_BACKGROUND_COLOR, -1) ?: -1
                val textSize = intent?.getFloatExtra(EXTRA_TEXT_SIZE, -1f) ?: -1f
                val widthDp = intent?.getFloatExtra(EXTRA_WIDTH_DP, -1f) ?: -1f
                val heightDp = intent?.getFloatExtra(EXTRA_HEIGHT_DP, -1f) ?: -1f
                val label = intent?.getStringExtra(EXTRA_LABEL)
                val hint = intent?.getStringExtra(EXTRA_HINT)
                val contentDesc = intent?.getStringExtra(EXTRA_CONTENT_DESCRIPTION)
                
                if (componentType != null) {
                    validateComponent(
                        componentType, 
                        fgColor, 
                        bgColor, 
                        textSize, 
                        widthDp, 
                        heightDp, 
                        label, 
                        hint, 
                        contentDesc
                    )
                }
            }
            ACTION_GET_ACCESSIBILITY_STATUS -> {
                getAccessibilityStatus()
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Runs comprehensive accessibility checks on the app
     */
    private fun runAccessibilityChecks() {
        try {
            val results = mutableMapOf<String, Any>()
            
            // Check if accessibility services are enabled
            results["accessibility_enabled"] = accessibilityManager.isEnabled
            
            // Check high contrast text setting (API 31+)
            results["high_contrast_enabled"] = getHighTextContrastEnabled()
            
            // Check touch exploration
            results["touch_exploration_enabled"] = accessibilityManager.isTouchExplorationEnabled
            
            // Check font scale
            results["font_scale"] = resources.configuration.fontScale
            
            // Check if reduce motion should be considered
            results["reduce_motion_enabled"] = false // Simplified check
            
            Log.d(TAG, "Accessibility checks completed")

            // Broadcast results
            val intent = Intent("ACCESSIBILITY_CHECKS_COMPLETED")
            intent.putExtra("results", HashMap(results))
            sendBroadcast(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error running accessibility checks: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("ACCESSIBILITY_CHECKS_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Validates a specific component for accessibility compliance
     */
    private fun validateComponent(
        componentType: String,
        fgColor: Int,
        bgColor: Int,
        textSize: Float,
        widthDp: Float,
        heightDp: Float,
        label: String?,
        hint: String?,
        contentDesc: String?
    ) {
        try {
            val validationResults = mutableMapOf<String, Any>()
            
            // Validate contrast ratio
            if (fgColor != -1 && bgColor != -1) {
                validationResults["meets_contrast_requirements"] = AccessibilityUtils.meetsAccessibilityContrast(fgColor, bgColor)
            }
            
            // Validate touch target size
            if (widthDp != -1f && heightDp != -1f) {
                validationResults["valid_touch_target"] = AccessibilityUtils.isValidTouchTarget(
                    androidx.compose.ui.unit.Dp(widthDp), 
                    androidx.compose.ui.unit.Dp(heightDp)
                )
            }
            
            // Validate text size
            if (textSize != -1f) {
                validationResults["valid_text_size"] = textSize >= AccessibilityUtils.getMinimumTextSize()
            }
            
            // Validate accessibility labels
            validationResults["valid_accessibility_labels"] = AccessibilityUtils.isValidFormFieldAccessibility(
                label, hint, contentDesc
            )
            
            Log.d(TAG, "Component validation completed for type: $componentType")
            
            // Broadcast results
            val intent = Intent("COMPONENT_VALIDATION_COMPLETED")
            intent.putExtra("component_type", componentType)
            intent.putExtra("validation_results", HashMap(validationResults))
            sendBroadcast(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating component: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("COMPONENT_VALIDATION_ERROR")
            errorIntent.putExtra("component_type", componentType)
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets the current accessibility status
     */
    private fun getAccessibilityStatus() {
        try {
            val status = mutableMapOf<String, Any>()
            
            status["is_accessibility_enabled"] = accessibilityManager.isEnabled
            status["is_high_contrast_enabled"] = getHighTextContrastEnabled()
            status["is_touch_exploration_enabled"] = accessibilityManager.isTouchExplorationEnabled
            status["font_scale"] = resources.configuration.fontScale
            status["should_reduce_animations"] = AccessibilityUtils.shouldReduceAnimations(this)
            
            Log.d(TAG, "Accessibility status retrieved")
            
            // Broadcast status
            val intent = Intent("ACCESSIBILITY_STATUS_RETRIEVED")
            intent.putExtra("status", HashMap(status))
            sendBroadcast(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting accessibility status: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("ACCESSIBILITY_STATUS_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Checks if the app is currently in an accessibility-friendly state
     */
    fun isAccessibilityFriendly(): Boolean {
        return accessibilityManager.isEnabled ||
               getHighTextContrastEnabled() ||
               resources.configuration.fontScale > 1.0f
    }
    
    /**
     * Gets recommended adjustments for accessibility
     */
    fun getAccessibilityRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()

        if (resources.configuration.fontScale < 1.2f) {
            recommendations.add("Consider increasing text size for better readability")
        }

        if (!getHighTextContrastEnabled()) {
            recommendations.add("Enable high contrast mode for better visibility")
        }
        
        if (!accessibilityManager.isEnabled) {
            recommendations.add("Enable accessibility services for enhanced navigation")
        }
        
        return recommendations
    }

    /**
     * Gets the high text contrast enabled status in a way that compiles on all API levels
     */
    private fun getHighTextContrastEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getHighTextContrastEnabledApi31()
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getHighTextContrastEnabledApi31(): Boolean {
        return accessibilityManager.isHighTextContrastEnabled
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "AccessibilityCheckerService destroyed")
    }
}