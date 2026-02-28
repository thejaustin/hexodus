package com.hexodus.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.hexodus.utils.SecurityUtils
import com.hexodus.HexodusApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import moe.shizuku.plus.ShizukuPlusAPI
import com.hexodus.utils.PrefsManager

/**
 * ShizukuInstaller - Downloads APKs and installs them natively via PackageInstaller
 * or silently using Shizuku. Refactored from Service to Singleton.
 */
object ShizukuInstaller {

    private const val TAG = "ShizukuInstaller"

    suspend fun downloadAndInstall(apkUrl: String, appName: String) = withContext(Dispatchers.IO) {
        val context = HexodusApplication.context
        val prefsManager = PrefsManager.getInstance(context)
        
        try {
            var finalUrl = apkUrl
            
            // Use GitHub API to find the actual APK asset
            if (apkUrl.contains("github.com") && apkUrl.contains("/releases/")) {
                finalUrl = resolveGitHubAsset(apkUrl)
            }

            // 1. Download the APK
            val file = File(context.cacheDir, "${appName.replace(" ", "_")}.apk")
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
                    return@withContext
                }
                return@withContext
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
                            HexodusApplication.context.sendBroadcast(progressIntent)
                        }
                    }
                }
            }

            // Notify that download is complete and installation is starting
            val installingIntent = Intent("APK_INSTALLATION_PROGRESS")
            installingIntent.putExtra("app_name", appName)
            installingIntent.putExtra("progress", 100)
            installingIntent.putExtra("status", "Installing...")
            HexodusApplication.context.sendBroadcast(installingIntent)

            // 2. Install the APK
            if (ShizukuBridge.isReady()) {
                installViaShizuku(context, file, prefsManager)
            } else {
                installViaNative(context, file)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during download/install of $appName", e)
            val errorIntent = Intent("APK_INSTALLATION_RESULT")
            errorIntent.putExtra("success", false)
            errorIntent.putExtra("app_name", appName)
            errorIntent.putExtra("error", e.message)
            HexodusApplication.context.sendBroadcast(errorIntent)
        }
    }

    /**
     * Resolves the direct APK download link from a GitHub release URL using the GitHub API.
     */
    private fun resolveGitHubAsset(releaseUrl: String): String {
        return try {
            // Convert https://github.com/owner/repo/releases/... to https://api.github.com/repos/owner/repo/releases/latest
            val parts = releaseUrl.split("/")
            val ownerIndex = parts.indexOf("github.com") + 1
            if (ownerIndex > 0 && parts.size >= ownerIndex + 2) {
                val owner = parts[ownerIndex]
                val repo = parts[ownerIndex + 1]
                val apiUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"
                
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)
                val assets = json.getJSONArray("assets")
                
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.getString("name")
                    if (name.endsWith(".apk")) {
                        return asset.getString("browser_download_url")
                    }
                }
            }
            releaseUrl
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve GitHub asset via API", e)
            releaseUrl
        }
    }

    private fun installViaShizuku(context: Context, apkFile: File, prefsManager: PrefsManager) {
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
        HexodusApplication.context.sendBroadcast(intent)
    }

    private fun installLegacy(apkFile: File): Boolean {
        // Using 'pm install' via Shizuku shell
        val command = "pm install -r \"${apkFile.absolutePath}\""
        val result = ShizukuBridge.executeShellCommand(command) ?: "Error"
        Log.d(TAG, "Legacy Shizuku installation result: $result")
        return result.contains("Success", ignoreCase = true)
    }

    private fun installViaNative(context: Context, apkFile: File) {
        Log.d(TAG, "Installing via native intent...")
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error during native installation", e)
        }
    }
}
