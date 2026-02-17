package com.hexodus

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import rikka.shizuku.Shizuku

class HexodusApplication : Application() {
    companion object {
        private const val TAG = "HexodusApplication"

        // Using lazy initialization to ensure Shizuku is initialized after the application is created
        val IS_SHIZUKU_SUPPORTED by lazy {
            try {
                Shizuku.pingBinder()
            } catch (e: Throwable) {
                Log.e(TAG, "Shizuku not supported", e)
                false
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        initShizuku()
    }

    private fun initShizuku() {
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
        Log.d(TAG, "Shizuku initialized")
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "Shizuku binder received")
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent("SHIZUKU_BINDER_RECEIVED"))
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.d(TAG, "Shizuku binder dead")
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent("SHIZUKU_BINDER_DEAD"))
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        Log.d(TAG, "Shizuku permission result: $requestCode, $grantResult")
        val intent = Intent("SHIZUKU_PERMISSION_RESULT")
        intent.putExtra("requestCode", requestCode)
        intent.putExtra("grantResult", grantResult)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
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
