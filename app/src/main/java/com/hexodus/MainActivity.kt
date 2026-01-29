package com.hexodus

import android.Manifest
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
        // Start core services
        startService(Intent(this, ShizukuBridgeService::class.java))
        startService(Intent(this, OverlayActivationService::class.java))
        startService(Intent(this, MonetOverrideService::class.java))
        startService(Intent(this, FoldableDisplayService::class.java))
        startService(Intent(this, HighContrastInjectorService::class.java))
        startService(Intent(this, ThemeManagerService::class.java))
        startService(Intent(this, SystemTunerService::class.java))
        startService(Intent(this, AppThemerService::class.java))
        startService(Intent(this, GestureManagerService::class.java))
        startService(Intent(this, MediaNotificationService::class.java))
        startService(Intent(this, AudioManagerService::class.java))
        startService(Intent(this, AppManagerService::class.java))
        startService(Intent(this, PrivacySecurityService::class.java))
        startService(Intent(this, NetworkFirewallService::class.java))
        startService(Intent(this, PowerManagerService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop services when activity is destroyed
        stopService(Intent(this, ShizukuBridgeService::class.java))
        stopService(Intent(this, OverlayActivationService::class.java))
        stopService(Intent(this, MonetOverrideService::class.java))
        stopService(Intent(this, FoldableDisplayService::class.java))
        stopService(Intent(this, HighContrastInjectorService::class.java))
        stopService(Intent(this, ThemeManagerService::class.java))
        stopService(Intent(this, SystemTunerService::class.java))
        stopService(Intent(this, AppThemerService::class.java))
        stopService(Intent(this, GestureManagerService::class.java))
        stopService(Intent(this, MediaNotificationService::class.java))
        stopService(Intent(this, AudioManagerService::class.java))
        stopService(Intent(this, AppManagerService::class.java))
        stopService(Intent(this, PrivacySecurityService::class.java))
        stopService(Intent(this, NetworkFirewallService::class.java))
        stopService(Intent(this, PowerManagerService::class.java))
    }
}