package com.hexodus.utils

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hexodus_prefs", Context.MODE_PRIVATE)

    var useDynamicTheming: Boolean
        get() = prefs.getBoolean("use_dynamic_theming", true)
        set(value) = prefs.edit().putBoolean("use_dynamic_theming", value).apply()

    var preferShizukuPlus: Boolean
        get() = prefs.getBoolean("prefer_shizuku_plus", true)
        set(value) = prefs.edit().putBoolean("prefer_shizuku_plus", value).apply()

    var enableDhizukuMode: Boolean
        get() = prefs.getBoolean("enable_dhizuku_mode", false)
        set(value) = prefs.edit().putBoolean("enable_dhizuku_mode", value).apply()

    var showIncompatibleFeatures: Boolean
        get() = prefs.getBoolean("show_incompatible", false)
        set(value) = prefs.edit().putBoolean("show_incompatible", value).apply()

    var overrideCompatibility: Boolean
        get() = prefs.getBoolean("override_compat", false)
        set(value) = prefs.edit().putBoolean("override_compat", value).apply()

    var showDeprecatedTools: Boolean
        get() = prefs.getBoolean("show_deprecated", true)
        set(value) = prefs.edit().putBoolean("show_deprecated", value).apply()

    // Per-feature enabled states (keyed by feature ID e.g. "circle_to_search")
    fun getFeatureEnabled(key: String, default: Boolean = false): Boolean =
        prefs.getBoolean("feature_$key", default)

    fun setFeatureEnabled(key: String, value: Boolean) =
        prefs.edit().putBoolean("feature_$key", value).apply()

    // Theme component selection (persists Status Bar, Nav Bar etc. choices)
    fun getComponentThemed(key: String): Boolean = prefs.getBoolean("themed_$key", true)
    fun setComponentThemed(key: String, value: Boolean) = prefs.edit().putBoolean("themed_$key", value).apply()

    // Theme engine state
    fun getThemeColor(): String = prefs.getString("theme_color", "#FF6200EE") ?: "#FF6200EE"
    fun setThemeColor(value: String) = prefs.edit().putString("theme_color", value).apply()

    fun getThemeName(): String = prefs.getString("theme_name", "My Theme") ?: "My Theme"
    fun setThemeName(value: String) = prefs.edit().putString("theme_name", value).apply()

    // "auto" | "root" | "shizuku" | "shizukuplus" | "adb"
    var preferredPrivilegeMethod: String
        get() = prefs.getString("privilege_method", "auto") ?: "auto"
        set(value) = prefs.edit().putString("privilege_method", value).apply()

    companion object {
        @Volatile
        private var instance: PrefsManager? = null

        fun getInstance(context: Context): PrefsManager =
            instance ?: synchronized(this) {
                instance ?: PrefsManager(context).also { instance = it }
            }
    }
}
