package com.hexodus

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (permission, granted) ->
            Log.d(TAG, "Permission $permission granted: $granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request necessary runtime permissions
        requestPermissions()

        // Start core services
        startCoreServices()

        setContent {
            HexodusTheme {
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
        // SYSTEM_ALERT_WINDOW requires Settings intent, not requestPermissions
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        // Collect runtime permissions that haven't been granted yet
        val needed = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SET_WALLPAPER)
            != PackageManager.PERMISSION_GRANTED
        ) {
            needed.add(Manifest.permission.SET_WALLPAPER)
        }

        // Storage permissions for older APIs
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                needed.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    private fun startCoreServices() {
        if (!ShizukuBridgeService.isRunning) {
            startService(Intent(this, ShizukuBridgeService::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't stop services when activity is destroyed
        // Services should manage their own lifecycle independently
    }
}
