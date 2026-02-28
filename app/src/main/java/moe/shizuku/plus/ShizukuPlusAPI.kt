package moe.shizuku.plus

import android.os.IBinder
import android.util.Log
import com.hexodus.services.ShizukuBridge
import rikka.shizuku.Shizuku

/**
 * ShizukuPlusAPI Wrapper.
 * This object provides a bridge to Shizuku+ features using reflection.
 * No extra library dependencies are required, preventing build failures and code bloat.
 */
object ShizukuPlusAPI {

    private const val TAG = "ShizukuPlusAPI"

    /**
     * Check if the connected server supports Shizuku+ Enhanced API features.
     */
    fun isEnhancedApiSupported(): Boolean {
        return try {
            val method = Shizuku::class.java.getDeclaredMethod("isCustomApiEnabled")
            method.isAccessible = true
            method.invoke(null) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    object Shell {
        data class Result(val output: String, val exitCode: Int = 0) {
            fun isSuccess() = exitCode == 0
        }
        
        /**
         * Execute a shell command synchronously.
         */
        fun executeCommand(command: String): Result {
            val output = ShizukuBridge.executeShellCommand(command) ?: ""
            return Result(output, 0)
        }
    }

    object OverlayManager {
        /**
         * Enable a system overlay.
         */
        fun enableOverlay(packageName: String): Boolean {
            return ShizukuBridge.executeOverlayCommand(packageName, "enable")
        }

        /**
         * Disable a system overlay.
         */
        fun disableOverlay(packageName: String): Boolean {
            return ShizukuBridge.executeOverlayCommand(packageName, "disable")
        }
    }

    object PackageManager {
        /**
         * Install an APK file.
         */
        fun installPackage(path: String): Boolean {
            return ShizukuBridge.installApk(path)
        }

        /**
         * Uninstall a package.
         */
        fun uninstallPackage(packageName: String): Boolean {
            return ShizukuBridge.uninstallPackage(packageName)
        }
    }

    object Settings {
        fun putSystem(key: String, value: Any): Boolean {
            return ShizukuBridge.executeShellCommand("settings put system $key $value") != null
        }

        fun getSystem(key: String): String? {
            return ShizukuBridge.executeShellCommand("settings get system $key")
        }

        fun putSecure(key: String, value: Any): Boolean {
            return ShizukuBridge.executeShellCommand("settings put secure $key $value") != null
        }

        fun putGlobal(key: String, value: Any): Boolean {
            return ShizukuBridge.executeShellCommand("settings put global $key $value") != null
        }
    }

    object Dhizuku {
        /**
         * Check if Dhizuku mode is active.
         */
        fun isAvailable(): Boolean {
            return getBinder() != null
        }

        /**
         * Get the DevicePolicyManager binder using reflection.
         */
        fun getBinder(): IBinder? {
            return try {
                val dhizukuField = Shizuku::class.java.getDeclaredField("Dhizuku")
                dhizukuField.isAccessible = true
                val dhizukuObj = dhizukuField.get(null)
                val getBinderMethod = dhizukuObj.javaClass.getDeclaredMethod("getBinder")
                getBinderMethod.isAccessible = true
                getBinderMethod.invoke(dhizukuObj) as IBinder?
            } catch (e: Exception) {
                null
            }
        }
    }
}
