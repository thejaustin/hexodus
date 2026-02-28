package moe.shizuku.plus

import rikka.shizuku.ShizukuPlusAPI as RealAPI
import android.os.IBinder

/**
 * ShizukuPlusAPI Wrapper.
 * This object provides a bridge to the rikka.shizuku.ShizukuPlusAPI library
 * while ensuring backward compatibility with standard Shizuku servers.
 */
object ShizukuPlusAPI {

    /**
     * Check if the connected server supports Shizuku+ Enhanced API features.
     */
    fun isEnhancedApiSupported(): Boolean {
        return try {
            RealAPI.isEnhancedApiSupported()
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
            return try {
                val res = RealAPI.Shell.executeCommand(command)
                Result(res.output, res.exitCode)
            } catch (e: Exception) {
                val output = com.hexodus.services.ShizukuBridge.executeShellCommand(command) ?: ""
                Result(output, 0)
            }
        }
    }

    object OverlayManager {
        fun enableOverlay(packageName: String): Boolean {
            return try {
                RealAPI.OverlayManager.enableOverlay(packageName)
            } catch (e: Exception) {
                com.hexodus.services.ShizukuBridge.executeOverlayCommand(packageName, "enable")
            }
        }

        fun disableOverlay(packageName: String): Boolean {
            return try {
                RealAPI.OverlayManager.disableOverlay(packageName)
            } catch (e: Exception) {
                com.hexodus.services.ShizukuBridge.executeOverlayCommand(packageName, "disable")
            }
        }
    }

    object PackageManager {
        fun installPackage(path: String): Boolean {
            return try {
                RealAPI.PackageManager.installPackage(path)
            } catch (e: Exception) {
                com.hexodus.services.ShizukuBridge.installApk(path)
            }
        }

        fun uninstallPackage(packageName: String): Boolean {
            return try {
                RealAPI.PackageManager.uninstallPackage(packageName)
            } catch (e: Exception) {
                com.hexodus.services.ShizukuBridge.uninstallPackage(packageName)
            }
        }
    }

    object Settings {
        fun putSystem(key: String, value: Any): Boolean {
            return try {
                RealAPI.Settings.putSystem(key, value.toString())
            } catch (e: Exception) {
                com.hexodus.services.ShizukuBridge.executeShellCommand("settings put system $key $value") != null
            }
        }

        fun getSystem(key: String): String? {
            return try {
                RealAPI.Settings.getSystem(key)
            } catch (e: Exception) {
                com.hexodus.services.ShizukuBridge.executeShellCommand("settings get system $key")
            }
        }

        fun putSecure(key: String, value: Any): Boolean {
            return try {
                RealAPI.Settings.putSecure(key, value.toString())
            } catch (e: Exception) {
                com.hexodus.services.ShizukuBridge.executeShellCommand("settings put secure $key $value") != null
            }
        }

        fun putGlobal(key: String, value: Any): Boolean {
            return try {
                RealAPI.Settings.putGlobal(key, value.toString())
            } catch (e: Exception) {
                com.hexodus.services.ShizukuBridge.executeShellCommand("settings put global $key $value") != null
            }
        }
    }

    object Dhizuku {
        fun isAvailable(): Boolean {
            return try {
                RealAPI.Dhizuku.isAvailable()
            } catch (e: Exception) {
                false
            }
        }

        fun getBinder(): IBinder? {
            return try {
                RealAPI.Dhizuku.getBinder()
            } catch (e: Exception) {
                null
            }
        }
    }
}
