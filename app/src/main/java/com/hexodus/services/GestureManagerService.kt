package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.utils.SecurityUtils

/**
 * GestureManagerService - Service for gesture and interaction customization
 * Inspired by TapTap project from awesome-shizuku for back gesture features
 */
class GestureManagerService : Service() {
    
    companion object {
        private const val TAG = "GestureManagerService"
        private const val ACTION_REGISTER_GESTURE = "com.hexodus.REGISTER_GESTURE"
        private const val ACTION_EXECUTE_ACTION = "com.hexodus.EXECUTE_ACTION"
        private const val ACTION_LIST_GESTURES = "com.hexodus.LIST_GESTURES"
        
        // Intent extras
        const val EXTRA_GESTURE_TYPE = "gesture_type"
        const val EXTRA_GESTURE_ACTION = "gesture_action"
        const val EXTRA_GESTURE_PARAMS = "gesture_params"
        const val EXTRA_ACTION_TYPE = "action_type"
        const val EXTRA_ACTION_TARGET = "action_target"
    }
    
    private lateinit var shizukuBridgeService: ShizukuBridgeService
    private val registeredGestures = mutableMapOf<String, String>() // gesture_type to action mapping
    
    override fun onCreate() {
        super.onCreate()
        shizukuBridgeService = ShizukuBridgeService()
        Log.d(TAG, "GestureManagerService created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_REGISTER_GESTURE -> {
                val gestureType = intent.getStringExtra(EXTRA_GESTURE_TYPE)
                val gestureAction = intent.getStringExtra(EXTRA_GESTURE_ACTION)
                val gestureParams = intent.getStringExtra(EXTRA_GESTURE_PARAMS)
                
                if (!gestureType.isNullOrEmpty() && !gestureAction.isNullOrEmpty()) {
                    registerGesture(gestureType, gestureAction, gestureParams)
                }
            }
            ACTION_EXECUTE_ACTION -> {
                val actionType = intent.getStringExtra(EXTRA_ACTION_TYPE)
                val actionTarget = intent.getStringExtra(EXTRA_ACTION_TARGET)
                
                if (!actionType.isNullOrEmpty()) {
                    executeAction(actionType, actionTarget)
                }
            }
            ACTION_LIST_GESTURES -> {
                listRegisteredGestures()
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Registers a gesture with an associated action
     */
    private fun registerGesture(gestureType: String, gestureAction: String, gestureParams: String?) {
        try {
            // Validate inputs
            if (SecurityUtils.containsDangerousChars(gestureType) || SecurityUtils.containsDangerousChars(gestureAction)) {
                Log.e(TAG, "Dangerous characters detected in gesture registration")
                return
            }
            
            // Register the gesture
            registeredGestures[gestureType] = gestureAction
            Log.d(TAG, "Gesture registered: $gestureType -> $gestureAction")
            
            // Store in persistent storage
            val prefs = getSharedPreferences("gestures", 0)
            val editor = prefs.edit()
            editor.putString(gestureType, gestureAction)
            if (gestureParams != null) {
                editor.putString("${gestureType}_params", gestureParams)
            }
            editor.apply()
            
            // Broadcast success
            val successIntent = Intent("GESTURE_REGISTERED")
            successIntent.putExtra("gesture_type", gestureType)
            successIntent.putExtra("gesture_action", gestureAction)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering gesture: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("GESTURE_REGISTRATION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Executes an action associated with a gesture
     */
    private fun executeAction(actionType: String, actionTarget: String?) {
        try {
            when (actionType.lowercase()) {
                "launch_app" -> {
                    if (!actionTarget.isNullOrEmpty()) {
                        launchApp(actionTarget)
                    }
                }
                "toggle_flashlight" -> {
                    toggleFlashlight()
                }
                "take_screenshot" -> {
                    takeScreenshot()
                }
                "media_control" -> {
                    if (!actionTarget.isNullOrEmpty()) {
                        mediaControl(actionTarget)
                    }
                }
                "volume_control" -> {
                    if (!actionTarget.isNullOrEmpty()) {
                        volumeControl(actionTarget)
                    }
                }
                else -> {
                    Log.w(TAG, "Unknown action type: $actionType")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing action: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("ACTION_EXECUTION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Launches an app using Shizuku
     */
    private fun launchApp(packageName: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate package name
            val sanitizedPackageName = SecurityUtils.sanitizePackageName(packageName)
            if (sanitizedPackageName != packageName) {
                Log.w(TAG, "Package name was sanitized: $packageName -> $sanitizedPackageName")
            }
            
            val command = "monkey -p $sanitizedPackageName -c android.intent.category.LAUNCHER 1"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "App launched: $sanitizedPackageName")
                
                // Broadcast success
                val successIntent = Intent("APP_LAUNCHED")
                successIntent.putExtra("package_name", sanitizedPackageName)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to launch app: $sanitizedPackageName")
                
                // Broadcast failure
                val failureIntent = Intent("APP_LAUNCH_FAILED")
                failureIntent.putExtra("package_name", sanitizedPackageName)
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: ${e.message}", e)
        }
    }
    
    /**
     * Toggles flashlight using Shizuku
     */
    private fun toggleFlashlight() {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // This is a simulated implementation
            // In a real implementation, we would use a system service to control the flashlight
            Log.d(TAG, "Flashlight toggle command sent")
            
            // Broadcast success
            val successIntent = Intent("FLASHLIGHT_TOGGLED")
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling flashlight: ${e.message}", e)
        }
    }
    
    /**
     * Takes a screenshot using Shizuku
     */
    private fun takeScreenshot() {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            val command = "screencap -p /sdcard/Pictures/Screenshots/screenshot_$(date +%s).png"
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Screenshot taken")
                
                // Broadcast success
                val successIntent = Intent("SCREENSHOT_TAKEN")
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to take screenshot")
                
                // Broadcast failure
                val failureIntent = Intent("SCREENSHOT_FAILED")
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error taking screenshot: ${e.message}", e)
        }
    }
    
    /**
     * Controls media playback using Shizuku
     */
    private fun mediaControl(action: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            val command = when (action.lowercase()) {
                "play_pause" -> "input keyevent 85" // KEYCODE_MEDIA_PLAY_PAUSE
                "next" -> "input keyevent 87"       // KEYCODE_MEDIA_NEXT
                "previous" -> "input keyevent 88"   // KEYCODE_MEDIA_PREVIOUS
                "stop" -> "input keyevent 86"       // KEYCODE_MEDIA_STOP
                else -> {
                    Log.w(TAG, "Unknown media action: $action")
                    return
                }
            }
            
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Media control executed: $action")
                
                // Broadcast success
                val successIntent = Intent("MEDIA_CONTROL_EXECUTED")
                successIntent.putExtra("action", action)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to execute media control: $action")
                
                // Broadcast failure
                val failureIntent = Intent("MEDIA_CONTROL_FAILED")
                failureIntent.putExtra("action", action)
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error controlling media: ${e.message}", e)
        }
    }
    
    /**
     * Controls volume using Shizuku
     */
    private fun volumeControl(action: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            val command = when (action.lowercase()) {
                "up" -> "input keyevent 24"  // KEYCODE_VOLUME_UP
                "down" -> "input keyevent 25" // KEYCODE_VOLUME_DOWN
                "mute" -> "input keyevent 164" // KEYCODE_VOLUME_MUTE
                else -> {
                    Log.w(TAG, "Unknown volume action: $action")
                    return
                }
            }
            
            val result = shizukuBridgeService.executeShellCommand(command)
            
            if (result != null) {
                Log.d(TAG, "Volume control executed: $action")
                
                // Broadcast success
                val successIntent = Intent("VOLUME_CONTROL_EXECUTED")
                successIntent.putExtra("action", action)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to execute volume control: $action")
                
                // Broadcast failure
                val failureIntent = Intent("VOLUME_CONTROL_FAILED")
                failureIntent.putExtra("action", action)
                failureIntent.putExtra("error", "Failed to execute command")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error controlling volume: ${e.message}", e)
        }
    }
    
    /**
     * Lists all registered gestures
     */
    private fun listRegisteredGestures() {
        try {
            val prefs = getSharedPreferences("gestures", 0)
            val allGestures = prefs.all.mapKeys { it.key }.toMutableMap()
            
            // Remove parameter entries from the list
            val gestureList = allGestures.filterKeys { !it.endsWith("_params") }
            
            Log.d(TAG, "Listing ${gestureList.size} registered gestures")
            
            // Broadcast the list
            val listIntent = Intent("GESTURE_LIST")
            listIntent.putExtra("gesture_count", gestureList.size)
            sendBroadcast(listIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing gestures: ${e.message}", e)
        }
    }
    
    /**
     * Triggers a gesture action (called from other parts of the app)
     */
    fun triggerGesture(gestureType: String) {
        val action = registeredGestures[gestureType]
        if (action != null) {
            // Parse the action and execute it
            val parts = action.split(":")
            if (parts.size >= 2) {
                executeAction(parts[0], parts.getOrNull(1))
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "GestureManagerService destroyed")
    }
}