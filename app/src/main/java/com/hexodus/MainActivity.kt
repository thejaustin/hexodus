package com.hexodus

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.hexodus.ui.theme.HexodusTheme
import com.hexodus.ui.HexodusApp
import com.hexodus.services.*

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.QUERY_ALL_PACKAGES,
            Manifest.permission.DUMP,
            Manifest.permission.INSTALL_PACKAGES,
            Manifest.permission.WRITE_SECURE_SETTINGS,
            Manifest.permission.SET_WALLPAPER,
            Manifest.permission.SYSTEM_ALERT_WINDOW
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request necessary permissions
        requestPermissions()

        // Start core services
        startCoreServices()

        setContent {
            HexodusTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HexodusApp()
                }
            }
        }
    }

    private fun requestPermissions() {
        // Note: In a real implementation, you would use Accompanist Permissions
        // or another library to handle runtime permissions properly
    }

    private fun startCoreServices() {
        // Start core services that are essential for app functionality
        // Only start services when actually needed, not all at once in onCreate
        startServiceIfNeeded(ShizukuBridgeService::class.java)
    }

    private fun startServiceIfNeeded(serviceClass: Class<*>) {
        // Check if service is already running before starting
        if (!isServiceRunning(serviceClass)) {
            startService(Intent(this, serviceClass))
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't stop services when activity is destroyed
        // Services should manage their own lifecycle independently
        // Only stop services when the app is explicitly closed or when needed
    }
}