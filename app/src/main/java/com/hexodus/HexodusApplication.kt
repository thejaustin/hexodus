package com.hexodus

import android.app.Application
import android.content.Intent
import android.util.Log
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

class HexodusApplication : Application() {
    companion object {
        private const val TAG = "HexodusApplication"

        // Using lazy initialization to ensure Shizuku is initialized after the application is created
        val IS_SHIZUKU_SUPPORTED by lazy {
            try {
                // Check if Shizuku is supported on this device
                Shizuku.pingBinder()
            } catch (e: Throwable) {
                Log.e(TAG, "Shizuku not supported", e)
                false
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Shizuku with the application context
        Shizuku.setApplicationContext(this)
        initShizuku()
    }

    private fun initShizuku() {
        // Initialize Shizuku with the application context
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)

        Log.d(TAG, "Shizuku initialized")
    }

    /**
     * Binder received listener
     */
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "Shizuku binder received")

        // Broadcast binder received
        val intent = Intent("SHIZUKU_BINDER_RECEIVED")
        sendBroadcast(intent)
    }

    /**
     * Binder dead listener
     */
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.d(TAG, "Shizuku binder dead")

        // Broadcast binder dead
        val intent = Intent("SHIZUKU_BINDER_DEAD")
        sendBroadcast(intent)
    }

    /**
     * Permission result listener for Shizuku
     */
    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        Log.d(TAG, "Shizuku permission result: $requestCode, $grantResult")

        // Broadcast permission result
        val intent = Intent("SHIZUKU_PERMISSION_RESULT")
        intent.putExtra("requestCode", requestCode)
        intent.putExtra("grantResult", grantResult)
        sendBroadcast(intent)
    }

    override fun onTerminate() {
        super.onTerminate()
        // Clean up Shizuku listeners
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
    }
}