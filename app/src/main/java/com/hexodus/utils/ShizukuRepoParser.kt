package com.hexodus.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

/**
 * ShizukuRepoParser - Utility to fetch and parse the awesome-shizuku repository list
 * directly from GitHub's README.md to ensure the app is always up-to-date.
 */
object ShizukuRepoParser {

    private const val TAG = "ShizukuRepoParser"
    private const val RAW_README_URL = "https://raw.githubusercontent.com/timschneeb/awesome-shizuku/master/README.md"

    data class ShizukuApp(
        val name: String,
        val description: String,
        val author: String,
        val repoUrl: String,
        val downloadUrl: String,
        val category: String,
        val tags: List<String>
    )

    /**
     * Fetches and parses the live awesome-shizuku list from GitHub.
     */
    suspend fun fetchAwesomeShizukuList(): List<ShizukuApp> = withContext(Dispatchers.IO) {
        val apps = mutableListOf<ShizukuApp>()
        try {
            val url = URL(RAW_README_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val content = connection.inputStream.bufferedReader().use { it.readText() }
                apps.addAll(parseMarkdown(content))
            } else {
                Log.e(TAG, "Failed to fetch README: ${connection.responseCode}")
                // Fallback to internal list if network fails
                apps.addAll(getFallbackList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching awesome-shizuku list", e)
            apps.addAll(getFallbackList())
        }
        
        return@withContext apps
    }

    /**
     * Parses the Markdown content from the README.
     * Extracts categories, app names, links, and descriptions.
     */
    private fun parseMarkdown(content: String): List<ShizukuApp> {
        val apps = mutableListOf<ShizukuApp>()
        val lines = content.lines()
        var currentCategory = "General"

        // Regex to match: - [App Name](URL) - Description.
        // Also supports variants like - [App Name](URL) Description.
        val appPattern = Pattern.compile("^\\s*-\\s*\\[([^\\]]+)\\]\\(([^\\)]+)\\)\\s*[:-]?\\s*(.*)$")

        for (line in lines) {
            val trimmed = line.trim()
            
            // Detect categories (Headers)
            if (trimmed.startsWith("### ")) {
                currentCategory = trimmed.replace("### ", "").trim()
                continue
            }

            val matcher = appPattern.matcher(trimmed)
            if (matcher.find()) {
                val name = matcher.group(1) ?: ""
                val url = matcher.group(2) ?: ""
                var description = matcher.group(3) ?: ""
                
                // Clean up description (remove trailing credits/links)
                description = description.split(" ([")[0].trim()

                // Determine author from URL if possible
                val author = extractAuthor(url)
                
                // Construct a release download URL (GitHub specific)
                val downloadUrl = if (url.contains("github.com")) {
                    "${url.removeSuffix("/")}/releases/latest"
                } else {
                    url
                }

                apps.add(
                    ShizukuApp(
                        name = name,
                        description = description,
                        author = author,
                        repoUrl = url,
                        downloadUrl = downloadUrl,
                        category = currentCategory,
                        tags = extractTags(name, description, currentCategory)
                    )
                )
            }
        }
        
        // Add our elite backports if they aren't parsed from the awesome-shizuku list
        val fallbackApps = getFallbackList()
        val existingNames = apps.map { it.name }
        for (fallback in fallbackApps) {
            if (!existingNames.contains(fallback.name)) {
                apps.add(fallback)
            }
        }

        return apps
    }

    private fun extractTags(name: String, desc: String, category: String): List<String> {
        val tags = mutableSetOf<String>()
        val searchString = "$name $desc $category".lowercase()

        if (searchString.contains("shizuku+") || searchString.contains("shizuku plus")) tags.add("Shizuku+")
        if (searchString.contains("shizuku") || category.contains("Shizuku")) tags.add("Shizuku")
        if (searchString.contains("dhizuku") || searchString.contains("device owner")) tags.add("Dhizuku")
        if (searchString.contains("lspatch") || searchString.contains("xposed")) tags.add("LSPatch")
        if (searchString.contains("vector") || searchString.contains("jingmatrix")) tags.add("Vector")
        if (searchString.contains("root") || searchString.contains("magisk")) tags.add("Root")
        if (searchString.contains("adb") || searchString.contains("shell")) tags.add("ADB")
        if (searchString.contains("backport") || category.contains("Backport") || searchString.contains("pixel-exclusive")) tags.add("Backport")
        if (searchString.contains("samsung") || searchString.contains("one ui") || searchString.contains("galaxy")) tags.add("Samsung")
        if (searchString.contains("developer") || searchString.contains("debug") || category.contains("Developer Tools")) tags.add("Dev")

        // Ensure every app has at least one tag, defaulting to Shizuku if none found on a Shizuku list
        if (tags.isEmpty()) {
            tags.add("Shizuku")
        }

        return tags.toList().sorted()
    }

    private fun extractAuthor(url: String): String {
        return try {
            if (url.contains("github.com/")) {
                url.substringAfter("github.com/").substringBefore("/")
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getFallbackList(): List<ShizukuApp> {
        return listOf(
            ShizukuApp("LSPatch", "No-root Xposed module injector. Use it to patch Google Photos for Magic Editor.", "LSPosed", "https://github.com/LSPosed/LSPatch", "https://github.com/LSPosed/LSPatch/releases/latest", "System Tools", listOf("LSPatch", "Shizuku")),
            ShizukuApp("Vector", "The 2026 Kotlin-based successor to LSPosed. Optimized for ART on Android 16.", "JingMatrix", "https://github.com/JingMatrix/Vector", "https://github.com/JingMatrix/Vector/releases/latest", "System Tools", listOf("Vector", "Root", "Dev")),
            ShizukuApp("Pixelify Google Photos", "Module for LSPatch to unlock Pixel-exclusive Magic Editor, Reimagine, and unlimited saves.", "BaltiApps", "https://github.com/BaltiApps/Pixelify-Google-Photos", "https://github.com/BaltiApps/Pixelify-Google-Photos/releases/latest", "Pixel Backport", listOf("Backport", "LSPatch")),
            ShizukuApp("Ambient Music Mod", "Pixel-exclusive 'Now Playing' offline music recognition for any device.", "KieronQuinn", "https://github.com/KieronQuinn/AmbientMusicMod", "https://github.com/KieronQuinn/AmbientMusicMod/releases/latest", "Pixel Backport", listOf("Backport", "Shizuku")),
            ShizukuApp("Smartspacer", "At a Glance replacement with Pixel features and extensibility.", "KieronQuinn", "https://github.com/KieronQuinn/Smartspacer", "https://github.com/KieronQuinn/Smartspacer/releases/latest", "Pixel Backport", listOf("Backport", "Shizuku")),
            ShizukuApp("Galaxy Enhancements", "Unlock One UI 7.0 features and S25 AI toggles on older Samsung devices.", "Community", "https://github.com/Feruzbek_101/GalaxyEnhancements", "https://github.com/Feruzbek_101/GalaxyEnhancements/releases/latest", "Samsung Backport", listOf("Backport", "Samsung", "Shizuku")),
            ShizukuApp("GCam (LMC 8.4)", "Advanced Google Camera port optimized for S22 Ultra 108MP sensor with Leica mode.", "Hasli", "https://github.com/HasliR/LMC8.4", "https://github.com/HasliR/LMC8.4/releases/latest", "Camera Enhancements", listOf("Backport")),
            ShizukuApp("GMS Phixit", "Toggle hidden Google Play Services flags to unlock AI features without root.", "Wanderer", "https://github.com/WandererV/GMS-Phixit", "https://github.com/WandererV/GMS-Phixit/releases/latest", "System Tools", listOf("Shizuku", "Root")),
            ShizukuApp("Dhizuku", "Share Device Owner permissions across apps. Enables persistent rootless features like freezing without Shizuku restart.", "Dhizuku", "https://github.com/iamr0s/Dhizuku", "https://github.com/iamr0s/Dhizuku/releases/latest", "System Tools", listOf("Dhizuku", "Shizuku", "ADB")),
            ShizukuApp("TrickyStore", "Advanced Keybox spoofer to bypass Strong Integrity API checks on rooted devices.", "5ec1cff", "https://github.com/5ec1cff/TrickyStore", "https://github.com/5ec1cff/TrickyStore/releases/latest", "Advanced Root", listOf("Root", "Magisk", "System")),
            ShizukuApp("Iconify", "Modern GravityBox alternative. Deep UI customization for QS tiles, brightness slider, and status bar without a custom ROM.", "Dr-Muda", "https://github.com/Dr-Muda/Iconify", "https://github.com/Dr-Muda/Iconify/releases/latest", "Advanced Root", listOf("Root", "LSPatch", "Theming")),
            ShizukuApp("Touch Me Not", "Security module that blocks Power Menu and QS access while the device is locked to prevent thieves from disabling networking.", "Xposed", "https://github.com/Xposed-Modules-Repo/com.drdisagree.touchmenot", "https://github.com/Xposed-Modules-Repo/com.drdisagree.touchmenot/releases/latest", "Security", listOf("LSPatch", "Root", "Security")),
            ShizukuApp("Disable Target API Block", "Bypass Android 14/15 restrictions preventing the installation of legacy apps (abandonware).", "Xposed", "https://github.com/Xposed-Modules-Repo/com.kieronquinn.app.disabletargetapiblock", "https://github.com/Xposed-Modules-Repo/com.kieronquinn.app.disabletargetapiblock/releases/latest", "System Tools", listOf("LSPatch", "Root", "Dev")),
            ShizukuApp("GPS Mover", "System-level location spoofer that simulates live movement with high accuracy without triggering developer mock location detection.", "System", "https://github.com/System-Mods/GPSMover", "https://github.com/System-Mods/GPSMover/releases/latest", "Advanced Root", listOf("Root", "LSPatch", "Dev")),
            ShizukuApp("KernelSU-Next", "The 2026 standard for stealth root. Implements root directly in the Linux Kernel, bypassing userspace detection entirely.", "rifsxd", "https://github.com/rifsxd/KernelSU-Next", "https://github.com/rifsxd/KernelSU-Next/releases/latest", "Advanced Root", listOf("Root", "Kernel", "System")),
            ShizukuApp("APatch", "Next-gen hybrid root solution for Android 16. Hooks kernel memory directly, supporting both APM and KPM modules.", "bmax121", "https://github.com/bmax121/APatch", "https://github.com/bmax121/APatch/releases/latest", "Advanced Root", listOf("Root", "Kernel", "System")),
            ShizukuApp("Install with Options", "Bypass Android 16 Target SDK limits and downgrade apps without losing data using Shizuku.", "MuntashirAkon", "https://github.com/MuntashirAkon/InstallWithOptions", "https://github.com/MuntashirAkon/InstallWithOptions/releases/latest", "System Tools", listOf("Shizuku", "App Management")),
            ShizukuApp("SD Maid SE", "System cleaner optimized for Android 16. Uses Shizuku to clear deep system cache and corpse files.", "darken", "https://github.com/darken/sdmaid-se", "https://github.com/darken/sdmaid-se/releases/latest", "System Tools", listOf("Shizuku", "Maintenance"))
        )
    }
}
