package moe.shizuku.plus

/**
 * Stub implementation of ShizukuPlusAPI.
 * isEnhancedApiSupported() always returns false so all callers fall through
 * to their existing standard-Shizuku fallback paths.
 */
object ShizukuPlusAPI {

    fun isEnhancedApiSupported(): Boolean = false

    object Shell {
        data class Result(val output: String)
        fun executeCommand(command: String): Result =
            throw UnsupportedOperationException("ShizukuPlus not available")
    }

    object OverlayManager {
        fun enableOverlay(packageName: String): Unit =
            throw UnsupportedOperationException("ShizukuPlus not available")
        fun disableOverlay(packageName: String): Unit =
            throw UnsupportedOperationException("ShizukuPlus not available")
    }

    object PackageManager {
        fun installPackage(path: String): Unit =
            throw UnsupportedOperationException("ShizukuPlus not available")
        fun uninstallPackage(packageName: String): Unit =
            throw UnsupportedOperationException("ShizukuPlus not available")
    }

    object Settings {
        fun putSystem(key: String, value: Any): Unit =
            throw UnsupportedOperationException("ShizukuPlus not available")
        fun getSystem(key: String): String? =
            throw UnsupportedOperationException("ShizukuPlus not available")
        fun putGlobal(key: String, value: Any): Unit =
            throw UnsupportedOperationException("ShizukuPlus not available")
        fun putSecure(key: String, value: Any): Unit =
            throw UnsupportedOperationException("ShizukuPlus not available")
    }

    object Dhizuku {
        fun isAvailable(): Boolean = false
    }
}
