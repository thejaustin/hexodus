package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.utils.SecurityUtils

/**
 * MediaNotificationService - Service for media and notification customization
 * Inspired by AmbientMusicMod project from awesome-shizuku for media features
 */
class MediaNotificationService : Service() {
    
    companion object {
        private const val TAG = "MediaNotificationService"
        private const val ACTION_UPDATE_NOW_PLAYING = "com.hexodus.UPDATE_NOW_PLAYING"
        private const val ACTION_HIDE_NOTIFICATION = "com.hexodus.HIDE_NOTIFICATION"
        private const val ACTION_SHOW_NOTIFICATION = "com.hexodus.SHOW_NOTIFICATION"
        private const val ACTION_CUSTOMIZE_NOTIFICATION = "com.hexodus.CUSTOMIZE_NOTIFICATION"
        
        // Intent extras
        const val EXTRA_TRACK_TITLE = "track_title"
        const val EXTRA_TRACK_ARTIST = "track_artist"
        const val EXTRA_ALBUM_ART = "album_art"
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_CUSTOMIZATION_CONFIG = "customization_config"
    }
    
    private lateinit var shizukuBridgeService: ShizukuBridgeService
    
    override fun onCreate() {
        super.onCreate()
        shizukuBridgeService = ShizukuBridgeService()
        Log.d(TAG, "MediaNotificationService created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_UPDATE_NOW_PLAYING -> {
                val title = intent.getStringExtra(EXTRA_TRACK_TITLE)
                val artist = intent.getStringExtra(EXTRA_TRACK_ARTIST)
                val albumArt = intent.getStringExtra(EXTRA_ALBUM_ART)
                
                updateNowPlaying(title, artist, albumArt)
            }
            ACTION_HIDE_NOTIFICATION -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
                
                if (!packageName.isNullOrEmpty()) {
                    hideNotification(packageName, notificationId)
                }
            }
            ACTION_SHOW_NOTIFICATION -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
                
                if (!packageName.isNullOrEmpty()) {
                    showNotification(packageName, notificationId)
                }
            }
            ACTION_CUSTOMIZE_NOTIFICATION -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val config = intent.getStringExtra(EXTRA_CUSTOMIZATION_CONFIG)
                
                if (!packageName.isNullOrEmpty() && !config.isNullOrEmpty()) {
                    customizeNotification(packageName, config)
                }
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Updates the now playing information
     */
    private fun updateNowPlaying(title: String?, artist: String?, albumArt: String?) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (title != null && SecurityUtils.containsDangerousChars(title)) {
                Log.e(TAG, "Dangerous characters detected in title")
                return
            }
            if (artist != null && SecurityUtils.containsDangerousChars(artist)) {
                Log.e(TAG, "Dangerous characters detected in artist")
                return
            }
            
            // In a real implementation, this would interact with the system's media session
            // For this example, we'll just log the action
            Log.d(TAG, "Now playing updated: $title by $artist")
            
            // Broadcast success
            val successIntent = Intent("NOW_PLAYING_UPDATED")
            successIntent.putExtra("track_title", title)
            successIntent.putExtra("track_artist", artist)
            successIntent.putExtra("album_art", albumArt)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating now playing: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("NOW_PLAYING_UPDATE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Hides a notification using Shizuku
     */
    private fun hideNotification(packageName: String, notificationId: Int) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            // In a real implementation, this would use NotificationManagerService to cancel notifications
            // For this example, we'll simulate the action
            Log.d(TAG, "Hiding notification: $sanitizedPackageName:$notificationId")
            
            // Broadcast success
            val successIntent = Intent("NOTIFICATION_HIDDEN")
            successIntent.putExtra("package_name", sanitizedPackageName)
            successIntent.putExtra("notification_id", notificationId)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding notification: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("NOTIFICATION_HIDE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Shows a notification using Shizuku
     */
    private fun showNotification(packageName: String, notificationId: Int) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            // In a real implementation, this would interact with the notification system
            // For this example, we'll simulate the action
            Log.d(TAG, "Showing notification: $sanitizedPackageName:$notificationId")
            
            // Broadcast success
            val successIntent = Intent("NOTIFICATION_SHOWN")
            successIntent.putExtra("package_name", sanitizedPackageName)
            successIntent.putExtra("notification_id", notificationId)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("NOTIFICATION_SHOW_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Customizes a notification using Shizuku
     */
    private fun customizeNotification(packageName: String, config: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            if (SecurityUtils.containsDangerousChars(config)) {
                Log.e(TAG, "Dangerous characters detected in config")
                return
            }
            
            // In a real implementation, this would modify notification properties
            // For this example, we'll simulate the action
            Log.d(TAG, "Customizing notification for: $sanitizedPackageName with config: $config")
            
            // Broadcast success
            val successIntent = Intent("NOTIFICATION_CUSTOMIZED")
            successIntent.putExtra("package_name", sanitizedPackageName)
            successIntent.putExtra("config", config)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error customizing notification: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("NOTIFICATION_CUSTOMIZE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Gets the current media session information
     */
    fun getCurrentMediaSession(): Map<String, String> {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return emptyMap()
            }
            
            // In a real implementation, this would query the media session service
            // For this example, we'll return a mock response
            return mapOf(
                "title" to "Mock Track Title",
                "artist" to "Mock Artist",
                "album" to "Mock Album",
                "package" to "com.mock.player"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting media session: ${e.message}", e)
            return emptyMap()
        }
    }
    
    /**
     * Gets active notifications
     */
    fun getActiveNotifications(): List<Map<String, Any>> {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return emptyList()
            }
            
            // In a real implementation, this would query the notification service
            // For this example, we'll return a mock response
            return listOf(
                mapOf(
                    "id" to 1,
                    "package" to "com.spotify.music",
                    "title" to "Currently Playing",
                    "text" to "Artist - Song Title"
                ),
                mapOf(
                    "id" to 2,
                    "package" to "com.google.android.youtube",
                    "title" to "Video Playing",
                    "text" to "Channel - Video Title"
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active notifications: ${e.message}", e)
            return emptyList()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MediaNotificationService destroyed")
    }
}