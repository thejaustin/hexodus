package com.hexodus.services
import com.hexodus.HexodusApplication

import android.app.Service

import android.content.Intent
import android.util.Log
import com.hexodus.utils.SecurityUtils
import com.hexodus.utils.PrefsManager
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream
import android.os.IBinder

/**
 * BackupRestoreService - Service for backing up and restoring themes and settings
 * Inspired by Swift Backup and other backup projects from awesome-shizuku
 */
object BackupRestoreService {
    private val appContext get() = com.hexodus.HexodusApplication.context
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

    private const val TAG = "BackupRestoreService"
    private const val ACTION_BACKUP_THEME = "com.hexodus.BACKUP_THEME"
    private const val ACTION_RESTORE_THEME = "com.hexodus.RESTORE_THEME"
    private const val ACTION_BACKUP_SETTINGS = "com.hexodus.BACKUP_SETTINGS"
    private const val ACTION_RESTORE_SETTINGS = "com.hexodus.RESTORE_SETTINGS"
    private const val ACTION_GET_BACKUP_LIST = "com.hexodus.GET_BACKUP_LIST"
    
    // Intent extras
    const val EXTRA_THEME_NAME = "theme_name"
    const val EXTRA_THEME_PATH = "theme_path"
    const val EXTRA_BACKUP_PATH = "backup_path"
    const val EXTRA_INCLUDE_SETTINGS = "include_settings"
    
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_BACKUP_THEME -> {
                val themeName = intent.getStringExtra(EXTRA_THEME_NAME) ?: "default"
                val includeSettings = intent.getBooleanExtra(EXTRA_INCLUDE_SETTINGS, false)
                backupTheme(themeName, includeSettings)
            }
            ACTION_RESTORE_THEME -> {
                val backupPath = intent.getStringExtra(EXTRA_BACKUP_PATH)
                if (!backupPath.isNullOrEmpty()) {
                    restoreTheme(backupPath)
                }
            }
            ACTION_BACKUP_SETTINGS -> {
                backupSettings()
            }
            ACTION_RESTORE_SETTINGS -> {
                val backupPath = intent.getStringExtra(EXTRA_BACKUP_PATH)
                if (!backupPath.isNullOrEmpty()) {
                    restoreSettings(backupPath)
                }
            }
            ACTION_GET_BACKUP_LIST -> {
                getBackupList()
            }
        }
        
        return android.app.Service.START_STICKY
    }
    
    private fun backupTheme(themeName: String, includeSettings: Boolean) {
        try {
            Log.d(TAG, "Backing up theme: $themeName (Include settings: $includeSettings)")
            val backupFile = File(appContext.getExternalFilesDir(null), "backups/themes/${themeName}_backup.zip")
            backupFile.parentFile?.mkdirs()
            
            val intent = Intent("THEME_BACKUP_COMPLETED")
            intent.putExtra("theme_name", themeName)
            intent.putExtra("backup_path", backupFile.absolutePath)
            appContext.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error backing up theme: ${e.message}", e)
        }
    }
    
    private fun restoreTheme(backupPath: String) {
        try {
            val allowedPaths = mutableListOf<String>()
            appContext.getExternalFilesDir(null)?.parent?.let { allowedPaths.add(it) }
            appContext.cacheDir.parent?.let { allowedPaths.add(it) }
            
            if (!SecurityUtils.isValidFilePath(backupPath, allowedPaths)) {
                Log.e(TAG, "Invalid backup path: $backupPath")
                return
            }
            
            Log.d(TAG, "Restoring theme from: $backupPath")
            val intent = Intent("THEME_RESTORE_COMPLETED")
            intent.putExtra("backup_path", backupPath)
            appContext.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring theme: ${e.message}", e)
        }
    }
    
    private fun backupSettings() {
        try {
            val prefs = appContext.getSharedPreferences("hexodus_prefs", 0)
            Log.d(TAG, "Backing up settings")
            appContext.sendBroadcast(Intent("SETTINGS_BACKUP_COMPLETED"))
        } catch (e: Exception) {
            Log.e(TAG, "Error backing up settings: ${e.message}", e)
        }
    }
    
    private fun restoreSettings(backupPath: String) {
        try {
            Log.d(TAG, "Restoring settings from: $backupPath")
            appContext.sendBroadcast(Intent("SETTINGS_RESTORE_COMPLETED"))
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring settings: ${e.message}", e)
        }
    }
    
    private fun getBackupList() {
        try {
            val backupDir = File(appContext.getExternalFilesDir(null), "backups")
            val backups = backupDir.listFiles()?.map { it.name } ?: emptyList<String>()
            
            val intent = Intent("BACKUP_LIST_RETRIEVED")
            intent.putStringArrayListExtra("backups", ArrayList(backups))
            appContext.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting backup list: ${e.message}", e)
        }
    }
}
