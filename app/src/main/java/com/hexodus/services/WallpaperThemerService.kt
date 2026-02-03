package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.utils.SecurityUtils
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.palette.graphics.Palette
import java.io.File
import java.io.IOException

/**
 * WallpaperThemerService - Service for wallpaper-based theming
 * Inspired by wallpaper-based theming projects from awesome-shizuku
 */
class WallpaperThemerService : Service() {
    
    companion object {
        private const val TAG = "WallpaperThemerService"
        private const val ACTION_EXTRACT_COLORS = "com.hexodus.EXTRACT_COLORS"
        private const val ACTION_APPLY_WALLPAPER_THEME = "com.hexodus.APPLY_WALLPAPER_THEME"
        private const val ACTION_GET_CURRENT_WALLPAPER_COLORS = "com.hexodus.GET_CURRENT_WALLPAPER_COLORS"
        private const val ACTION_SET_WALLPAPER_FROM_THEME = "com.hexodus.SET_WALLPAPER_FROM_THEME"
        private const val ACTION_GENERATE_PALETTE = "com.hexodus.GENERATE_PALETTE"
        
        // Intent extras
        const val EXTRA_IMAGE_PATH = "image_path"
        const val EXTRA_COLOR_COUNT = "color_count"
        const val EXTRA_THEME_NAME = "theme_name"
        const val EXTRA_PALETTE_TYPE = "palette_type" // vibrant, muted, dominant, all
    }
    
    private lateinit var shizukuBridgeService: ShizukuBridgeService
    private lateinit var wallpaperManager: WallpaperManager
    
    override fun onCreate() {
        super.onCreate()
        shizukuBridgeService = ShizukuBridgeService()
        wallpaperManager = WallpaperManager.getInstance(this)
        Log.d(TAG, "WallpaperThemerService created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_EXTRACT_COLORS -> {
                val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
                val colorCount = intent.getIntExtra(EXTRA_COLOR_COUNT, 5)
                
                if (!imagePath.isNullOrEmpty()) {
                    extractColorsFromImage(imagePath, colorCount)
                }
            }
            ACTION_APPLY_WALLPAPER_THEME -> {
                val themeName = intent.getStringExtra(EXTRA_THEME_NAME)
                
                if (!themeName.isNullOrEmpty()) {
                    applyWallpaperTheme(themeName)
                }
            }
            ACTION_GET_CURRENT_WALLPAPER_COLORS -> {
                getCurrentWallpaperColors()
            }
            ACTION_SET_WALLPAPER_FROM_THEME -> {
                val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
                
                if (!imagePath.isNullOrEmpty()) {
                    setWallpaperFromTheme(imagePath)
                }
            }
            ACTION_GENERATE_PALETTE -> {
                val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
                val paletteType = intent.getStringExtra(EXTRA_PALETTE_TYPE) ?: "all"
                
                if (!imagePath.isNullOrEmpty()) {
                    generatePalette(imagePath, paletteType)
                }
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Extracts colors from an image using Palette API
     */
    private fun extractColorsFromImage(imagePath: String, colorCount: Int) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (!SecurityUtils.isValidFilePath(imagePath, listOf(filesDir.parent, cacheDir.parent, "/sdcard"))) {
                Log.e(TAG, "Invalid image path: $imagePath")
                return
            }
            
            val imageFile = File(imagePath)
            if (!imageFile.exists()) {
                Log.e(TAG, "Image file does not exist: $imagePath")
                return
            }
            
            // Load the image
            val bitmap = BitmapFactory.decodeFile(imagePath)
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode image: $imagePath")
                return
            }
            
            // Generate palette using Palette API
            val palette = Palette.from(bitmap).generate()
            
            // Extract colors based on requested count
            val extractedColors = mutableListOf<Int>()
            
            // Add dominant color
            palette.dominantSwatch?.let { extractedColors.add(it.rgb) }
            
            // Add vibrant colors
            palette.vibrantSwatch?.let { extractedColors.add(it.rgb) }
            palette.lightVibrantSwatch?.let { extractedColors.add(it.rgb) }
            palette.darkVibrantSwatch?.let { extractedColors.add(it.rgb) }
            
            // Add muted colors
            palette.mutedSwatch?.let { extractedColors.add(it.rgb) }
            palette.lightMutedSwatch?.let { extractedColors.add(it.rgb) }
            palette.darkMutedSwatch?.let { extractedColors.add(it.rgb) }
            
            // Limit to requested count
            val finalColors = extractedColors.take(colorCount)
            
            Log.d(TAG, "Extracted ${finalColors.size} colors from image: $imagePath")
            
            // Broadcast results
            val successIntent = Intent("COLORS_EXTRACTED")
            successIntent.putExtra("image_path", imagePath)
            successIntent.putIntegerArrayListExtra("colors", ArrayList(finalColors))
            sendBroadcast(successIntent)
            
            // Clean up bitmap
            bitmap.recycle()
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting colors from image: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("COLOR_EXTRACTION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Applies a wallpaper-based theme using Shizuku
     */
    private fun applyWallpaperTheme(themeName: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate theme name
            if (SecurityUtils.containsDangerousChars(themeName)) {
                Log.e(TAG, "Dangerous characters detected in theme name")
                return
            }
            
            // In a real implementation, this would apply the wallpaper-based theme
            // For this example, we'll simulate the process
            Log.d(TAG, "Applied wallpaper-based theme: $themeName")
            
            // Broadcast success
            val successIntent = Intent("WALLPAPER_THEME_APPLIED")
            successIntent.putExtra("theme_name", themeName)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error applying wallpaper theme: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("WALLPAPER_THEME_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets colors from the current wallpaper
     */
    private fun getCurrentWallpaperColors() {
        try {
            // Get current wallpaper as bitmap
            val wallpaperBitmap = wallpaperManager.drawable?.let { drawable ->
                // Convert drawable to bitmap
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
            
            if (wallpaperBitmap != null) {
                // Generate palette from current wallpaper
                val palette = Palette.from(wallpaperBitmap).generate()
                
                // Extract key colors
                val colors = mapOf(
                    "dominant" to palette.dominantSwatch?.rgb,
                    "vibrant" to palette.vibrantSwatch?.rgb,
                    "light_vibrant" to palette.lightVibrantSwatch?.rgb,
                    "dark_vibrant" to palette.darkVibrantSwatch?.rgb,
                    "muted" to palette.mutedSwatch?.rgb,
                    "light_muted" to palette.lightMutedSwatch?.rgb,
                    "dark_muted" to palette.darkMutedSwatch?.rgb
                ).filterValues { it != null }.mapValues { it.value as Int }
                
                Log.d(TAG, "Retrieved colors from current wallpaper")
                
                // Broadcast results
                val successIntent = Intent("CURRENT_WALLPAPER_COLORS_RETRIEVED")
                successIntent.putExtra("colors", HashMap(colors))
                sendBroadcast(successIntent)
                
                // Clean up bitmap
                wallpaperBitmap.recycle()
            } else {
                Log.e(TAG, "Failed to get current wallpaper as bitmap")
                
                // Broadcast error
                val errorIntent = Intent("CURRENT_WALLPAPER_COLORS_ERROR")
                errorIntent.putExtra("error", "Failed to retrieve wallpaper")
                sendBroadcast(errorIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current wallpaper colors: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("CURRENT_WALLPAPER_COLORS_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Sets wallpaper from a theme
     */
    private fun setWallpaperFromTheme(imagePath: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (!SecurityUtils.isValidFilePath(imagePath, listOf(filesDir.parent, cacheDir.parent, "/sdcard"))) {
                Log.e(TAG, "Invalid image path: $imagePath")
                return
            }
            
            val imageFile = File(imagePath)
            if (!imageFile.exists()) {
                Log.e(TAG, "Image file does not exist: $imagePath")
                return
            }
            
            // In a real implementation, this would set the wallpaper
            // For this example, we'll simulate the process
            Log.d(TAG, "Set wallpaper from theme: $imagePath")
            
            // Broadcast success
            val successIntent = Intent("WALLPAPER_SET_FROM_THEME")
            successIntent.putExtra("image_path", imagePath)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting wallpaper from theme: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("WALLPAPER_SET_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Generates a color palette from an image
     */
    private fun generatePalette(imagePath: String, paletteType: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (!SecurityUtils.isValidFilePath(imagePath, listOf(filesDir.parent, cacheDir.parent, "/sdcard"))) {
                Log.e(TAG, "Invalid image path: $imagePath")
                return
            }
            
            val imageFile = File(imagePath)
            if (!imageFile.exists()) {
                Log.e(TAG, "Image file does not exist: $imagePath")
                return
            }
            
            // Load the image
            val bitmap = BitmapFactory.decodeFile(imagePath)
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode image: $imagePath")
                return
            }
            
            // Generate palette based on type
            val palette = when (paletteType.lowercase()) {
                "vibrant" -> {
                    Palette.from(bitmap).maximumColorCount(8).setRegion(0, 0, bitmap.width, bitmap.height).generate()
                }
                "muted" -> {
                    Palette.from(bitmap).maximumColorCount(8).setRegion(0, 0, bitmap.width, bitmap.height).generate()
                }
                "dominant" -> {
                    Palette.from(bitmap).maximumColorCount(1).setRegion(0, 0, bitmap.width, bitmap.height).generate()
                }
                else -> { // "all"
                    Palette.from(bitmap).maximumColorCount(16).setRegion(0, 0, bitmap.width, bitmap.height).generate()
                }
            }
            
            // Extract colors based on palette type
            val colors = when (paletteType.lowercase()) {
                "vibrant" -> {
                    mapOf(
                        "vibrant" to palette.vibrantSwatch?.rgb,
                        "light_vibrant" to palette.lightVibrantSwatch?.rgb,
                        "dark_vibrant" to palette.darkVibrantSwatch?.rgb
                    )
                }
                "muted" -> {
                    mapOf(
                        "muted" to palette.mutedSwatch?.rgb,
                        "light_muted" to palette.lightMutedSwatch?.rgb,
                        "dark_muted" to palette.darkMutedSwatch?.rgb
                    )
                }
                "dominant" -> {
                    mapOf(
                        "dominant" to palette.dominantSwatch?.rgb
                    )
                }
                else -> { // "all"
                    mapOf(
                        "dominant" to palette.dominantSwatch?.rgb,
                        "vibrant" to palette.vibrantSwatch?.rgb,
                        "light_vibrant" to palette.lightVibrantSwatch?.rgb,
                        "dark_vibrant" to palette.darkVibrantSwatch?.rgb,
                        "muted" to palette.mutedSwatch?.rgb,
                        "light_muted" to palette.lightMutedSwatch?.rgb,
                        "dark_muted" to palette.darkMutedSwatch?.rgb
                    )
                }
            }.filterValues { it != null }.mapValues { it.value as Int }
            
            Log.d(TAG, "Generated ${colors.size} colors from image: $imagePath with type: $paletteType")
            
            // Broadcast results
            val successIntent = Intent("PALETTE_GENERATED")
            successIntent.putExtra("image_path", imagePath)
            successIntent.putExtra("palette_type", paletteType)
            successIntent.putExtra("colors", HashMap(colors))
            sendBroadcast(successIntent)
            
            // Clean up bitmap
            bitmap.recycle()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating palette: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("PALETTE_GENERATION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Generates a theme based on wallpaper colors
     */
    fun generateThemeFromWallpaper(wallpaperPath: String): Map<String, String>? {
        try {
            val bitmap = BitmapFactory.decodeFile(wallpaperPath)
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode wallpaper: $wallpaperPath")
                return null
            }
            
            val palette = Palette.from(bitmap).generate()
            
            // Get the dominant color as the primary theme color
            val dominantColor = palette.dominantSwatch?.rgb ?: return null
            val hexColor = String.format("#%06X", (0xFFFFFF and dominantColor))
            
            // Generate complementary colors
            val complementaryColors = generateComplementaryColors(dominantColor)
            
            val themeConfig = mapOf(
                "primary_color" to hexColor,
                "secondary_color" to complementaryColors["secondary"] ?: "#FF03DAC6",
                "background_color" to complementaryColors["background"] ?: "#FFFFFFFF",
                "surface_color" to complementaryColors["surface"] ?: "#FFFFFFFF",
                "error_color" to complementaryColors["error"] ?: "#FFB00020",
                "on_primary_color" to if (isColorLight(dominantColor)) "#000000" else "#FFFFFF",
                "on_secondary_color" to if (isColorLight(android.graphics.Color.parseColor(complementaryColors["secondary"] ?: "#FF03DAC6"))) "#000000" else "#FFFFFF",
                "on_background_color" to if (isColorLight(android.graphics.Color.parseColor(complementaryColors["background"] ?: "#FFFFFFFF"))) "#000000" else "#FFFFFF",
                "on_surface_color" to if (isColorLight(android.graphics.Color.parseColor(complementaryColors["surface"] ?: "#FFFFFFFF"))) "#000000" else "#FFFFFF",
                "on_error_color" to if (isColorLight(android.graphics.Color.parseColor(complementaryColors["error"] ?: "#FFB00020"))) "#000000" else "#FFFFFF"
            )
            
            // Clean up bitmap
            bitmap.recycle()
            
            return themeConfig
        } catch (e: Exception) {
            Log.e(TAG, "Error generating theme from wallpaper: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Generates complementary colors based on a primary color
     */
    private fun generateComplementaryColors(primaryColor: Int): Map<String, String> {
        val complementaryColors = mutableMapOf<String, String>()
        
        // Generate secondary color (slightly different hue)
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(primaryColor, hsv)
        hsv[0] = (hsv[0] + 30) % 360f // Shift hue by 30 degrees
        val secondaryColor = android.graphics.Color.HSVToColor(hsv)
        complementaryColors["secondary"] = String.format("#%06X", (0xFFFFFF and secondaryColor))
        
        // Generate background color (desaturated and lightened)
        val bgHsv = FloatArray(3)
        android.graphics.Color.colorToHSV(primaryColor, bgHsv)
        bgHsv[1] = bgHsv[1] * 0.1f // Reduce saturation
        bgHsv[2] = minOf(1.0f, bgHsv[2] + 0.8f) // Increase brightness
        val backgroundColor = android.graphics.Color.HSVToColor(bgHsv)
        complementaryColors["background"] = String.format("#%06X", (0xFFFFFF and backgroundColor))
        
        // Generate surface color (similar to background but slightly different)
        val surfaceHsv = FloatArray(3)
        android.graphics.Color.colorToHSV(backgroundColor, surfaceHsv)
        surfaceHsv[2] = minOf(1.0f, surfaceHsv[2] + 0.05f) // Slightly brighter
        val surfaceColor = android.graphics.Color.HSVToColor(surfaceHsv)
        complementaryColors["surface"] = String.format("#%06X", (0xFFFFFF and surfaceColor))
        
        // Generate error color (red-based)
        complementaryColors["error"] = "#FFB00020"
        
        return complementaryColors
    }
    
    /**
     * Checks if a color is light or dark
     */
    private fun isColorLight(color: Int): Boolean {
        val darkness = 1 - (0.299 * android.graphics.Color.red(color) + 0.587 * android.graphics.Color.green(color) + 0.114 * android.graphics.Color.blue(color)) / 255
        return darkness < 0.5
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "WallpaperThemerService destroyed")
    }
}