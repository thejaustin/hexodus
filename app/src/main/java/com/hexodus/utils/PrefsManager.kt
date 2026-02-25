package com.hexodus.utils

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hexodus_prefs", Context.MODE_PRIVATE)

    var preferShizukuPlus: Boolean
        get() = prefs.getBoolean("prefer_shizuku_plus", true)
        set(value) = prefs.edit().putBoolean("prefer_shizuku_plus", value).apply()

    var enableDhizukuMode: Boolean
        get() = prefs.getBoolean("enable_dhizuku_mode", false)
        set(value) = prefs.edit().putBoolean("enable_dhizuku_mode", value).apply()

    companion object {
        @Volatile
        private var instance: PrefsManager? = null

        fun getInstance(context: Context): PrefsManager =
            instance ?: synchronized(this) {
                instance ?: PrefsManager(context).also { instance = it }
            }
    }
}
