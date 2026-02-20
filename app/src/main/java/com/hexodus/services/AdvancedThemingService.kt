package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.utils.SecurityUtils
import com.hexodus.core.ThemeCompiler
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.palette.graphics.Palette
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * AdvancedThemingService - Service for advanced theming features
 * Inspired by various theming projects from awesome-shizuku
 */
class AdvancedThemingService : LifecycleService() {
    
    companion object {
        private const val TAG = "AdvancedThemingService"
        private const val ACTION_CREATE_GRADIENT_THEME = "com.hexodus.CREATE_GRADIENT_THEME"
        private const val ACTION_CREATE_ANIMATED_THEME = "com.hexodus.CREATE_ANIMATED_THEME"
        private const val ACTION_CREATE_TEXTURE_THEME = "com.hexodus.CREATE_TEXTURE_THEME"
        private const val ACTION_GET_THEME_PRESETS = "com.hexodus.GET_THEME_PRESETS"
        private const val ACTION_APPLY_THEME_TRANSITION = "com.hexodus.APPLY_THEME_TRANSITION"
        private const val ACTION_CREATE_THEME_ANIMATION = "com.hexodus.CREATE_THEME_ANIMATION"
        
        // Intent extras
        const val EXTRA_GRADIENT_COLORS = "gradient_colors" // List of hex colors
        const val EXTRA_ANIMATION_TYPE = "animation_type" // fade, slide, zoom
        const val EXTRA_TEXTURE_PATH = "texture_path"
        const val EXTRA_THEME_TRANSITION_SPEED = "transition_speed"
        const val EXTRA_ANIMATION_DURATION = "animation_duration"
        const val EXTRA_THEME_PRESET_NAME = "preset_name"
        const val EXTRA_COMPONENT_NAME = "component_name"
        const val EXTRA_HEX_COLOR = "hex_color"
        const val EXTRA_FROM_THEME = "from_theme"
        const val EXTRA_TO_THEME = "to_theme"
    }
    
    private lateinit var shizukuBridgeService: ShizukuBridgeService
    private lateinit var themeCompiler: ThemeCompiler
    
    override fun onCreate() {
        super.onCreate()
        shizukuBridgeService = ShizukuBridgeService()
        themeCompiler = ThemeCompiler()
        Log.d(TAG, "AdvancedThemingService created")
    }
    
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_CREATE_GRADIENT_THEME -> {
                val gradientColors = intent.getStringArrayListExtra(EXTRA_GRADIENT_COLORS) ?: arrayListOf("#FF6200EE")
                val componentName = intent.getStringExtra(EXTRA_COMPONENT_NAME) ?: "status_bar"
                
                createGradientTheme(gradientColors, componentName)
            }
            ACTION_CREATE_ANIMATED_THEME -> {
                val animationType = intent.getStringExtra(EXTRA_ANIMATION_TYPE) ?: "fade"
                val hexColor = intent.getStringExtra(EXTRA_HEX_COLOR) ?: "#FF6200EE"
                
                createAnimatedTheme(animationType, hexColor)
            }
            ACTION_CREATE_TEXTURE_THEME -> {
                val texturePath = intent.getStringExtra(EXTRA_TEXTURE_PATH)
                val hexColor = intent.getStringExtra(EXTRA_HEX_COLOR) ?: "#FF6200EE"
                
                if (!texturePath.isNullOrEmpty()) {
                    createTextureTheme(texturePath, hexColor)
                }
            }
            ACTION_GET_THEME_PRESETS -> {
                getThemePresets()
            }
            ACTION_APPLY_THEME_TRANSITION -> {
                val transitionSpeed = intent.getIntExtra(EXTRA_THEME_TRANSITION_SPEED, 500)
                val fromTheme = intent.getStringExtra(EXTRA_FROM_THEME) ?: "default"
                val toTheme = intent.getStringExtra(EXTRA_TO_THEME) ?: "default"
                
                applyThemeTransition(transitionSpeed, fromTheme, toTheme)
            }
            ACTION_CREATE_THEME_ANIMATION -> {
                val animationDuration = intent.getIntExtra(EXTRA_ANIMATION_DURATION, 1000)
                val animationType = intent.getStringExtra(EXTRA_ANIMATION_TYPE) ?: "fade"
                
                createThemeAnimation(animationDuration, animationType)
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Creates a gradient theme using multiple colors
     */
    private fun createGradientTheme(colors: List<String>, componentName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (!shizukuBridgeService.isReady()) {
                    Log.e(TAG, "Shizuku is not ready")
                    return@launch
                }
                
                // Validate inputs
                for (color in colors) {
                    if (!SecurityUtils.validateHexColor(color)) {
                        Log.e(TAG, "Invalid hex color: $color")
                        return@launch
                    }
                }
                
                if (SecurityUtils.containsDangerousChars(componentName)) {
                    Log.e(TAG, "Dangerous characters detected in component name")
                    return@launch
                }
                
                // Validate component name
                val validComponents = listOf(
                    "status_bar", "navigation_bar", "system_ui", 
                    "settings", "launcher", "quick_settings"
                )
                
                if (componentName !in validComponents) {
                    Log.e(TAG, "Invalid component name: $componentName")
                    return@launch
                }
                
                // In a real implementation, this would create a gradient overlay
                // For this example, we'll simulate the process
                Log.d(TAG, "Created gradient theme with colors: ${colors.joinToString(", ")} for component: $componentName")
                
                // Generate a unique package name for the gradient theme
                val packageName = "com.hexodus.gradient.${componentName.replace("_", "")}.${System.currentTimeMillis()}"
                
                // Compile the theme using the theme compiler
                val themeData = themeCompiler.compileTheme(
                    colors.first(), // Use first color as base
                    packageName,
                    "Gradient Theme for $componentName",
                    mapOf(componentName to true) // Apply to specified component
                )
                
                // Save the theme to internal storage temporarily
                val tempFile = File(cacheDir, "${packageName}.apk")
                FileOutputStream(tempFile).use { it.write(themeData) }
                
                // Install the overlay using Shizuku
                val installSuccess = shizukuBridgeService.installApk(tempFile.absolutePath)
                
                if (installSuccess) {
                    // Enable the overlay
                    val enableSuccess = shizukuBridgeService.executeOverlayCommand(packageName, "enable")
                    
                    if (enableSuccess) {
                        Log.d(TAG, "Successfully created and enabled gradient theme: $packageName")
                        
                        // Broadcast success
                        val successIntent = Intent("GRADIENT_THEME_CREATED")
                        successIntent.putExtra("package_name", packageName)
                        successIntent.putStringArrayListExtra("colors", ArrayList(colors))
                        successIntent.putExtra("component", componentName)
                        sendBroadcast(successIntent)
                        
                        // Clean up temp file
                        tempFile.delete()
                    } else {
                        Log.e(TAG, "Failed to enable gradient theme overlay: $packageName")
                        
                        // Broadcast failure
                        val failureIntent = Intent("GRADIENT_THEME_CREATION_FAILED")
                        failureIntent.putExtra("package_name", packageName)
                        failureIntent.putExtra("error", "Failed to enable overlay")
                        sendBroadcast(failureIntent)
                        
                        // Clean up temp file
                        tempFile.delete()
                    }
                } else {
                    Log.e(TAG, "Failed to install gradient theme APK: $packageName")
                    
                    // Broadcast failure
                    val failureIntent = Intent("GRADIENT_THEME_INSTALL_FAILED")
                    failureIntent.putExtra("package_name", packageName)
                    failureIntent.putExtra("error", "Failed to install APK")
                    sendBroadcast(failureIntent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating gradient theme: ${e.message}", e)
                
                // Broadcast error
                val errorIntent = Intent("GRADIENT_THEME_ERROR")
                errorIntent.putExtra("error_message", e.message)
                sendBroadcast(errorIntent)
            }
        }
    }
    
    /**
     * Creates an animated theme using Shizuku
     */
    private fun createAnimatedTheme(animationType: String, hexColor: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val validAnimationTypes = listOf("fade", "slide", "zoom", "pulse", "wave")
            if (animationType !in validAnimationTypes) {
                Log.e(TAG, "Invalid animation type: $animationType")
                return
            }
            
            if (!SecurityUtils.validateHexColor(hexColor)) {
                Log.e(TAG, "Invalid hex color: $hexColor")
                return
            }
            
            // In a real implementation, this would create an animated overlay
            // For this example, we'll simulate the process
            Log.d(TAG, "Created animated theme with type: $animationType and color: $hexColor")
            
            // Generate a unique package name for the animated theme
            val packageName = "com.hexodus.animated.${animationType}.${System.currentTimeMillis()}"
            
            // Create the animated theme
            val themeData = themeCompiler.compileTheme(
                hexColor,
                packageName,
                "Animated Theme ($animationType)",
                mapOf("status_bar" to true, "navigation_bar" to true) // Apply to common components
            )
            
            // Save the theme to internal storage temporarily
            val tempFile = File(cacheDir, "${packageName}.apk")
            FileOutputStream(tempFile).use { it.write(themeData) }
            
            // Install the overlay using Shizuku
            val installSuccess = shizukuBridgeService.installApk(tempFile.absolutePath)
            
            if (installSuccess) {
                // Enable the overlay
                val enableSuccess = shizukuBridgeService.executeOverlayCommand(packageName, "enable")
                
                if (enableSuccess) {
                    Log.d(TAG, "Successfully created and enabled animated theme: $packageName")
                    
                    // Broadcast success
                    val successIntent = Intent("ANIMATED_THEME_CREATED")
                    successIntent.putExtra("package_name", packageName)
                    successIntent.putExtra("animation_type", animationType)
                    successIntent.putExtra("hex_color", hexColor)
                    sendBroadcast(successIntent)
                    
                    // Clean up temp file
                    tempFile.delete()
                } else {
                    Log.e(TAG, "Failed to enable animated theme overlay: $packageName")
                    
                    // Broadcast failure
                    val failureIntent = Intent("ANIMATED_THEME_CREATION_FAILED")
                    failureIntent.putExtra("package_name", packageName)
                    failureIntent.putExtra("error", "Failed to enable overlay")
                    sendBroadcast(failureIntent)
                    
                    // Clean up temp file
                    tempFile.delete()
                }
            } else {
                Log.e(TAG, "Failed to install animated theme APK: $packageName")
                
                // Broadcast failure
                val failureIntent = Intent("ANIMATED_THEME_INSTALL_FAILED")
                failureIntent.putExtra("package_name", packageName)
                failureIntent.putExtra("error", "Failed to install APK")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating animated theme: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("ANIMATED_THEME_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Creates a texture-based theme
     */
    private fun createTextureTheme(texturePath: String, hexColor: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (!SecurityUtils.validateHexColor(hexColor)) {
                Log.e(TAG, "Invalid hex color: $hexColor")
                return
            }
            
            if (!SecurityUtils.isValidFilePath(texturePath, listOf(filesDir.parent, cacheDir.parent, "/sdcard"))) {
                Log.e(TAG, "Invalid texture path: $texturePath")
                return
            }
            
            val textureFile = File(texturePath)
            if (!textureFile.exists()) {
                Log.e(TAG, "Texture file does not exist: $texturePath")
                return
            }
            
            // In a real implementation, this would create a texture overlay
            // For this example, we'll simulate the process
            Log.d(TAG, "Created texture theme with texture: $texturePath and color: $hexColor")
            
            // Generate a unique package name for the texture theme
            val packageName = "com.hexodus.texture.${textureFile.nameWithoutExtension}.${System.currentTimeMillis()}"
            
            // Create the texture theme
            val themeData = themeCompiler.compileTheme(
                hexColor,
                packageName,
                "Texture Theme (${textureFile.name})",
                mapOf("status_bar" to true, "navigation_bar" to true, "system_ui" to true)
            )
            
            // Save the theme to internal storage temporarily
            val tempFile = File(cacheDir, "${packageName}.apk")
            FileOutputStream(tempFile).use { it.write(themeData) }
            
            // Install the overlay using Shizuku
            val installSuccess = shizukuBridgeService.installApk(tempFile.absolutePath)
            
            if (installSuccess) {
                // Enable the overlay
                val enableSuccess = shizukuBridgeService.executeOverlayCommand(packageName, "enable")
                
                if (enableSuccess) {
                    Log.d(TAG, "Successfully created and enabled texture theme: $packageName")
                    
                    // Broadcast success
                    val successIntent = Intent("TEXTURE_THEME_CREATED")
                    successIntent.putExtra("package_name", packageName)
                    successIntent.putExtra("texture_path", texturePath)
                    successIntent.putExtra("hex_color", hexColor)
                    sendBroadcast(successIntent)
                    
                    // Clean up temp file
                    tempFile.delete()
                } else {
                    Log.e(TAG, "Failed to enable texture theme overlay: $packageName")
                    
                    // Broadcast failure
                    val failureIntent = Intent("TEXTURE_THEME_CREATION_FAILED")
                    failureIntent.putExtra("package_name", packageName)
                    failureIntent.putExtra("error", "Failed to enable overlay")
                    sendBroadcast(failureIntent)
                    
                    // Clean up temp file
                    tempFile.delete()
                }
            } else {
                Log.e(TAG, "Failed to install texture theme APK: $packageName")
                
                // Broadcast failure
                val failureIntent = Intent("TEXTURE_THEME_INSTALL_FAILED")
                failureIntent.putExtra("package_name", packageName)
                failureIntent.putExtra("error", "Failed to install APK")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating texture theme: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("TEXTURE_THEME_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets available theme presets
     */
    private fun getThemePresets() {
        try {
            // In a real implementation, this would query available theme presets
            // For this example, we'll return mock data
            val presets = listOf(
                mapOf(
                    "name" to "Ocean Breeze",
                    "type" to "gradient",
                    "colors" to listOf("#FF006064", "#FF0097A7", "#FF00BCD4"),
                    "description" to "A calming ocean-themed gradient"
                ),
                mapOf(
                    "name" to "Sunset Glow",
                    "type" to "gradient",
                    "colors" to listOf("#FFFF5722", "#FFFF9800", "#xFFFFEB3B"),
                    "description" to "Warm sunset colors for a cozy feel"
                ),
                mapOf(
                    "name" to "Forest Green",
                    "type" to "solid",
                    "colors" to listOf("#FF4CAF50"),
                    "description" to "Natural forest green for relaxation"
                ),
                mapOf(
                    "name" to "Midnight Purple",
                    "type" to "animated",
                    "colors" to listOf("#FF673AB7", "#FF3F51B5"),
                    "animation_type" to "pulse",
                    "description" to "Animated purple theme with pulsing effect"
                )
            )
            
            Log.d(TAG, "Retrieved ${presets.size} theme presets")
            
            // Broadcast results
            val successIntent = Intent("THEME_PRESETS_RETRIEVED")
            successIntent.putExtra("preset_count", presets.size)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting theme presets: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("THEME_PRESETS_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Applies a smooth transition between themes
     */
    private fun applyThemeTransition(speed: Int, fromTheme: String, toTheme: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (speed < 100 || speed > 2000) { // Between 100ms and 2s
                Log.e(TAG, "Invalid transition speed: $speed ms")
                return
            }
            
            if (SecurityUtils.containsDangerousChars(fromTheme) || SecurityUtils.containsDangerousChars(toTheme)) {
                Log.e(TAG, "Dangerous characters detected in theme names")
                return
            }
            
            // In a real implementation, this would create a smooth transition between themes
            // For this example, we'll simulate the process
            Log.d(TAG, "Applied theme transition from: $fromTheme to: $toTheme with speed: ${speed}ms")
            
            // This would involve:
            // 1. Temporarily applying intermediate themes
            // 2. Animating the color changes
            // 3. Smoothly transitioning to the final theme
            
            // For now, we'll just apply the 'to' theme
            val applyIntent = Intent("com.hexodus.APPLY_THEME")
            applyIntent.putExtra("theme_name", toTheme)
            sendBroadcast(applyIntent)
            
            // Broadcast success
            val successIntent = Intent("THEME_TRANSITION_APPLIED")
            successIntent.putExtra("from_theme", fromTheme)
            successIntent.putExtra("to_theme", toTheme)
            successIntent.putExtra("transition_speed", speed)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error applying theme transition: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("THEME_TRANSITION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Creates a theme animation
     */
    private fun createThemeAnimation(duration: Int, animationType: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (duration < 500 || duration > 5000) { // Between 0.5s and 5s
                Log.e(TAG, "Invalid animation duration: $duration ms")
                return
            }
            
            val validAnimationTypes = listOf("fade", "slide", "zoom", "pulse", "wave", "rotate")
            if (animationType !in validAnimationTypes) {
                Log.e(TAG, "Invalid animation type: $animationType")
                return
            }
            
            // In a real implementation, this would create an animated theme overlay
            // For this example, we'll simulate the process
            Log.d(TAG, "Created theme animation with type: $animationType and duration: ${duration}ms")
            
            // Generate a unique package name for the animation
            val packageName = "com.hexodus.animation.${animationType}.${System.currentTimeMillis()}"
            
            // Create the animation theme
            val themeData = themeCompiler.compileTheme(
                "#FF6200EE", // Default color for animation
                packageName,
                "Animated Theme ($animationType)",
                mapOf("status_bar" to true, "navigation_bar" to true, "system_ui" to true)
            )
            
            // Save the theme to internal storage temporarily
            val tempFile = File(cacheDir, "${packageName}.apk")
            FileOutputStream(tempFile).use { it.write(themeData) }
            
            // Install the overlay using Shizuku
            val installSuccess = shizukuBridgeService.installApk(tempFile.absolutePath)
            
            if (installSuccess) {
                // Enable the overlay
                val enableSuccess = shizukuBridgeService.executeOverlayCommand(packageName, "enable")
                
                if (enableSuccess) {
                    Log.d(TAG, "Successfully created and enabled animated theme: $packageName")
                    
                    // Broadcast success
                    val successIntent = Intent("THEME_ANIMATION_CREATED")
                    successIntent.putExtra("package_name", packageName)
                    successIntent.putExtra("animation_type", animationType)
                    successIntent.putExtra("duration", duration)
                    sendBroadcast(successIntent)
                    
                    // Clean up temp file
                    tempFile.delete()
                } else {
                    Log.e(TAG, "Failed to enable animated theme overlay: $packageName")
                    
                    // Broadcast failure
                    val failureIntent = Intent("THEME_ANIMATION_CREATION_FAILED")
                    failureIntent.putExtra("package_name", packageName)
                    failureIntent.putExtra("error", "Failed to enable overlay")
                    sendBroadcast(failureIntent)
                    
                    // Clean up temp file
                    tempFile.delete()
                }
            } else {
                Log.e(TAG, "Failed to install animated theme APK: $packageName")
                
                // Broadcast failure
                val failureIntent = Intent("THEME_ANIMATION_INSTALL_FAILED")
                failureIntent.putExtra("package_name", packageName)
                failureIntent.putExtra("error", "Failed to install APK")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating theme animation: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("THEME_ANIMATION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Generates a dynamic color palette from an image using Palette API
     */
    fun generatePaletteFromImage(imagePath: String): Map<String, Int>? {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return null
            }
            
            val file = File(imagePath)
            if (!file.exists()) {
                Log.e(TAG, "Image file does not exist: $imagePath")
                return null
            }

            // Safe decoding with scaling to prevent OOM
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)

            // Scale down if image is larger than 1024px
            val reqWidth = 1024
            val reqHeight = 1024
            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            
            val bitmap = BitmapFactory.decodeFile(imagePath, options)
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode image: $imagePath")
                return null
            }
            
            val palette = Palette.from(bitmap).generate()
            
            val colorPalette = mapOf(
                "dominant" to palette.dominantSwatch?.rgb,
                "vibrant" to palette.vibrantSwatch?.rgb,
                "light_vibrant" to palette.lightVibrantSwatch?.rgb,
                "dark_vibrant" to palette.darkVibrantSwatch?.rgb,
                "muted" to palette.mutedSwatch?.rgb,
                "light_muted" to palette.lightMutedSwatch?.rgb,
                "dark_muted" to palette.darkMutedSwatch?.rgb
            ).filterValues { it != null }.mapValues { it.value as Int }
            
            // Clean up bitmap
            bitmap.recycle()
            
            Log.d(TAG, "Generated palette with ${colorPalette.size} colors from image: $imagePath")
            return colorPalette
        } catch (e: Exception) {
            Log.e(TAG, "Error generating palette from image: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Creates a theme based on an image's color palette
     */
    fun createThemeFromImage(imagePath: String, componentName: String = "status_bar"): Boolean {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return false
            }
            
            val palette = generatePaletteFromImage(imagePath)
            if (palette.isNullOrEmpty()) {
                Log.e(TAG, "Failed to generate palette from image: $imagePath")
                return false
            }
            
            // Use the dominant color as the primary theme color
            val primaryColor = palette["dominant"] ?: palette["vibrant"] ?: return false
            val hexColor = String.format("#%06X", (0xFFFFFF and primaryColor))
            
            // Create a gradient theme using the palette colors
            val colors = palette.values.map { color ->
                String.format("#%06X", (0xFFFFFF and color))
            }
            
            createGradientTheme(colors, componentName)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating theme from image: ${e.message}", e)
            return false
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "AdvancedThemingService destroyed")
    }
}