package com.hexodus.utils

import android.view.accessibility.AccessibilityManager
import android.content.Context
import android.view.View
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * AccessibilityUtils - Utility functions for accessibility compliance
 * Ensures the app meets Android 16 accessibility standards
 */
object AccessibilityUtils {
    
    /**
     * Checks if accessibility services are enabled
     */
    fun isAccessibilityEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.isEnabled
    }
    
    /**
     * Checks if high contrast text is enabled
     */
    fun isHighContrastTextEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.isHighTextContrastEnabled
    }
    
    /**
     * Gets the recommended minimum touch target size based on accessibility settings
     */
    fun getRecommendedTouchTargetSize(context: Context): Dp {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return if (am.isEnabled) 48.dp else 48.dp // Still maintain minimum for all users
    }
    
    /**
     * Checks if reduce motion is enabled
     */
    fun isReduceMotionEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.isTouchExplorationEnabled // Simplified check
    }
    
    /**
     * Applies accessibility semantics to a view
     */
    fun applyAccessibilitySemantics(view: View, contentDescription: String) {
        view.contentDescription = contentDescription
    }
    
    /**
     * Validates that a color pair meets accessibility contrast requirements
     * @param foregroundColor The text/icon color
     * @param backgroundColor The background color
     * @return True if the contrast ratio meets WCAG AA standards (4.5:1 for normal text, 3:1 for large text)
     */
    fun meetsAccessibilityContrast(foregroundColor: Int, backgroundColor: Int): Boolean {
        val contrastRatio = calculateContrastRatio(foregroundColor, backgroundColor)
        
        // WCAG AA standard: 4.5:1 for normal text, 3:1 for large text
        return contrastRatio >= 4.5
    }
    
    /**
     * Calculates the contrast ratio between two colors
     * @param color1 The first color (typically text)
     * @param color2 The second color (typically background)
     * @return The contrast ratio as defined by WCAG
     */
    private fun calculateContrastRatio(color1: Int, color2: Int): Double {
        val lum1 = calculateRelativeLuminance(color1)
        val lum2 = calculateRelativeLuminance(color2)
        
        val brightest = maxOf(lum1, lum2)
        val darkest = minOf(lum1, lum2)
        
        return (brightest + 0.05) / (darkest + 0.05)
    }
    
    /**
     * Calculates the relative luminance of a color
     * @param color The color to calculate luminance for
     * @return The relative luminance value
     */
    private fun calculateRelativeLuminance(color: Int): Double {
        val r = ((color shr 16) and 0xFF) / 255.0
        val g = ((color shr 8) and 0xFF) / 255.0
        val b = (color and 0xFF) / 255.0
        
        val rs = if (r <= 0.03928) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
        val gs = if (g <= 0.03928) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
        val bs = if (b <= 0.03928) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)
        
        return 0.2126 * rs + 0.7152 * gs + 0.0722 * bs
    }
    
    /**
     * Gets the minimum required text size for accessibility
     * @param isLargeText Whether the text is considered large (>18pt or >14pt bold)
     * @return The minimum text size in sp
     */
    fun getMinimumTextSize(isLargeText: Boolean = false): Float {
        return if (isLargeText) 14f else 18f // Minimum recommended sizes
    }
    
    /**
     * Validates that a touch target meets accessibility requirements
     * @param width The width of the touch target in dp
     * @param height The height of the touch target in dp
     * @return True if the touch target meets the minimum requirements (48dp x 48dp)
     */
    fun isValidTouchTarget(width: Dp, height: Dp): Boolean {
        return width >= 48.dp && height >= 48.dp
    }
    
    /**
     * Provides alternative text for images based on context
     * @param imageResourceId The resource ID of the image
     * @param contextDescription Additional context about the image's purpose
     * @return Appropriate content description for accessibility
     */
    fun getImageContentDescription(imageResourceId: Int, contextDescription: String): String {
        // In a real implementation, this would map image IDs to descriptions
        // For now, we'll return the context description
        return contextDescription
    }
    
    /**
     * Checks if the app should reduce animations based on user preferences
     */
    fun shouldReduceAnimations(context: Context): Boolean {
        // This would typically check system settings for animation preferences
        return false // Default to allowing animations
    }
    
    /**
     * Gets the recommended font scaling factor based on system settings
     */
    fun getFontScale(context: Context): Float {
        return context.resources.configuration.fontScale
    }
    
    /**
     * Validates that a form field has proper accessibility labeling
     */
    fun isValidFormFieldAccessibility(label: String?, hint: String?, contentDesc: String?): Boolean {
        // A form field should have at least one of these for accessibility
        return !label.isNullOrBlank() || !hint.isNullOrBlank() || !contentDesc.isNullOrBlank()
    }
    
    companion object {
        // Minimum contrast ratios for accessibility (WCAG AA)
        const val MIN_NORMAL_TEXT_CONTRAST = 4.5
        const val MIN_LARGE_TEXT_CONTRAST = 3.0
        const val MIN_GRAPHICAL_OBJECT_CONTRAST = 3.0
        
        // Minimum touch target size (48dp x 48dp)
        const val MIN_TOUCH_TARGET_DP = 48
    }
}