package com.hexodus.services
import com.hexodus.HexodusApplication

import android.app.Service

import android.content.Intent
import android.util.Log

/**
 * OverlayActivationService - Service that wraps OverlayManager to handle Intents
 */
object OverlayActivationService {
    private val context: android.content.Context get() = com.hexodus.HexodusApplication.context
    private val packageName: String get() = context.packageName
    private val cacheDir: java.io.File get() = context.cacheDir
    private val filesDir: java.io.File get() = context.filesDir
    private val contentResolver: android.content.ContentResolver get() = context.contentResolver
    private val packageManager: android.content.pm.PackageManager get() = context.packageManager
    private val applicationContext: android.content.Context get() = context

    

    
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
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val apkPath = intent.getStringExtra(EXTRA_APK_PATH)
                val validateSignature = intent.getBooleanExtra(EXTRA_VALIDATE_SIGNATURE, true)
                
                if (!packageName.isNullOrEmpty() && !apkPath.isNullOrEmpty()) {
                    val success = OverlayManager.activateOverlay(HexodusApplication.context, packageName, apkPath, validateSignature)
                    val resultIntent = Intent(if (success) "OVERLAY_ACTIVATION_SUCCESS" else "OVERLAY_ACTIVATION_FAILURE")
                    resultIntent.putExtra("package_name", packageName)
                    context.sendBroadcast(resultIntent)
                }
            }
            ACTION_DEACTIVATE_OVERLAY -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                if (!packageName.isNullOrEmpty()) {
                    val success = OverlayManager.deactivateOverlay(HexodusApplication.context, packageName)
                    val resultIntent = Intent(if (success) "OVERLAY_DEACTIVATION_SUCCESS" else "OVERLAY_DEACTIVATION_FAILURE")
                    resultIntent.putExtra("package_name", packageName)
                    context.sendBroadcast(resultIntent)
                }
            }
            ACTION_REFRESH_OVERLAYS -> {
                OverlayManager.refreshSystemUI(HexodusApplication.context)
            }
        }
        
        return android.app.Service.android.app.Service.START_STICKY
    }

    // Still providing context for legacy/direct callers, though they should use OverlayManager directly
    fun applyTheme(themeData: ByteArray, themeName: String) {
        OverlayManager.applyTheme(HexodusApplication.context, themeData, themeName)
    }
}
