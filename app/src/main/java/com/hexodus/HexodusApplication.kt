package com.hexodus

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import com.hexodus.utils.CrashHandler
import io.sentry.SentryEvent
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import rikka.shizuku.Shizuku

class HexodusApplication : Application() {
    companion object {
        private const val TAG = "HexodusApplication"
        lateinit var context: android.content.Context
            private set

        // Safe initialization to avoid crashes if Shizuku provider is not yet ready
        var IS_SHIZUKU_SUPPORTED: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        initSentry()
        CrashHandler.setup(this)
        checkShizukuSupport()
        initShizuku()
    }

    private fun initSentry() {
        SentryAndroid.init(this) { options ->
            options.dsn = "https://befafa4507a58a7e9d2e8eacc380c7bd@o4510887187841024.ingest.us.sentry.io/4510970837532672"
            options.environment = BuildConfig.BUILD_TYPE
            options.release = "hexodus@${BuildConfig.VERSION_NAME}"

            // Performance
            options.tracesSampleRate = 0.2
            options.profilesSampleRate = 0.1
            options.isEnableAppStartProfiling = true

            // ANR detection
            options.isAnrEnabled = true
            options.anrTimeoutIntervalMillis = 5000

            // Session health tracking
            options.isEnableAutoSessionTracking = true

            // Breadcrumbs
            options.isEnableUserInteractionBreadcrumbs = true
            options.isEnableAppLifecycleBreadcrumbs = true
            options.isEnableSystemEventBreadcrumbs = true
            options.isEnableNetworkEventBreadcrumbs = true

            // Privacy
            options.isSendDefaultPii = false
            options.isAttachScreenshot = false

            // Filter out expected Shizuku exceptions to reduce noise
            options.beforeSend = SentryOptions.BeforeSendCallback { event, _ ->
                filterShizukuNoise(event)
            }

            // Device tags for Samsung/foldable filtering in dashboard
            options.setTag("manufacturer", Build.MANUFACTURER.lowercase())
            options.setTag("device_model", Build.MODEL)
            options.setTag("android_api", Build.VERSION.SDK_INT.toString())
            options.setTag("is_samsung", (Build.MANUFACTURER.equals("samsung", ignoreCase = true)).toString())
            options.setTag("is_foldable", isFoldableDevice().toString())
        }
    }

    private fun filterShizukuNoise(event: SentryEvent): SentryEvent? {
        val throwable = event.throwable ?: return event
        return when {
            // Shizuku binder died — happens whenever Shizuku stops/restarts
            throwable is android.os.DeadObjectException -> null
            // Permission denied before Shizuku grants access
            throwable is SecurityException &&
                throwable.message?.contains("shizuku", ignoreCase = true) == true -> null
            // Shizuku IPC failures
            throwable is android.os.RemoteException &&
                throwable.stackTrace.any { it.className.contains("shizuku", ignoreCase = true) } -> null
            else -> event
        }
    }

    private fun isFoldableDevice(): Boolean =
        Build.MODEL.contains("fold", ignoreCase = true) ||
        Build.MODEL.contains("flip", ignoreCase = true)

    private fun checkShizukuSupport() {
        IS_SHIZUKU_SUPPORTED = try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            Log.e(TAG, "Shizuku not supported or not yet initialized", e)
            false
        }
    }

    private fun initShizuku() {
        try {
            Shizuku.addBinderReceivedListener(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
            Log.d(TAG, "Shizuku listeners added")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add Shizuku listeners", e)
        }
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "Shizuku binder received")
        // Standard broadcast (LocalBroadcastManager is deprecated)
        sendBroadcast(Intent("SHIZUKU_BINDER_RECEIVED").setPackage(packageName))
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.d(TAG, "Shizuku binder dead")
        sendBroadcast(Intent("SHIZUKU_BINDER_DEAD").setPackage(packageName))
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        Log.d(TAG, "Shizuku permission result: $requestCode, $grantResult")
        val intent = Intent("SHIZUKU_PERMISSION_RESULT")
        intent.setPackage(packageName)
        intent.putExtra("requestCode", requestCode)
        intent.putExtra("grantResult", grantResult)
        sendBroadcast(intent)
    }

    override fun onTerminate() {
        super.onTerminate()
        cleanupShizukuListeners()
    }

    private fun cleanupShizukuListeners() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
            Log.d(TAG, "Shizuku listeners cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up Shizuku listeners: ${e.message}", e)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "Application running low on memory")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.w(TAG, "Memory trim level: RUNNING_${
                    when (level) {
                        TRIM_MEMORY_RUNNING_MODERATE -> "MODERATE"
                        TRIM_MEMORY_RUNNING_LOW -> "LOW"
                        TRIM_MEMORY_RUNNING_CRITICAL -> "CRITICAL"
                        else -> level
                    }
                }")
            }
            TRIM_MEMORY_UI_HIDDEN -> {
                Log.d(TAG, "UI hidden, releasing memory")
            }
            TRIM_MEMORY_BACKGROUND,
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_COMPLETE -> {
                Log.w(TAG, "Memory trim level: ${
                    when (level) {
                        TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
                        TRIM_MEMORY_MODERATE -> "MODERATE"
                        TRIM_MEMORY_COMPLETE -> "COMPLETE"
                        else -> level
                    }
                }")
                cleanupShizukuListeners()
            }
        }
    }
}
