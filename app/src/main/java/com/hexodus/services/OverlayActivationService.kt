package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.HexodusApplication

/**
 * OverlayActivationService - Service that wraps OverlayManager to handle Intents
 */
object OverlayActivationService {
    private val appContext: android.content.Context get() = com.hexodus.HexodusApplication.context

    
    
    
    
    
    
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

    private const val TAG = "OverlayActivationService"
    const val ACTION_ACTIVATE_OVERLAY = "com.hexodus.ACTIVATE_OVERLAY"
    const val ACTION_DEACTIVATE_OVERLAY = "com.hexodus.DEACTIVATE_OVERLAY"
    const val ACTION_REFRESH_OVERLAYS = "com.hexodus.REFRESH_OVERLAYS"
    
    const val EXTRA_PACKAGE_NAME = "package_name"
    const val EXTRA_APK_PATH = "apk_path"
    const val EXTRA_VALIDATE_SIGNATURE = "validate_signature"
    
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_ACTIVATE_OVERLAY -> {
                val targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val apkPath = intent.getStringExtra(EXTRA_APK_PATH)
                val validateSignature = intent.getBooleanExtra(EXTRA_VALIDATE_SIGNATURE, true)
                
                if (!targetPackage.isNullOrEmpty() && !apkPath.isNullOrEmpty()) {
                    val success = OverlayManager.activateOverlay(targetPackage, apkPath, validateSignature)
                    val resultIntent = Intent(if (success) "OVERLAY_ACTIVATION_SUCCESS" else "OVERLAY_ACTIVATION_FAILURE")
                    resultIntent.putExtra("package_name", targetPackage)
                    appContext.sendBroadcast(resultIntent)
                }
            }
            ACTION_DEACTIVATE_OVERLAY -> {
                val targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                if (!targetPackage.isNullOrEmpty()) {
                    val success = OverlayManager.deactivateOverlay(targetPackage)
                    val resultIntent = Intent(if (success) "OVERLAY_DEACTIVATION_SUCCESS" else "OVERLAY_DEACTIVATION_FAILURE")
                    resultIntent.putExtra("package_name", targetPackage)
                    appContext.sendBroadcast(resultIntent)
                }
            }
            ACTION_REFRESH_OVERLAYS -> {
                OverlayManager.refreshSystemUI()
            }
        }
        
        return android.app.Service.START_STICKY
    }

    // Still providing this for legacy/direct callers, though they should use OverlayManager directly
    fun applyTheme(themeData: ByteArray, themeName: String) {
        OverlayManager.applyTheme(themeData, themeName)
    }
}
