package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.utils.SecurityUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.hexodus.core.ThemeCompiler

/**
 * BackupRestoreService - Service for theme backup and restore functionality
 * Inspired by backup features from various awesome-shizuku projects
 */
class BackupRestoreService : Service() {
    
    companion object {
        private const val TAG = "BackupRestoreService"
        private const val ACTION_CREATE_BACKUP = "com.hexodus.CREATE_BACKUP"
        private const val ACTION_RESTORE_BACKUP = "com.hexodus.RESTORE_BACKUP"
        private const val ACTION_LIST_BACKUPS = "com.hexodus.LIST_BACKUPS"
        private const val ACTION_DELETE_BACKUP = "com.hexodus.DELETE_BACKUP"
        private const val ACTION_EXPORT_THEME = "com.hexodus.EXPORT_THEME"
        private const val ACTION_IMPORT_THEME = "com.hexodus.IMPORT_THEME"
        
        // Intent extras
        const val EXTRA_BACKUP_NAME = "backup_name"
        const val EXTRA_BACKUP_PATH = "backup_path"
        const val EXTRA_BACKUP_TYPE = "backup_type" // full, themes_only, settings_only
        const val EXTRA_THEME_NAME = "theme_name"
        const val EXTRA_THEME_PATH = "theme_path"
        const val EXTRA_INCLUDE_COMPONENTS = "include_components"
        const val EXTRA_INCLUDE_APP_SETTINGS = "include_app_settings"
        const val EXTRA_INCLUDE_SYSTEM_SETTINGS = "include_system_settings"
    }
    
    private lateinit var shizukuBridgeService: ShizukuBridgeService
    private val backupDir = File(getExternalFilesDir(null), "backups")
    
    data class BackupMetadata(
        val name: String,
        val type: String,
        val createdAt: Long,
        val version: String,
        val includesComponents: Boolean,
        val includesAppSettings: Boolean,
        val includesSystemSettings: Boolean,
        val themeCount: Int,
        val overlayPackages: List<String>
    )
    
    override fun onCreate() {
        super.onCreate()
        shizukuBridgeService = ShizukuBridgeService()
        backupDir.mkdirs()
        Log.d(TAG, "BackupRestoreService created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_CREATE_BACKUP -> {
                val backupName = intent.getStringExtra(EXTRA_BACKUP_NAME) ?: "backup_${System.currentTimeMillis()}"
                val backupType = intent.getStringExtra(EXTRA_BACKUP_TYPE) ?: "full"
                val includeComponents = intent.getBooleanExtra(EXTRA_INCLUDE_COMPONENTS, true)
                val includeAppSettings = intent.getBooleanExtra(EXTRA_INCLUDE_APP_SETTINGS, true)
                val includeSystemSettings = intent.getBooleanExtra(EXTRA_INCLUDE_SYSTEM_SETTINGS, true)
                
                createBackup(backupName, backupType, includeComponents, includeAppSettings, includeSystemSettings)
            }
            ACTION_RESTORE_BACKUP -> {
                val backupPath = intent.getStringExtra(EXTRA_BACKUP_PATH)
                
                if (!backupPath.isNullOrEmpty()) {
                    restoreBackup(backupPath)
                }
            }
            ACTION_LIST_BACKUPS -> {
                listBackups()
            }
            ACTION_DELETE_BACKUP -> {
                val backupPath = intent.getStringExtra(EXTRA_BACKUP_PATH)
                
                if (!backupPath.isNullOrEmpty()) {
                    deleteBackup(backupPath)
                }
            }
            ACTION_EXPORT_THEME -> {
                val themeName = intent.getStringExtra(EXTRA_THEME_NAME)
                val themePath = intent.getStringExtra(EXTRA_THEME_PATH)
                
                if (!themeName.isNullOrEmpty() && !themePath.isNullOrEmpty()) {
                    exportTheme(themeName, themePath)
                }
            }
            ACTION_IMPORT_THEME -> {
                val themePath = intent.getStringExtra(EXTRA_THEME_PATH)
                
                if (!themePath.isNullOrEmpty()) {
                    importTheme(themePath)
                }
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Creates a comprehensive backup of themes and settings
     */
    private fun createBackup(
        backupName: String,
        backupType: String,
        includeComponents: Boolean,
        includeAppSettings: Boolean,
        includeSystemSettings: Boolean
    ) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (SecurityUtils.containsDangerousChars(backupName)) {
                Log.e(TAG, "Dangerous characters detected in backup name")
                return
            }
            
            val validBackupTypes = listOf("full", "themes_only", "settings_only", "overlays_only")
            if (backupType !in validBackupTypes) {
                Log.e(TAG, "Invalid backup type: $backupType")
                return
            }
            
            // Create backup file
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
            val timestamp = LocalDateTime.now().format(formatter)
            val backupFileName = "${backupName}_${timestamp}.hexbackup"
            val backupFile = File(backupDir, backupFileName)
            
            // Create backup data based on type
            val backupData = createBackupData(backupType, includeComponents, includeAppSettings, includeSystemSettings)
            
            // Write backup to file with compression
            ZipOutputStream(FileOutputStream(backupFile)).use { zipOutputStream ->
                // Add metadata
                val metadataEntry = ZipEntry("metadata.json")
                zipOutputStream.putNextEntry(metadataEntry)
                val metadata = BackupMetadata(
                    name = backupName,
                    type = backupType,
                    createdAt = System.currentTimeMillis(),
                    version = "1.0.0",
                    includesComponents = includeComponents,
                    includesAppSettings = includeAppSettings,
                    includesSystemSettings = includeSystemSettings,
                    themeCount = backupData.themes.size,
                    overlayPackages = backupData.overlays.map { it.packageName }
                )
                val metadataJson = """
                    {
                        "name": "${metadata.name}",
                        "type": "${metadata.type}",
                        "created_at": ${metadata.createdAt},
                        "version": "${metadata.version}",
                        "includes_components": ${metadata.includesComponents},
                        "includes_app_settings": ${metadata.includesAppSettings},
                        "includes_system_settings": ${metadata.includesSystemSettings},
                        "theme_count": ${metadata.themeCount},
                        "overlay_packages": ${metadata.overlayPackages}
                    }
                """.trimIndent()
                zipOutputStream.write(metadataJson.toByteArray())
                zipOutputStream.closeEntry()
                
                // Add themes data
                if (backupType == "full" || backupType == "themes_only") {
                    val themesEntry = ZipEntry("themes.json")
                    zipOutputStream.putNextEntry(themesEntry)
                    val themesJson = """
                        {
                            "themes": ${backupData.themes.map { theme ->
                                """
                                {
                                    "name": "${theme.name}",
                                    "hex_color": "${theme.hexColor}",
                                    "components": ${theme.themedComponents},
                                    "created_at": ${theme.createdAt},
                                    "version": "${theme.version}"
                                }
                                """.trimIndent()
                            }.joinToString(",", "[", "]")}
                        }
                    """.trimIndent()
                    zipOutputStream.write(themesJson.toByteArray())
                    zipOutputStream.closeEntry()
                }
                
                // Add settings data
                if (backupType == "full" || backupType == "settings_only") {
                    val settingsEntry = ZipEntry("settings.json")
                    zipOutputStream.putNextEntry(settingsEntry)
                    val settingsJson = """
                        {
                            "app_settings": ${backupData.appSettings},
                            "system_settings": ${backupData.systemSettings}
                        }
                    """.trimIndent()
                    zipOutputStream.write(settingsJson.toByteArray())
                    zipOutputStream.closeEntry()
                }
                
                // Add overlay data
                if (backupType == "full" || backupType == "overlays_only") {
                    val overlaysEntry = ZipEntry("overlays.json")
                    zipOutputStream.putNextEntry(overlaysEntry)
                    val overlaysJson = """
                        {
                            "overlays": ${backupData.overlays.map { overlay ->
                                """
                                {
                                    "package_name": "${overlay.packageName}",
                                    "name": "${overlay.name}",
                                    "enabled": ${overlay.enabled},
                                    "priority": ${overlay.priority},
                                    "target_packages": ${overlay.targetPackages}
                                }
                                """.trimIndent()
                            }.joinToString(",", "[", "]")}
                        }
                    """.trimIndent()
                    zipOutputStream.write(overlaysJson.toByteArray())
                    zipOutputStream.closeEntry()
                }
            }
            
            Log.d(TAG, "Created backup: ${backupFile.name} with ${backupData.themes.size} themes")
            
            // Broadcast success
            val successIntent = Intent("BACKUP_CREATED")
            successIntent.putExtra("backup_path", backupFile.absolutePath)
            successIntent.putExtra("backup_name", backupName)
            successIntent.putExtra("theme_count", backupData.themes.size)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating backup: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("BACKUP_CREATION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Restores a backup using Shizuku
     */
    private fun restoreBackup(backupPath: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate backup path
            if (!SecurityUtils.isValidFilePath(backupPath, listOf(backupDir.absolutePath))) {
                Log.e(TAG, "Invalid backup path: $backupPath")
                return
            }
            
            val backupFile = File(backupPath)
            if (!backupFile.exists()) {
                Log.e(TAG, "Backup file does not exist: $backupPath")
                return
            }
            
            // Extract backup data
            val backupData = extractBackupData(backupFile)
            if (backupData == null) {
                Log.e(TAG, "Failed to extract backup data from: $backupPath")
                return
            }
            
            // Apply themes
            for (theme in backupData.themes) {
                // Create and apply each theme
                val themeCompiler = ThemeCompiler()
                val themeData = themeCompiler.compileTheme(
                    theme.hexColor,
                    "com.hexodus.restored.${theme.name.replace(" ", "_").lowercase()}.${System.currentTimeMillis()}",
                    theme.name,
                    theme.themedComponents
                )
                
                // Save and install the theme overlay
                val tempFile = File(cacheDir, "${theme.name.replace(" ", "_")}.apk")
                FileOutputStream(tempFile).use { it.write(themeData) }
                
                val installSuccess = shizukuBridgeService.installApk(tempFile.absolutePath)
                if (installSuccess) {
                    // Enable the overlay
                    shizukuBridgeService.executeOverlayCommand(tempFile.nameWithoutExtension, "enable")
                }
                
                // Clean up temp file
                tempFile.delete()
            }
            
            // Apply settings
            if (backupData.appSettings.isNotEmpty()) {
                applyAppSettings(backupData.appSettings)
            }
            
            if (backupData.systemSettings.isNotEmpty()) {
                applySystemSettings(backupData.systemSettings)
            }
            
            // Apply overlays
            for (overlay in backupData.overlays) {
                // In a real implementation, this would restore overlay configurations
                Log.d(TAG, "Restored overlay: ${overlay.packageName}")
            }
            
            Log.d(TAG, "Restored backup from: $backupPath with ${backupData.themes.size} themes")
            
            // Broadcast success
            val successIntent = Intent("BACKUP_RESTORED")
            successIntent.putExtra("backup_path", backupPath)
            successIntent.putExtra("theme_count", backupData.themes.size)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring backup: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("BACKUP_RESTORE_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Lists all available backups
     */
    private fun listBackups() {
        try {
            val backupFiles = backupDir.listFiles { file -> 
                file.isFile && file.name.endsWith(".hexbackup") 
            }
            
            val backups = backupFiles?.map { file ->
                // Extract metadata from the backup file
                val metadata = extractBackupMetadata(file)
                
                mapOf(
                    "name" to (metadata?.name ?: file.nameWithoutExtension),
                    "path" to file.absolutePath,
                    "size" to file.length(),
                    "modified" to file.lastModified(),
                    "type" to (metadata?.type ?: "unknown"),
                    "theme_count" to (metadata?.themeCount ?: 0),
                    "created_at" to (metadata?.createdAt ?: file.lastModified())
                )
            } ?: emptyList()
            
            Log.d(TAG, "Found ${backups.size} backups")
            
            // Broadcast results
            val successIntent = Intent("BACKUPS_LISTED")
            successIntent.putExtra("backup_count", backups.size)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing backups: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("BACKUPS_LIST_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Deletes a backup file
     */
    private fun deleteBackup(backupPath: String) {
        try {
            // Validate backup path
            if (!SecurityUtils.isValidFilePath(backupPath, listOf(backupDir.absolutePath))) {
                Log.e(TAG, "Invalid backup path: $backupPath")
                return
            }
            
            val backupFile = File(backupPath)
            if (!backupFile.exists()) {
                Log.e(TAG, "Backup file does not exist: $backupPath")
                return
            }
            
            if (backupFile.delete()) {
                Log.d(TAG, "Deleted backup: $backupPath")
                
                // Broadcast success
                val successIntent = Intent("BACKUP_DELETED")
                successIntent.putExtra("backup_path", backupPath)
                sendBroadcast(successIntent)
            } else {
                Log.e(TAG, "Failed to delete backup: $backupPath")
                
                // Broadcast failure
                val failureIntent = Intent("BACKUP_DELETION_FAILED")
                failureIntent.putExtra("backup_path", backupPath)
                failureIntent.putExtra("error", "Failed to delete file")
                sendBroadcast(failureIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting backup: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("BACKUP_DELETION_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Exports a theme to a portable format
     */
    private fun exportTheme(themeName: String, exportPath: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate inputs
            if (SecurityUtils.containsDangerousChars(themeName)) {
                Log.e(TAG, "Dangerous characters detected in theme name")
                return
            }
            
            if (!SecurityUtils.isValidFilePath(exportPath, listOf(filesDir.parent, cacheDir.parent))) {
                Log.e(TAG, "Invalid export path: $exportPath")
                return
            }
            
            // Create export directory if it doesn't exist
            val exportDir = File(exportPath).parentFile
            exportDir?.mkdirs()
            
            // In a real implementation, this would export the theme
            // For this example, we'll simulate the process
            val exportFile = File(exportPath, "${themeName.replace(" ", "_")}.hextheme")
            
            // Create a mock theme export
            ZipOutputStream(FileOutputStream(exportFile)).use { zipOutputStream ->
                // Add theme metadata
                val metadataEntry = ZipEntry("theme_metadata.json")
                zipOutputStream.putNextEntry(metadataEntry)
                val metadata = """
                    {
                        "name": "$themeName",
                        "version": "1.0.0",
                        "created_at": ${System.currentTimeMillis()},
                        "hex_color": "#FF6200EE",
                        "components": ["status_bar", "navigation_bar", "system_ui"],
                        "author": "Hexodus User"
                    }
                """.trimIndent()
                zipOutputStream.write(metadata.toByteArray())
                zipOutputStream.closeEntry()
                
                // Add theme resources (in a real implementation, this would include actual resources)
                val resourcesEntry = ZipEntry("resources/")
                zipOutputStream.putNextEntry(resourcesEntry)
                zipOutputStream.closeEntry()
            }
            
            Log.d(TAG, "Exported theme: $themeName to: ${exportFile.absolutePath}")
            
            // Broadcast success
            val successIntent = Intent("THEME_EXPORTED")
            successIntent.putExtra("theme_name", themeName)
            successIntent.putExtra("export_path", exportFile.absolutePath)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting theme: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("THEME_EXPORT_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Imports a theme from a portable format
     */
    private fun importTheme(themePath: String) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            // Validate theme path
            if (!SecurityUtils.isValidFilePath(themePath, listOf(filesDir.parent, cacheDir.parent))) {
                Log.e(TAG, "Invalid theme path: $themePath")
                return
            }
            
            val themeFile = File(themePath)
            if (!themeFile.exists()) {
                Log.e(TAG, "Theme file does not exist: $themePath")
                return
            }
            
            if (!themeFile.name.endsWith(".hextheme")) {
                Log.e(TAG, "Invalid theme file format: $themePath")
                return
            }
            
            // In a real implementation, this would import the theme
            // For this example, we'll simulate the process
            Log.d(TAG, "Imported theme from: $themePath")
            
            // Broadcast success
            val successIntent = Intent("THEME_IMPORTED")
            successIntent.putExtra("theme_path", themePath)
            sendBroadcast(successIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error importing theme: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("THEME_IMPORT_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Creates backup data based on type and components
     */
    private fun createBackupData(
        type: String,
        includeComponents: Boolean,
        includeAppSettings: Boolean,
        includeSystemSettings: Boolean
    ): BackupData {
        // In a real implementation, this would gather actual theme and settings data
        // For this example, we'll return mock data
        return BackupData(
            themes = listOf(
                ThemeData(
                    name = "My Custom Theme",
                    hexColor = "#FF6200EE",
                    themedComponents = mapOf(
                        "status_bar" to true,
                        "navigation_bar" to true,
                        "system_ui" to true
                    ),
                    createdAt = System.currentTimeMillis(),
                    version = "1.0.0"
                )
            ),
            appSettings = if (includeAppSettings) mapOf(
                "auto_apply_on_boot" to true,
                "backup_frequency" to "weekly",
                "theme_preview_enabled" to true
            ) else emptyMap(),
            systemSettings = if (includeSystemSettings) mapOf(
                "immersive_mode" to true,
                "status_bar_icons_hidden" to false
            ) else emptyMap(),
            overlays = if (type == "full" || type == "overlays_only") listOf(
                OverlayData(
                    packageName = "com.hexodus.overlay.statusbar",
                    name = "Status Bar Theme",
                    enabled = true,
                    priority = 100,
                    targetPackages = listOf("android")
                )
            ) else emptyList()
        )
    }
    
    /**
     * Extracts backup metadata from a backup file
     */
    private fun extractBackupMetadata(backupFile: File): BackupMetadata? {
        try {
            ZipInputStream(FileInputStream(backupFile)).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    if (entry.name == "metadata.json") {
                        val metadataJson = zipInputStream.bufferedReader().readText()
                        // In a real implementation, this would parse the JSON
                        // For this example, we'll return mock data
                        return BackupMetadata(
                            name = "Mock Backup",
                            type = "full",
                            createdAt = System.currentTimeMillis(),
                            version = "1.0.0",
                            includesComponents = true,
                            includesAppSettings = true,
                            includesSystemSettings = true,
                            themeCount = 5,
                            overlayPackages = listOf("com.hexodus.overlay.statusbar")
                        )
                    }
                    entry = zipInputStream.nextEntry
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting backup metadata: ${e.message}", e)
        }
        
        return null
    }
    
    /**
     * Extracts backup data from a backup file
     */
    private fun extractBackupData(backupFile: File): BackupData? {
        try {
            val themes = mutableListOf<ThemeData>()
            val appSettings = mutableMapOf<String, Any>()
            val systemSettings = mutableMapOf<String, Any>()
            val overlays = mutableListOf<OverlayData>()
            
            ZipInputStream(FileInputStream(backupFile)).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        "themes.json" -> {
                            val themesJson = zipInputStream.bufferedReader().readText()
                            // In a real implementation, this would parse the JSON
                            // For this example, we'll add mock data
                            themes.add(
                                ThemeData(
                                    name = "Restored Theme",
                                    hexColor = "#FF03DAC6",
                                    themedComponents = mapOf("status_bar" to true),
                                    createdAt = System.currentTimeMillis(),
                                    version = "1.0.0"
                                )
                            )
                        }
                        "settings.json" -> {
                            val settingsJson = zipInputStream.bufferedReader().readText()
                            // In a real implementation, this would parse the JSON
                            // For this example, we'll add mock data
                            appSettings["auto_apply_on_boot"] = true
                            systemSettings["immersive_mode"] = false
                        }
                        "overlays.json" -> {
                            val overlaysJson = zipInputStream.bufferedReader().readText()
                            // In a real implementation, this would parse the JSON
                            // For this example, we'll add mock data
                            overlays.add(
                                OverlayData(
                                    packageName = "com.hexodus.overlay.navbar",
                                    name = "Navigation Bar Theme",
                                    enabled = true,
                                    priority = 100,
                                    targetPackages = listOf("android")
                                )
                            )
                        }
                    }
                    entry = zipInputStream.nextEntry
                }
            }
            
            return BackupData(
                themes = themes,
                appSettings = appSettings,
                systemSettings = systemSettings,
                overlays = overlays
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting backup data: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Applies app settings from backup
     */
    private fun applyAppSettings(settings: Map<String, Any>) {
        try {
            val prefs = getSharedPreferences("app_settings", 0)
            val editor = prefs.edit()
            
            for ((key, value) in settings) {
                when (value) {
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Set<*> -> editor.putStringSet(key, value as Set<String>)
                }
            }
            
            editor.apply()
            Log.d(TAG, "Applied ${settings.size} app settings from backup")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying app settings: ${e.message}", e)
        }
    }
    
    /**
     * Applies system settings using Shizuku
     */
    private fun applySystemSettings(settings: Map<String, Any>) {
        try {
            if (!shizukuBridgeService.isReady()) {
                Log.e(TAG, "Shizuku is not ready")
                return
            }
            
            for ((key, value) in settings) {
                val command = when (key) {
                    "immersive_mode" -> {
                        if (value as? Boolean == true) {
                            "settings put global policy_control immersive.full=*"
                        } else {
                            "settings put global policy_control null"
                        }
                    }
                    "status_bar_icons_hidden" -> {
                        if (value as? Boolean == true) {
                            "settings put global sysui_demo DURATION_MILLIS 3000; am broadcast -a com.android.systemui.demo -e command enter"
                        } else {
                            "am broadcast -a com.android.systemui.demo -e command exit"
                        }
                    }
                    else -> continue
                }
                
                shizukuBridgeService.executeShellCommand(command)
            }
            
            Log.d(TAG, "Applied ${settings.size} system settings from backup")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying system settings: ${e.message}", e)
        }
    }
    
    /**
     * Gets information about a backup file
     */
    fun getBackupInfo(backupPath: String): Map<String, Any>? {
        try {
            val file = File(backupPath)
            if (!file.exists()) {
                Log.e(TAG, "Backup file does not exist: $backupPath")
                return null
            }
            
            val metadata = extractBackupMetadata(file)
            
            return mapOf(
                "name" to (metadata?.name ?: file.nameWithoutExtension),
                "size" to file.length(),
                "modified" to file.lastModified(),
                "type" to (metadata?.type ?: "unknown"),
                "theme_count" to (metadata?.themeCount ?: 0),
                "valid" to (metadata != null),
                "created_at" to (metadata?.createdAt ?: file.lastModified())
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting backup info: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Validates a backup file
     */
    fun validateBackup(backupPath: String): Boolean {
        try {
            val file = File(backupPath)
            if (!file.exists()) {
                Log.e(TAG, "Backup file does not exist: $backupPath")
                return false
            }
            
            // Check if it's a valid zip file with required entries
            ZipInputStream(FileInputStream(file)).use { zipInputStream ->
                var hasMetadata = false
                var hasThemes = false
                var hasSettings = false
                
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        "metadata.json" -> hasMetadata = true
                        "themes.json" -> hasThemes = true
                        "settings.json" -> hasSettings = true
                    }
                    entry = zipInputStream.nextEntry
                }
                
                // For a basic backup, we need at least metadata
                return hasMetadata
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error validating backup: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Gets all available backups
     */
    fun getAllBackups(): List<Map<String, Any>> {
        try {
            val backupFiles = backupDir.listFiles { file -> 
                file.isFile && file.name.endsWith(".hexbackup") 
            }
            
            return backupFiles?.mapNotNull { file ->
                getBackupInfo(file.absolutePath)
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all backups: ${e.message}", e)
            return emptyList()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "BackupRestoreService destroyed")
    }
    
    // Data classes for backup functionality
    data class BackupData(
        val themes: List<ThemeData>,
        val appSettings: Map<String, Any>,
        val systemSettings: Map<String, Any>,
        val overlays: List<OverlayData>
    )
    
    data class ThemeData(
        val name: String,
        val hexColor: String,
        val themedComponents: Map<String, Boolean>,
        val createdAt: Long,
        val version: String
    )
    
    data class OverlayData(
        val packageName: String,
        val name: String,
        val enabled: Boolean,
        val priority: Int,
        val targetPackages: List<String>
    )
}