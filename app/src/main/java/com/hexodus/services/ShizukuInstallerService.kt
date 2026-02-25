package com.hexodus.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.content.FileProvider
import com.hexodus.utils.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern
import moe.shizuku.plus.ShizukuPlusAPI
import com.hexodus.utils.PrefsManager

/**
 * ShizukuInstallerService - Downloads APKs and installs them natively via PackageInstaller
 * or silently using Shizuku.
 */
class ShizukuInstallerService : Service() {

    companion object {
        private const val TAG = "ShizukuInstallerService"
        
        const val ACTION_INSTALL_APK = "com.hexodus.INSTALL_APK"
        const val EXTRA_APK_URL = "apk_url"
        const val EXTRA_APP_NAME = "app_name"
    }

    private lateinit var shizukuBridgeService: ShizukuBridgeService
    private lateinit var prefsManager: PrefsManager
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        shizukuBridgeService = ShizukuBridgeService()
        prefsManager = PrefsManager.getInstance(this)
        Log.d(TAG, "ShizukuInstallerService created")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_INSTALL_APK -> {
                val apkUrl = intent.getStringExtra(EXTRA_APK_URL)
                val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "app"
                
                if (!apkUrl.isNullOrEmpty() && SecurityUtils.isValidUri(apkUrl)) {
                    downloadAndInstall(apkUrl, appName)
                } else {
                    Log.e(TAG, "Invalid APK URL: $apkUrl")
                }
            }
        }
        return START_STICKY
    }

    private fun downloadAndInstall(apkUrl: String, appName: String) {
        scope.launch {
            try {
                var finalUrl = apkUrl
                
                // If it's a GitHub release page, try to find the actual APK asset
                if (apkUrl.contains("github.com") && apkUrl.contains("/releases/")) {
                    finalUrl = resolveGitHubAsset(apkUrl)
                }

                // 1. Download the APK
                val file = File(cacheDir, "${appName.replace(" ", "_")}.apk")
                Log.d(TAG, "Downloading $appName from $finalUrl to ${file.absolutePath}")
                
                val url = URL(finalUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP ${connection.responseCode}")
                    // Handle redirection for GitHub assets
                    if (connection.responseCode == 302 || connection.responseCode == 301) {
                        val newUrl = connection.getHeaderField("Location")
                        downloadAndInstall(newUrl, appName)
                        return@launch
                    }
                    return@launch
                }

                val fileLength = connection.contentLength
                var downloaded = 0L

                connection.inputStream.use { input ->
                    FileOutputStream(file).use { output ->
                        val data = ByteArray(4096)
                        var count: Int
                        while (input.read(data).also { count = it } != -1) {
                            output.write(data, 0, count)
                            downloaded += count
                            if (fileLength > 0) {
                                val progress = (downloaded * 100 / fileLength).toInt()
                                val progressIntent = Intent("APK_INSTALLATION_PROGRESS")
                                progressIntent.putExtra("app_name", appName)
                                progressIntent.putExtra("progress", progress)
                                sendBroadcast(progressIntent)
                            }
                        }
                    }
                }

                // Notify that download is complete and installation is starting
                val installingIntent = Intent("APK_INSTALLATION_PROGRESS")
                installingIntent.putExtra("app_name", appName)
                installingIntent.putExtra("progress", 100)
                installingIntent.putExtra("status", "Installing...")
                sendBroadcast(installingIntent)

                // 2. Install the APK
                if (shizukuBridgeService.isReady()) {
                    installViaShizuku(file)
                } else {
                    installViaNative(file)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during download/install of $appName", e)
                val errorIntent = Intent("APK_INSTALLATION_RESULT")
                errorIntent.putExtra("success", false)
                errorIntent.putExtra("app_name", appName)
                errorIntent.putExtra("error", e.message)
                sendBroadcast(errorIntent)
            }
        }
    }

    /**
     * Attempts to resolve the direct APK download link from a GitHub release URL.
     * In a production app, this would use the GitHub API. 
     * Here we use a simplified scraping approach for the demo.
     */
    private fun resolveGitHubAsset(releaseUrl: String): String {
        return try {
            val url = URL(releaseUrl)
            val connection = url.openConnection() as HttpURLConnection
            val html = connection.inputStream.bufferedReader().use { it.readText() }
            
            // Look for .apk links in the release page
            val pattern = Pattern.compile("href=\"([^\"]+\\.apk)\"")
            val matcher = pattern.matcher(html)
            
            if (matcher.find()) {
                val path = matcher.group(1) ?: return releaseUrl
                if (path.startsWith("http")) path else "https://github.com$path"
            } else {
                releaseUrl
            }
        } catch (e: Exception) {
            releaseUrl
        }
    }

    private suspend fun installViaShizuku(apkFile: File) {
        Log.d(TAG, "Installing via Shizuku...")
        
        val success: Boolean = if (prefsManager.preferShizukuPlus && ShizukuPlusAPI.isEnhancedApiSupported()) {
            Log.d(TAG, "Using Shizuku+ Enhanced API for installation")
            try {
                ShizukuPlusAPI.PackageManager.installPackage(apkFile.absolutePath)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Shizuku+ installation failed, falling back to legacy", e)
                installLegacy(apkFile)
            }
        } else {
            installLegacy(apkFile)
        }
        
        val intent = Intent("APK_INSTALLATION_RESULT")
        intent.putExtra("success", success)
        sendBroadcast(intent)
    }

    private fun installLegacy(apkFile: File): Boolean {
        // Using 'pm install' via Shizuku shell
        val command = "pm install -r \"${apkFile.absolutePath}\""
        val result = shizukuBridgeService.executeShellCommand(command) ?: "Error"
        Log.d(TAG, "Legacy Shizuku installation result: $result")
        return result.contains("Success", ignoreCase = true)
    }

    private fun installViaNative(apkFile: File) {
        Log.d(TAG, "Installing via native intent...")
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error during native installation", e)
        }
    }
}
