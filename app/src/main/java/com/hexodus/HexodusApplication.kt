package com.hexodus

import android.app.Application
import android.content.Context
import rikka.shizuku.Shizuku

class HexodusApplication : Application() {
    companion object {
        private const val TAG = "HexodusApplication"

        // Using lazy initialization to ensure Shizuku is initialized after the application is created
        val IS_SHIZUKU_SUPPORTED by lazy {
            try {
                // Check if Shizuku is supported on this device
                Shizuku.pingBinder()
            } catch (e: Throwable) {
                false
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Shizuku
        initShizuku()
    }

    private fun initShizuku() {
        // Initialize Shizuku with the application context
        Shizuku.addBinderReceivedListener {
            // Called when Shizuku service is connected
        }

        Shizuku.addBinderDeadListener {
            // Called when Shizuku service is disconnected
        }

        Shizuku.addRequestPermissionResultListener { requestCode, grantResult ->
            // Handle permission request results
        }
    }
}