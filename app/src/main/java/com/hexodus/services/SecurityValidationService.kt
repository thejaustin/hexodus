package com.hexodus.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hexodus.utils.SecurityUtils

/**
 * SecurityValidationService - Service for performing security validations
 * Ensures all operations comply with security best practices
 */
class SecurityValidationService : Service() {
    
    companion object {
        private const val TAG = "SecurityValidationService"
        private const val ACTION_VALIDATE_INPUT = "com.hexodus.VALIDATE_INPUT"
        private const val ACTION_VALIDATE_FILE_PATH = "com.hexodus.VALIDATE_FILE_PATH"
        private const val ACTION_VALIDATE_COMMAND = "com.hexodus.VALIDATE_COMMAND"
        private const val ACTION_VALIDATE_PACKAGE_NAME = "com.hexodus.VALIDATE_PACKAGE_NAME"
        private const val ACTION_SANITIZE_INPUT = "com.hexodus.SANITIZE_INPUT"
        
        // Intent extras
        const val EXTRA_INPUT_STRING = "input_string"
        const val EXTRA_FILE_PATH = "file_path"
        const val EXTRA_ALLOWED_DIRECTORIES = "allowed_directories"
        const val EXTRA_COMMAND = "command"
        const val EXTRA_PACKAGE_NAME = "package_name"
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_VALIDATE_INPUT -> {
                val input = intent?.getStringExtra(EXTRA_INPUT_STRING)
                if (!input.isNullOrEmpty()) {
                    validateInput(input)
                }
            }
            ACTION_VALIDATE_FILE_PATH -> {
                val filePath = intent?.getStringExtra(EXTRA_FILE_PATH)
                val allowedDirs = intent?.getStringArrayListExtra(EXTRA_ALLOWED_DIRECTORIES) ?: listOf()
                
                if (!filePath.isNullOrEmpty()) {
                    validateFilePath(filePath, allowedDirs)
                }
            }
            ACTION_VALIDATE_COMMAND -> {
                val command = intent?.getStringExtra(EXTRA_COMMAND)
                if (!command.isNullOrEmpty()) {
                    validateCommand(command)
                }
            }
            ACTION_VALIDATE_PACKAGE_NAME -> {
                val packageName = intent?.getStringExtra(EXTRA_PACKAGE_NAME)
                if (!packageName.isNullOrEmpty()) {
                    validatePackageName(packageName)
                }
            }
            ACTION_SANITIZE_INPUT -> {
                val input = intent?.getStringExtra(EXTRA_INPUT_STRING)
                if (!input.isNullOrEmpty()) {
                    sanitizeInput(input)
                }
            }
        }
        
        return START_STICKY
    }
    
    /**
     * Validates an input string for security compliance
     */
    private fun validateInput(input: String) {
        try {
            val results = mutableMapOf<String, Any>()
            
            // Check length
            results["valid_length"] = input.length <= SecurityUtils.MAX_INPUT_LENGTH
            
            // Check for dangerous characters
            results["contains_dangerous_chars"] = !SecurityUtils.containsDangerousChars(input)
            
            // Validate based on expected format (if it's a hex color)
            if (input.startsWith("#") || input.matches(Regex("[A-Fa-f0-9]+"))) {
                results["valid_hex_format"] = SecurityUtils.validateHexColor(input)
            }
            
            Log.d(TAG, "Input validation completed for: ${input.take(20)}...")
            
            // Broadcast results
            val intent = Intent("INPUT_VALIDATION_COMPLETED")
            intent.putExtra("input", input)
            intent.putExtra("validation_results", results)
            sendBroadcast(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating input: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("INPUT_VALIDATION_ERROR")
            errorIntent.putExtra("input", input)
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Validates a file path for security compliance
     */
    private fun validateFilePath(filePath: String, allowedDirectories: List<String>) {
        try {
            val results = mutableMapOf<String, Any>()
            
            // Check length
            results["valid_length"] = filePath.length <= SecurityUtils.MAX_FILE_PATH_LENGTH
            
            // Check for dangerous characters
            results["contains_dangerous_chars"] = !SecurityUtils.containsDangerousChars(filePath)
            
            // Validate path is within allowed directories
            results["valid_path"] = SecurityUtils.isValidFilePath(filePath, allowedDirectories)
            
            // Validate file name
            results["valid_filename"] = SecurityUtils.isValidFileName(filePath.substringAfterLast("/", filePath))
            
            Log.d(TAG, "File path validation completed for: $filePath")
            
            // Broadcast results
            val intent = Intent("FILE_PATH_VALIDATION_COMPLETED")
            intent.putExtra("file_path", filePath)
            intent.putExtra("allowed_directories", ArrayList(allowedDirectories))
            intent.putExtra("validation_results", results)
            sendBroadcast(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating file path: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("FILE_PATH_VALIDATION_ERROR")
            errorIntent.putExtra("file_path", filePath)
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Validates a command string for security compliance
     */
    private fun validateCommand(command: String) {
        try {
            val results = mutableMapOf<String, Any>()
            
            // Check length
            results["valid_length"] = command.length <= SecurityUtils.MAX_COMMAND_LENGTH
            
            // Check for dangerous characters
            results["contains_dangerous_chars"] = !SecurityUtils.containsDangerousChars(command)
            
            // Validate command is safe
            results["safe_command"] = SecurityUtils.isValidCommand(command)
            
            Log.d(TAG, "Command validation completed for: ${command.take(30)}...")
            
            // Broadcast results
            val intent = Intent("COMMAND_VALIDATION_COMPLETED")
            intent.putExtra("command", command)
            intent.putExtra("validation_results", results)
            sendBroadcast(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating command: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("COMMAND_VALIDATION_ERROR")
            errorIntent.putExtra("command", command)
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Validates a package name for security compliance
     */
    private fun validatePackageName(packageName: String) {
        try {
            val results = mutableMapOf<String, Any>()
            
            // Validate package name format
            results["valid_format"] = SecurityUtils.isValidPackageName(packageName)
            
            // Check for dangerous characters
            results["contains_dangerous_chars"] = !SecurityUtils.containsDangerousChars(packageName)
            
            Log.d(TAG, "Package name validation completed for: $packageName")
            
            // Broadcast results
            val intent = Intent("PACKAGE_NAME_VALIDATION_COMPLETED")
            intent.putExtra("package_name", packageName)
            intent.putExtra("validation_results", results)
            sendBroadcast(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating package name: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("PACKAGE_NAME_VALIDATION_ERROR")
            errorIntent.putExtra("package_name", packageName)
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Sanitizes an input string
     */
    private fun sanitizeInput(input: String) {
        try {
            var sanitized = input
            
            // Sanitize based on type
            if (input.startsWith("am ") || input.startsWith("pm ")) {
                // Command-like input
                sanitized = SecurityUtils.sanitizeCommand(input)
            } else if (input.contains("/")) {
                // Path-like input
                sanitized = input.filter { it.isLetterOrDigit() || it in "./-_~" }
            } else {
                // General input
                sanitized = input.filter { !SecurityUtils.containsDangerousChars(it.toString()) }
            }
            
            Log.d(TAG, "Input sanitized from: ${input.take(20)}... to: ${sanitized.take(20)}...")
            
            // Broadcast results
            val intent = Intent("INPUT_SANITIZATION_COMPLETED")
            intent.putExtra("original_input", input)
            intent.putExtra("sanitized_input", sanitized)
            sendBroadcast(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sanitizing input: ${e.message}", e)
            
            // Broadcast error
            val errorIntent = Intent("INPUT_SANITIZATION_ERROR")
            errorIntent.putExtra("input", input)
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
        }
    }
    
    /**
     * Performs a comprehensive security check on a theme package
     */
    fun validateThemePackage(apkPath: String): Boolean {
        try {
            // Validate file path
            if (!SecurityUtils.isValidFilePath(apkPath, listOf(filesDir.parent, cacheDir.parent))) {
                Log.e(TAG, "Invalid APK path: $apkPath")
                return false
            }
            
            // Validate file name
            val fileName = apKPath.substringAfterLast("/")
            if (!SecurityUtils.isValidFileName(fileName)) {
                Log.e(TAG, "Invalid APK filename: $fileName")
                return false
            }
            
            // Validate APK signature
            if (!SecurityUtils.validateApkSignature(apkPath)) {
                Log.e(TAG, "Invalid APK signature: $apkPath")
                return false
            }
            
            Log.d(TAG, "Theme package validation passed: $apkPath")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error validating theme package: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Validates a hex color string
     */
    fun validateHexColor(hexColor: String): Boolean {
        return SecurityUtils.validateHexColor(hexColor)
    }
    
    /**
     * Sanitizes a package name
     */
    fun sanitizePackageName(packageName: String): String {
        return SecurityUtils.sanitizePackageName(packageName)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SecurityValidationService destroyed")
    }
}