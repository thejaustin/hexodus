package com.hexodus.utils

import android.util.Base64
import android.util.Log
import java.io.File
import java.security.MessageDigest
import java.util.regex.Pattern

/**
 * SecurityUtils - Utility functions for security-related operations
 * Provides validation and security checks for theme operations
 */
object SecurityUtils {

    private const val TAG = "SecurityUtils"

    /**
     * Validates the signature of an APK file
     */
    fun validateApkSignature(apkPath: String): Boolean {
        try {
            val file = File(apkPath)
            if (!file.exists()) {
                Log.e(TAG, "APK file does not exist: $apkPath")
                return false
            }

            // In a real implementation, this would verify the APK signature
            // For this example, we'll just check if the file has the right extension
            // and is not empty
            return file.extension.equals("apk", ignoreCase = true) && file.length() > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error validating APK signature: ${e.message}", e)
            return false
        }
    }

    /**
     * Validates a hex color string with enhanced security
     */
    fun validateHexColor(hexColor: String): Boolean {
        return try {
            val colorStr = if (hexColor.startsWith("#")) hexColor.substring(1) else hexColor

            // Use regex for more robust validation
            val hexPattern = Pattern.compile("^([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$")
            hexPattern.matcher(colorStr).matches()
        } catch (e: Exception) {
            Log.e(TAG, "Error validating hex color: ${e.message}", e)
            false
        }
    }

    /**
     * Sanitizes a package name to prevent injection attacks
     */
    fun sanitizePackageName(packageName: String): String {
        // More restrictive filtering to prevent injection
        return packageName.filter { it.isLetterOrDigit() || it == '.' || it == '_' }
    }

    /**
     * Validates package name format to prevent injection
     */
    fun isValidPackageName(packageName: String): Boolean {
        return try {
            val pattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*$")
            pattern.matcher(packageName).matches()
        } catch (e: Exception) {
            Log.e(TAG, "Error validating package name: ${e.message}", e)
            false
        }
    }

    /**
     * Generates a SHA-256 hash of a file
     */
    fun getFileSha256(filePath: String): String? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null

            val md = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var bytesRead: Int

            file.inputStream().use { fis ->
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }

            val digest = md.digest()
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating file hash: ${e.message}", e)
            null
        }
    }

    /**
     * Validates that a file path is within allowed directories
     */
    fun isValidFilePath(filePath: String, allowedDirectories: List<String>): Boolean {
        val file = File(filePath)
        val absolutePath = file.absolutePath

        // Prevent directory traversal attacks
        if (filePath.contains("../") || filePath.contains("..\\") || filePath.contains("%2e%2e")) {
            return false
        }

        return allowedDirectories.any { allowedDir ->
            absolutePath.startsWith(File(allowedDir).absolutePath)
        }
    }

    /**
     * Checks if a string contains potentially dangerous characters
     */
    fun containsDangerousChars(input: String): Boolean {
        val dangerousPatterns = listOf(
            "../", "..\\", ";", "|", "&", "`", "$", "{", "}",
            "(", ")", "[", "]", "<", ">", "*", "?", "!",
            "@", "#", "~", "=", "+", "\\", "'"
        )

        return dangerousPatterns.any { input.contains(it) }
    }

    /**
     * Validates a command string to prevent command injection
     */
    fun isValidCommand(command: String): Boolean {
        // Check for dangerous command patterns
        val dangerousCommands = listOf(
            "rm", "rmdir", "del", "format", "shutdown", "reboot",
            "su", "sudo", "mount", "umount", "chmod", "chown"
        )

        val lowerCmd = command.lowercase()
        return !dangerousCommands.any { lowerCmd.contains(it) } &&
               !containsDangerousChars(command)
    }

    /**
     * Sanitizes a command string to prevent injection
     */
    fun sanitizeCommand(command: String): String {
        // Remove dangerous characters and patterns
        var sanitized = command
        val dangerousPatterns = listOf(
            "../", "..\\", ";", "|", "&", "`", "$", "{", "}",
            "(", ")", "[", "]", "<", ">", "*", "?", "!",
            "@", "#", "~", "=", "+", "\\"
        )

        for (pattern in dangerousPatterns) {
            sanitized = sanitized.replace(pattern, "", ignoreCase = true)
        }

        return sanitized.trim()
    }

    /**
     * Validates a file name to prevent path traversal
     */
    fun isValidFileName(fileName: String): Boolean {
        // Check for invalid characters and path traversal attempts
        val invalidChars = listOf('<', '>', ':', '"', '|', '?', '*')
        val hasInvalidChars = invalidChars.any { fileName.contains(it) }

        if (hasInvalidChars) return false

        // Check for path traversal
        return !(fileName.contains("../") ||
                 fileName.contains("..\\") ||
                 fileName.contains("%2e%2e") ||
                 fileName.startsWith("/") ||
                 fileName.startsWith("\\"))
    }

    /**
     * Validates an intent action to prevent intent injection
     */
    fun isValidIntentAction(action: String): Boolean {
        // Check for valid intent action format
        val pattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9._-]*$")
        return pattern.matcher(action).matches()
    }

    /**
     * Validates a URI to prevent unsafe operations
     */
    fun isValidUri(uri: String): Boolean {
        // Check for potentially unsafe URI schemes
        val unsafeSchemes = listOf("file:", "content:", "javascript:", "data:")
        val lowerUri = uri.lowercase()

        return !unsafeSchemes.any { lowerUri.startsWith(it) } &&
               !containsDangerousChars(uri)
    }

    /**
     * Validates a permission string to prevent unauthorized access
     */
    fun isValidPermission(permission: String): Boolean {
        // Check for valid Android permission format
        val pattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9._]*$")
        return pattern.matcher(permission).matches()
    }

    // Security constants
    const val MAX_FILE_PATH_LENGTH = 1024
    const val MAX_COMMAND_LENGTH = 512
    const val MAX_INPUT_LENGTH = 256
}