package com.hexodus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hexodus.ui.components.FeatureExplanationCard
import com.hexodus.ui.components.FeatureStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureExplorerScreen(
    category: String,
    onBackClick: () -> Unit
) {
    val features = when (category) {
        "Theming" -> getThemingFeatures()
        "System" -> getSystemFeatures()
        "App Management" -> getAppManagementFeatures()
        "Privacy" -> getPrivacyFeatures()
        "Network" -> getNetworkFeatures()
        "Audio" -> getAudioFeatures()
        "Interaction" -> getInteractionFeatures()
        "Device Specific" -> getDeviceSpecificFeatures()
        "Camera" -> getCameraFeatures()
        "NextGen" -> getNextGenFeatures()
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.headlineSmall // M3E typography standard
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back to dashboard", // Accessibility description
                            modifier = Modifier.size(24.dp) // Proper icon size
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(features.size) { index ->
                val feature = features[index]
                FeatureExplanationCard(
                    title = feature.title,
                    description = feature.description,
                    icon = feature.icon,
                    colorIndicator = feature.color,
                    status = feature.status,
                    onClick = feature.onClick
                )
            }
        }
    }
}

data class Feature(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val status: FeatureStatus,
    val onClick: (() -> Unit)? = null
)

fun getThemingFeatures(): List<Feature> {
    return listOf(
        Feature(
            title = "Hex-to-Overlay Compiler",
            description = "Converts hex color codes into system-compatible overlay APKs",
            icon = Icons.Default.Colorize,
            color = Color(0xFF6200EE),
            status = FeatureStatus.Enabled
        ),
        Feature(
            title = "Material You Override",
            description = "Bypasses One UI 8's aggressive Monet/Material You enforcement",
            icon = Icons.Default.Style,
            color = Color(0xFF03DAC6),
            status = FeatureStatus.Enabled
        ),
        Feature(
            title = "3D Glassmorphism (One UI 8.5)",
            description = "Forces the floating design language and 3D icons from Android 16 on older system UI elements",
            icon = Icons.Default.Layers,
            color = Color(0xFF9C27B0),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "High Contrast Injection",
            description = "Exploits Samsung's High Contrast accessibility themes to bypass standard theme checks",
            icon = Icons.Default.Visibility,
            color = Color(0xFFFF9800),
            status = FeatureStatus.Enabled
        ),
        Feature(
            title = "Dynamic Color Generator",
            description = "Creates custom color schemes that integrate seamlessly with the system",
            icon = Icons.Default.Gradient,
            color = Color(0xFF9C27B0),
            status = FeatureStatus.Enabled
        )
    )
}

fun getSystemFeatures(): List<Feature> {
    return listOf(
        Feature(
            title = "Shizuku Bridge",
            description = "Leverages Shizuku for system-level operations without root",
            icon = Icons.Default.Link,
            color = Color(0xFF795548),
            status = FeatureStatus.Enabled
        ),
        Feature(
            title = "Vector Framework Hub",
            description = "Kotlin-based successor to LSPosed for Android 16 hooking and system modification",
            icon = Icons.Default.Hub,
            color = Color(0xFF9C27B0),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "System UI Tuner",
            description = "Access and modify hidden system settings",
            icon = Icons.Default.Tune,
            color = Color(0xFF9C27B0),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Overlay Manager",
            description = "Advanced overlay activation/deactivation through trusted shell processes",
            icon = Icons.Default.ViewCompact,
            color = Color(0xFF9C27B0),
            status = FeatureStatus.Enabled
        ),
        Feature(
            title = "Privileged Command Shell",
            description = "Execute advanced scripts with Shizuku or Vector system-level privileges",
            icon = Icons.Default.Terminal,
            color = Color(0xFF9C27B0),
            status = FeatureStatus.Available
        )
    )
}

fun getAppManagementFeatures(): List<Feature> {
    return listOf(
        Feature(
            title = "App Freezer",
            description = "Freeze apps without uninstalling them",
            icon = Icons.Default.HideSource,
            color = Color(0xFF4CAF50),
            status = FeatureStatus.Enabled
        ),
        Feature(
            title = "App Hider",
            description = "Hide apps from the launcher",
            icon = Icons.Default.VisibilityOff,
            color = Color(0xFF4CAF50),
            status = FeatureStatus.Enabled
        ),
        Feature(
            title = "Batch Operations",
            description = "Perform actions on multiple apps simultaneously",
            icon = Icons.Default.SelectAll,
            color = Color(0xFF4CAF50),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "App Info Viewer",
            description = "Get detailed information about installed apps",
            icon = Icons.Default.Info,
            color = Color(0xFF4CAF50),
            status = FeatureStatus.Enabled
        )
    )
}

fun getPrivacyFeatures(): List<Feature> {
    return listOf(
        Feature(
            title = "App Locker",
            description = "Secure apps with PIN or biometric authentication",
            icon = Icons.Default.Lock,
            color = Color(0xFFF44336),
            status = FeatureStatus.Enabled
        ),
        Feature(
            title = "File Hider",
            description = "Hide sensitive files from other apps",
            icon = Icons.Default.FolderShared,
            color = Color(0xFFF44336),
            status = FeatureStatus.Enabled
        ),
        Feature(
            title = "Privacy Scanner",
            description = "Scan for potential privacy issues",
            icon = Icons.Default.Search,
            color = Color(0xFFF44336),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Context-Aware Privacy",
            description = "Manage privacy based on device lock state",
            icon = Icons.Default.LockClock,
            color = Color(0xFFF44336),
            status = FeatureStatus.Available
        )
    )
}

fun getNetworkFeatures(): List<Feature> {
    return listOf(
        Feature(
            title = "App Firewall",
            description = "Block network access for specific applications",
            icon = Icons.Default.Shield,
            color = Color(0xFF2196F3),
            status = FeatureStatus.Enabled
        ),
        Feature(
            title = "Custom Rules",
            description = "Create custom firewall rules",
            icon = Icons.Default.Rule,
            color = Color(0xFF2196F3),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Network Monitor",
            description = "Scan and monitor network activity",
            icon = Icons.Default.NetworkCheck,
            color = Color(0xFF2196F3),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Connection Control",
            description = "Allow/block specific network types (WiFi/mobile)",
            icon = Icons.Default.Wifi,
            color = Color(0xFF2196F3),
            status = FeatureStatus.Enabled
        )
    )
}

fun getAudioFeatures(): List<Feature> {
    return listOf(
        Feature(
            title = "Equalizer Control",
            description = "Adjust various frequency bands for fine-tuned sound",
            icon = Icons.Default.Equalizer,
            color = Color(0xFF3F51B5),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Bass Boost",
            description = "Enhance bass frequencies for richer audio",
            icon = Icons.Default.VolumeUp,
            color = Color(0xFF3F51B5),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Audio Effects",
            description = "Apply various audio processing effects",
            icon = Icons.Default.Audiotrack,
            color = Color(0xFF3F51B5),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Session Manager",
            description = "Control audio for specific applications",
            icon = Icons.Default.PlayCircle,
            color = Color(0xFF3F51B5),
            status = FeatureStatus.Available
        )
    )
}

fun getInteractionFeatures(): List<Feature> {
    return listOf(
        Feature(
            title = "Gesture Manager",
            description = "Register custom gestures with associated actions",
            icon = Icons.Default.TouchApp,
            color = Color(0xFF9E9E9E),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Back Gesture",
            description = "Double/triple tap on back of device actions",
            icon = Icons.Default.TouchApp,
            color = Color(0xFF9E9E9E),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Gesture Actions",
            description = "Launch apps, control media, adjust volume via gestures",
            icon = Icons.Default.SettingsRemote,
            color = Color(0xFF9E9E9E),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Customizable Gestures",
            description = "Assign different actions to various gestures",
            icon = Icons.Default.Gesture,
            color = Color(0xFF9E9E9E),
            status = FeatureStatus.Available
        )
    )
}

fun getDeviceSpecificFeatures(): List<Feature> {
    return listOf(
        Feature(
            title = "Now Brief (S25 Exclusive)",
            description = "Unlocks the hidden daily summary widget on lock screen (via smart suggestions developer mode)",
            icon = Icons.Default.AutoAwesome,
            color = Color(0xFF00BCD4),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Advanced Battery Health",
            description = "View exact battery cycle count and estimated health percentage",
            icon = Icons.Default.BatteryChargingFull,
            color = Color(0xFF00BCD4),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Scoped Storage Bypass",
            description = "Enables access to Android/data and Android/obb folders for supported file managers",
            icon = Icons.Default.FolderSpecial,
            color = Color(0xFF00BCD4),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Per-App Dark Mode (DarQ)",
            description = "Force dark mode on specific apps that don't support it natively",
            icon = Icons.Default.DarkMode,
            color = Color(0xFF00BCD4),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Light Performance Mode",
            description = "Prioritize battery life and cooling over raw speed (S23 feature backported)",
            icon = Icons.Default.Speed,
            color = Color(0xFF00BCD4),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Enhanced Processing",
            description = "Forces CPU to jump to higher performance states more aggressively for better responsiveness",
            icon = Icons.Default.Bolt,
            color = Color(0xFF00BCD4),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Vertical App Drawer",
            description = "Enables the hidden Home Up vertical scroll for the app drawer (One UI 7.0 style)",
            icon = Icons.Default.ViewStream,
            color = Color(0xFF00BCD4),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Circle to Search",
            description = "Backports the AI-powered 'Circle to Search' capability (requires recent Google App)",
            icon = Icons.Default.Search,
            color = Color(0xFF00BCD4),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Now Bar (One UI 7.0+)",
            description = "Enables the persistent pill-shaped status bar notification for active tasks",
            icon = Icons.Default.ViewHeadline,
            color = Color(0xFF00BCD4),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Priority Notifications (One UI 8.5)",
            description = "Backports the Galaxy AI glow effect that highlights important alerts at the top of the shade",
            icon = Icons.Default.NotificationsActive,
            color = Color(0xFF00BCD4),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Vertical Quick Panel (One UI 8.5)",
            description = "Switches the brightness and volume sliders to vertical orientation in the Quick Settings",
            icon = Icons.Default.ViewQuilt,
            color = Color(0xFF00BCD4),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Magic Editor (Pixelify)",
            description = "Unlocks Pixel-exclusive AI editing (Reimagine, Magic Eraser) in Google Photos via LSPatch",
            icon = Icons.Default.AutoFixHigh,
            color = Color(0xFF00BCD4),
            status = FeatureStatus.Available
        )
    )
}

fun getCameraFeatures(): List<Feature> {
    return listOf(
        Feature(
            title = "GCam (LMC 8.4)",
            description = "Optimized Google Camera port for S22 Ultra 108MP sensor. Includes Leica colors and better dynamic range.",
            icon = Icons.Default.Camera,
            color = Color(0xFFFF5722),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Camera Assistant",
            description = "Advanced Samsung settings: disable auto-lens switching, faster shutter, and clean HDMI.",
            icon = Icons.Default.SettingsSuggest,
            color = Color(0xFFFF5722),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Expert RAW Mods",
            description = "Unlock 50MP/108MP RAW output and community custom processing libraries (requires root for libs).",
            icon = Icons.Default.PhotoCamera,
            color = Color(0xFFFF5722),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Sketch to Image",
            description = "AI-powered Drawing Assist from One UI 6.1.1. Turns simple sketches into realistic objects.",
            icon = Icons.Default.Edit,
            color = Color(0xFFFF5722),
            status = FeatureStatus.Available
        )
    )
}

fun getNextGenFeatures(): List<Feature> {
    return listOf(
        Feature(
            title = "Notification Cooldown (Android 16)",
            description = "Automatically lowers volume for apps that send rapid-fire notifications.",
            icon = Icons.Default.NotificationsPaused,
            color = Color(0xFFFFC107),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Freeform Desktop Windowing",
            description = "Unlocks Android 16's resizable windowing framework, previously restricted to foldables and tablets.",
            icon = Icons.Default.DesktopWindows,
            color = Color(0xFFFFC107),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Screen-Off FOD",
            description = "Allows the ultrasonic fingerprint scanner to unlock the device without waking the screen first.",
            icon = Icons.Default.Fingerprint,
            color = Color(0xFFFFC107),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Sensitive Notification Redaction",
            description = "Uses AI to automatically hide OTPs or sensitive details on the lock screen.",
            icon = Icons.Default.Security,
            color = Color(0xFFFFC107),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Vertical Quick Panel (One UI 8.5)",
            description = "Backports the S26's vertical brightness and volume sliders to the Quick Settings.",
            icon = Icons.Default.ViewQuilt,
            color = Color(0xFFE91E63),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "Priority Notifications",
            description = "Galaxy AI glow effect highlighting critical alerts, pulled from One UI 8.5.",
            icon = Icons.Default.NotificationsActive,
            color = Color(0xFFE91E63),
            status = FeatureStatus.Available
        ),
        Feature(
            title = "3D Glassmorphism",
            description = "Forces the floating design language and translucent 3D elements of Android 16.",
            icon = Icons.Default.Layers,
            color = Color(0xFFE91E63),
            status = FeatureStatus.Available
        )
    )
}