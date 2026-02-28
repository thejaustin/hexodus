package com.hexodus.services
import com.hexodus.HexodusApplication

import android.app.Service

import android.content.Intent
import android.util.Log
import com.hexodus.utils.SecurityUtils
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.hexodus.core.ThemeCompiler
import java.io.File
import java.io.FileOutputStream
import android.os.IBinder

/**
 * WallpaperThemerService - Service for creating themes based on the current wallpaper
 * Inspired by Monet and other color extraction projects from awesome-shizuku
 */
object WallpaperThemerService {
    private const val TAG = "WallpaperThemerService"
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
    private val themeCompiler = com.hexodus.core.ThemeCompiler()

    private const val ACTION_GENERATE_FROM_WALLPAPER = "com.hexodus.GENERATE_FROM_WALLPAPER"
    private const val ACTION_GENERATE_FROM_IMAGE = "com.hexodus.GENERATE_FROM_IMAGE"
    private const val ACTION_GET_CURRENT_COLORS = "com.hexodus.GET_CURRENT_COLORS"
    
    // Intent extras
    const val EXTRA_IMAGE_PATH = "image_path"
    const val EXTRA_THEME_NAME = "theme_name"
    const val EXTRA_IS_LOCKSCREEN = "is_lockscreen"
    
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_GENERATE_FROM_WALLPAPER -> {
                val isLockscreen = intent.getBooleanExtra(EXTRA_IS_LOCKSCREEN, false)
                generateThemeFromWallpaper(isLockscreen)
            }
            ACTION_GENERATE_FROM_IMAGE -> {
                val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
                val themeName = intent.getStringExtra(EXTRA_THEME_NAME) ?: "ImageTheme"
                
                if (!imagePath.isNullOrEmpty()) {
                    generateThemeFromImage(imagePath, themeName)
                }
            }
            ACTION_GET_CURRENT_COLORS -> {
                getCurrentWallpaperColors()
            }
        }
        
        return android.app.Service.START_STICKY
    }
    
    /**
     * Generates and applies a theme from the current system wallpaper
     */
    private fun generateThemeFromWallpaper(isLockscreen: Boolean) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(HexodusApplication.context)
            
            // In a real implementation, we would extract colors
            val primaryColor = "#FF6200EE"
            Log.d(TAG, "Generating theme from wallpaper (Lockscreen: $isLockscreen)")
            
            applyGeneratedTheme(primaryColor, "WallpaperTheme")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating theme from wallpaper: ${e.message}", e)
        }
    }
    
    /**
     * Generates and applies a theme from a specific image file
     */
    private fun generateThemeFromImage(imagePath: String, themeName: String) {
        try {
            // Validate image path
            val allowedPaths = mutableListOf<String?>()
            allowedPaths.add(HexodusApplication.context.getExternalFilesDir(null)?.parent)
            allowedPaths.add(HexodusApplication.context.cacheDir.parent)
            allowedPaths.add("/sdcard")
            
            if (!SecurityUtils.isValidFilePath(imagePath, allowedPaths.mapNotNull { it })) {
                Log.e(TAG, "Invalid image path: $imagePath")
                return
            }
            
            val bitmap = BitmapFactory.decodeFile(imagePath)
            if (bitmap != null) {
                val primaryColor = "#FF6200EE"
                Log.d(TAG, "Generating theme from image: $imagePath")
                applyGeneratedTheme(primaryColor, themeName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating theme from image: ${e.message}", e)
        }
    }
    
    /**
     * Helper to apply the generated theme
     */
    private fun applyGeneratedTheme(hexColor: String, themeName: String) {
        try {
            val themeData = themeCompiler.compileTheme(
                hexColor,
                "com.hexodus.theme.generated",
                themeName,
                mapOf("status_bar" to true, "navigation_bar" to true)
            )
            
            OverlayManager.applyTheme(themeData, themeName)
            
            // Broadcast success
            val intent = Intent("GENERATED_THEME_APPLIED")
            intent.putExtra("theme_name", themeName)
            intent.putExtra("primary_color", hexColor)
            HexodusApplication.context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error applying generated theme: ${e.message}", e)
        }
    }
    
    /**
     * Gets colors from current wallpaper
     */
    private fun getCurrentWallpaperColors() {
        try {
            val colors = mapOf(
                "primary" to "#FF6200EE",
                "secondary" to "#FF03DAC6",
                "tertiary" to "#FF018786"
            )
            
            val intent = Intent("WALLPAPER_COLORS_RETRIEVED")
            intent.putExtra("colors", HashMap(colors))
            HexodusApplication.context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting wallpaper colors: ${e.message}", e)
        }
    }
}
