package com.hexodus.utils

import android.graphics.Color

/**
 * Utility functions for color manipulation
 */
object ColorUtils {

    private const val RED_WEIGHT = 0.299f
    private const val GREEN_WEIGHT = 0.587f
    private const val BLUE_WEIGHT = 0.114f
    private const val DARKNESS_THRESHOLD = 0.5f

    /**
     * Shifts a color by a factor, making it lighter or darker
     */
    fun shiftColor(color: Int, factor: Float, lighter: Boolean = true): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        // Extract brightness (value in HSV)
        var brightness = hsv[2]

        // Adjust brightness based on factor and direction
        // The factor represents the proportion of the range to shift
        val adjustment = factor.coerceIn(0f, 1f)

        if (lighter) {
            brightness = brightness + (1.0f - brightness) * adjustment
        } else {
            brightness = brightness - brightness * adjustment
        }

        hsv[2] = brightness.coerceIn(0f, 1f)

        return Color.HSVToColor(hsv)
    }
    
    /**
     * Rotates the hue of a color by specified degrees
     */
    fun rotateHue(color: Int, degrees: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[0] = (hsv[0] + degrees) % 360f
        if (hsv[0] < 0) hsv[0] += 360f
        return Color.HSVToColor(hsv)
    }
    
    /**
     * Desaturates a color by a factor (0.0 = grayscale, 1.0 = original)
     */
    fun desaturate(color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[1] = hsv[1] * factor
        return Color.HSVToColor(hsv)
    }

    /**
     * Determines if a color is light or dark
     */
    fun isColorLight(color: Int): Boolean {
        val darkness = 1 - (RED_WEIGHT * Color.red(color) + GREEN_WEIGHT * Color.green(color) + BLUE_WEIGHT * Color.blue(color)) / 255
        return darkness < DARKNESS_THRESHOLD
    }
    
    /**
     * Blends two colors with a specified ratio
     */
    fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        
        val r = (Color.red(color1) * ratio + Color.red(color2) * inverseRatio).toInt()
        val g = (Color.green(color1) * ratio + Color.green(color2) * inverseRatio).toInt()
        val b = (Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio).toInt()
        
        return Color.rgb(r, g, b)
    }
    
    /**
     * Converts hex string to color integer
     */
    fun hexToColor(hex: String): Int {
        return try {
            Color.parseColor(if (hex.startsWith("#")) hex else "#$hex")
        } catch (e: IllegalArgumentException) {
            // Return a default color if parsing fails
            Color.BLACK
        }
    }
    
    /**
     * Converts color integer to hex string
     */
    fun colorToHex(color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }
}