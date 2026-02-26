package com.hexodus.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Date
import kotlin.system.exitProcess

/**
 * Global Crash Handler to catch uncaught exceptions and log them to a file
 * for better debugging of system-level issues.
 */
class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    companion object {
        private const val TAG = "CrashHandler"
        private const val CRASH_LOG_FILE = "latest_crash.log"

        fun setup(context: Context) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context))
        }

        fun getCrashLog(context: Context): String? {
            val file = File(context.filesDir, CRASH_LOG_FILE)
            return if (file.exists()) file.readText() else null
        }

        fun clearCrashLog(context: Context) {
            val file = File(context.filesDir, CRASH_LOG_FILE)
            if (file.exists()) file.delete()
        }
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val stackTrace = StringWriter()
        throwable.printStackTrace(PrintWriter(stackTrace))
        
        val report = buildString {
            append("--- Hexodus Crash Report ---\n")
            append("Timestamp: ${Date()}\n")
            append("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
            append("Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n")
            append("Thread: ${thread.name}\n")
            append("\nStack Trace:\n")
            append(stackTrace.toString())
            append("\n---------------------------\n")
        }

        Log.e(TAG, "Uncaught Exception detected!")
        Log.e(TAG, report)

        // Save to file
        try {
            val file = File(context.filesDir, CRASH_LOG_FILE)
            file.writeText(report)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save crash log: ${e.message}")
        }

        // We can't easily start a new activity from a dying process in all cases,
        // but we'll try to notify the system or the next launch.
        
        // Let the system handle it or terminate
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, throwable)
        } else {
            exitProcess(1)
        }
    }
}
