package com.hexodus

import android.app.Application
import android.content.Intent
import android.util.Log
import rikka.shizuku.Shizuku

class HexodusApplication : Application() {
    companion object {
        private const val TAG = "HexodusApplication"

        // Safe initialization to avoid crashes if Shizuku provider is not yet ready
        var IS_SHIZUKU_SUPPORTED: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        checkShizukuSupport()
        initShizuku()
    }

    private fun checkShizukuSupport() {
        IS_SHIZUKU_SUPPORTED = try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            Log.e(TAG, "Shizuku not supported or not yet initialized", e)
            false
        }
    }

    private fun initShizuku() {
        try {
            Shizuku.addBinderReceivedListener(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
            Log.d(TAG, "Shizuku listeners added")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add Shizuku listeners", e)
        }
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "Shizuku binder received")
        // Standard broadcast (LocalBroadcastManager is deprecated)
        sendBroadcast(Intent("SHIZUKU_BINDER_RECEIVED").setPackage(packageName))
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.d(TAG, "Shizuku binder dead")
        sendBroadcast(Intent("SHIZUKU_BINDER_DEAD").setPackage(packageName))
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        Log.d(TAG, "Shizuku permission result: $requestCode, $grantResult")
        val intent = Intent("SHIZUKU_PERMISSION_RESULT")
        intent.setPackage(packageName)
        intent.putExtra("requestCode", requestCode)
        intent.putExtra("grantResult", grantResult)
        sendBroadcast(intent)
    }

    override fun onTerminate() {
        super.onTerminate()
        cleanupShizukuListeners()
    }

    private fun cleanupShizukuListeners() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
            Log.d(TAG, "Shizuku listeners cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up Shizuku listeners: ${e.message}", e)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "Application running low on memory")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.w(TAG, "Memory trim level: RUNNING_${
                    when (level) {
                        TRIM_MEMORY_RUNNING_MODERATE -> "MODERATE"
                        TRIM_MEMORY_RUNNING_LOW -> "LOW"
                        TRIM_MEMORY_RUNNING_CRITICAL -> "CRITICAL"
                        else -> level
                    }
                }")
            }
            TRIM_MEMORY_UI_HIDDEN -> {
                Log.d(TAG, "UI hidden, releasing memory")
            }
            TRIM_MEMORY_BACKGROUND,
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_COMPLETE -> {
                Log.w(TAG, "Memory trim level: ${
                    when (level) {
                        TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
                        TRIM_MEMORY_MODERATE -> "MODERATE"
                        TRIM_MEMORY_COMPLETE -> "COMPLETE"
                        else -> level
                    }
                }")
                cleanupShizukuListeners()
            }
        }
    }
}
