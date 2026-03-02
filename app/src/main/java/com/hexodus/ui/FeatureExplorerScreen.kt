package com.hexodus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hexodus.services.CapabilityManager
import com.hexodus.services.FeatureFlagsService
import com.hexodus.ui.components.FeatureExplanationCard
import com.hexodus.ui.components.FeatureStatus
import com.hexodus.ui.components.FeatureToggleCard
import com.hexodus.utils.PrefsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureExplorerScreen(
    category: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager.getInstance(context) }
    val capabilityManager = remember { CapabilityManager(context) }
    var caps by remember { mutableStateOf<CapabilityManager.DeviceCapabilities?>(null) }

    LaunchedEffect(Unit) { caps = capabilityManager.detectCapabilities() }

    val features = when (category) {
        "Theming"         -> getThemingFeatures()
        "System"          -> getSystemFeatures()
        "App Management"  -> getAppManagementFeatures()
        "Privacy"         -> getPrivacyFeatures()
        "Network"         -> getNetworkFeatures()
        "Audio"           -> getAudioFeatures()
        "Interaction"     -> getInteractionFeatures()
        "Device Specific" -> getDeviceSpecificFeatures()
        "Camera"          -> getCameraFeatures()
        "NextGen"         -> getNextGenFeatures()
        else              -> emptyList()
    }

    // Initialise toggle state from prefs for features that have a featureKey
    val featureStates = remember {
        mutableStateMapOf<String, Boolean>().also { map ->
            features.forEach { f ->
                f.featureKey?.let { key -> map[key] = prefs.getFeatureEnabled(key) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (features.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No features in this category yet.", style = MaterialTheme.typography.titleMedium)
                    Text("Check back after an update.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(features, key = { it.title }) { feature ->
                val key = feature.featureKey

                if (key != null) {
                    val isEnabled = featureStates[key] ?: false
                    val isCompatible = caps?.let {
                        capabilityManager.isCompatible(feature.requirements, it)
                    } ?: true
                    val overrideCompat = prefs.overrideCompatibility

                    FeatureToggleCard(
                        title = feature.title,
                        description = feature.description,
                        icon = feature.icon,
                        isEnabled = isEnabled,
                        onToggle = { v ->
                            featureStates[key] = v
                            prefs.setFeatureEnabled(key, v)
                            FeatureFlagsService.toggleFeature(key, v)
                        },
                        switchEnabled = isCompatible || overrideCompat,
                        colorIndicator = feature.color,
                        modifier = if (!isCompatible) Modifier.fillMaxWidth() else Modifier.fillMaxWidth()
                    )
                } else {
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
        } // end else (features not empty)
    }
}

data class Feature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val status: FeatureStatus,
    val onClick: (() -> Unit)? = null,
    /** If non-null, feature renders as a live toggle wired to FeatureFlagsService */
    val featureKey: String? = null,
    val requirements: List<String> = emptyList()
)

fun getThemingFeatures(): List<Feature> = listOf(
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
        description = "Forces the floating design language and 3D icons from Android 16 on older SystemUI elements",
        icon = Icons.Default.Layers,
        color = Color(0xFF9C27B0),
        status = FeatureStatus.Available,
        featureKey = "glassmorphism",
        requirements = listOf("Samsung", "Shizuku")
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

fun getSystemFeatures(): List<Feature> = listOf(
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

fun getAppManagementFeatures(): List<Feature> = listOf(
    Feature(
        title = "App Freezer",
        description = "Disable apps without uninstalling — they consume no RAM or battery",
        icon = Icons.Default.HideSource,
        color = Color(0xFF4CAF50),
        status = FeatureStatus.Enabled
    ),
    Feature(
        title = "App Hider",
        description = "Hide apps from the launcher without disabling them",
        icon = Icons.Default.VisibilityOff,
        color = Color(0xFF4CAF50),
        status = FeatureStatus.Enabled
    ),
    Feature(
        title = "Batch Operations",
        description = "Freeze, hide, or force-stop multiple apps at once",
        icon = Icons.Default.SelectAll,
        color = Color(0xFF4CAF50),
        status = FeatureStatus.Available
    ),
    Feature(
        title = "App Info Viewer",
        description = "Get detailed information about installed apps via Shizuku",
        icon = Icons.Default.Info,
        color = Color(0xFF4CAF50),
        status = FeatureStatus.Enabled
    )
)

fun getPrivacyFeatures(): List<Feature> = listOf(
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
        description = "Scan for apps with excessive permission usage",
        icon = Icons.Default.Search,
        color = Color(0xFFF44336),
        status = FeatureStatus.Available
    ),
    Feature(
        title = "Sensitive Notification Redaction",
        description = "AI-hides OTPs and sensitive text on the lock screen",
        icon = Icons.Default.Security,
        color = Color(0xFFF44336),
        status = FeatureStatus.Available,
        featureKey = "notification_redaction",
        requirements = listOf("Shizuku")
    )
)

fun getNetworkFeatures(): List<Feature> = listOf(
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
        description = "Scan and monitor network activity per-app",
        icon = Icons.Default.NetworkCheck,
        color = Color(0xFF2196F3),
        status = FeatureStatus.Available
    ),
    Feature(
        title = "Connection Control",
        description = "Allow/block specific network types per app (WiFi/mobile)",
        icon = Icons.Default.Wifi,
        color = Color(0xFF2196F3),
        status = FeatureStatus.Enabled
    )
)

fun getAudioFeatures(): List<Feature> = listOf(
    Feature(
        title = "Equalizer Control",
        description = "Adjust frequency bands for fine-tuned sound",
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
        description = "Apply various audio processing effects system-wide",
        icon = Icons.Default.Audiotrack,
        color = Color(0xFF3F51B5),
        status = FeatureStatus.Available
    ),
    Feature(
        title = "Session Manager",
        description = "Control audio routing for specific applications",
        icon = Icons.Default.PlayCircle,
        color = Color(0xFF3F51B5),
        status = FeatureStatus.Available
    )
)

fun getInteractionFeatures(): List<Feature> = listOf(
    Feature(
        title = "Gesture Manager",
        description = "Register custom gestures with associated actions",
        icon = Icons.Default.TouchApp,
        color = Color(0xFF9E9E9E),
        status = FeatureStatus.Available
    ),
    Feature(
        title = "Back Gesture",
        description = "Double/triple tap on back of device triggers custom actions",
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
        description = "Assign different actions to various swipe/tap patterns",
        icon = Icons.Default.Gesture,
        color = Color(0xFF9E9E9E),
        status = FeatureStatus.Available
    )
)

fun getDeviceSpecificFeatures(): List<Feature> = listOf(
    Feature(
        title = "Now Brief",
        description = "Unlocks the hidden daily summary widget on lock screen (S25 exclusive)",
        icon = Icons.Default.AutoAwesome,
        color = Color(0xFF00BCD4),
        status = FeatureStatus.Available,
        featureKey = "now_brief",
        requirements = listOf("Samsung", "S22Ultra")
    ),
    Feature(
        title = "Advanced Battery Health",
        description = "View exact battery cycle count and estimated health percentage",
        icon = Icons.Default.BatteryChargingFull,
        color = Color(0xFF00BCD4),
        status = FeatureStatus.Available,
        featureKey = "battery_stats",
        requirements = listOf("Shizuku")
    ),
    Feature(
        title = "Scoped Storage Bypass",
        description = "Enables access to Android/data and obb folders for supported file managers",
        icon = Icons.Default.FolderSpecial,
        color = Color(0xFF00BCD4),
        status = FeatureStatus.Available
    ),
    Feature(
        title = "Per-App Dark Mode",
        description = "Force dark mode on apps that don't support it natively",
        icon = Icons.Default.DarkMode,
        color = Color(0xFF00BCD4),
        status = FeatureStatus.Available
    ),
    Feature(
        title = "Light Performance Mode",
        description = "Prioritise battery and cooling over speed (S23 feature backported)",
        icon = Icons.Default.Speed,
        color = Color(0xFF00BCD4),
        status = FeatureStatus.Available
    ),
    Feature(
        title = "Enhanced Processing",
        description = "Forces CPU to higher performance states for better responsiveness",
        icon = Icons.Default.Bolt,
        color = Color(0xFF00BCD4),
        status = FeatureStatus.Available,
        featureKey = "enhanced_processing",
        requirements = listOf("Samsung", "Shizuku")
    ),
    Feature(
        title = "Vertical App Drawer",
        description = "Enables hidden Home Up vertical scroll for the app drawer (One UI 7.0 style)",
        icon = Icons.Default.ViewStream,
        color = Color(0xFF00BCD4),
        status = FeatureStatus.Available,
        featureKey = "vertical_drawer",
        requirements = listOf("Samsung", "Shizuku")
    ),
    Feature(
        title = "Circle to Search",
        description = "Backports AI-powered Circle to Search (requires recent Google App)",
        icon = Icons.Default.Search,
        color = Color(0xFF00BCD4),
        status = FeatureStatus.Available,
        featureKey = "circle_to_search",
        requirements = listOf("Samsung", "Shizuku")
    ),
    Feature(
        title = "Now Bar",
        description = "Enables the persistent pill-shaped status bar notification for active tasks",
        icon = Icons.Default.ViewHeadline,
        color = Color(0xFF00BCD4),
        status = FeatureStatus.Available,
        featureKey = "now_bar",
        requirements = listOf("Samsung", "Shizuku")
    ),
    Feature(
        title = "Priority Notifications",
        description = "Backports Galaxy AI glow effect for important alerts (One UI 8.5)",
        icon = Icons.Default.NotificationsActive,
        color = Color(0xFF00BCD4),
        status = FeatureStatus.Available,
        featureKey = "priority_notifs",
        requirements = listOf("Samsung", "Shizuku")
    ),
    Feature(
        title = "Vertical Quick Panel",
        description = "Switches brightness and volume sliders to vertical in Quick Settings (One UI 8.5)",
        icon = Icons.Default.ViewQuilt,
        color = Color(0xFF00BCD4),
        status = FeatureStatus.Available,
        featureKey = "vertical_qs",
        requirements = listOf("Samsung", "Shizuku")
    ),
    Feature(
        title = "Magic Editor (Pixelify)",
        description = "Unlocks Pixel-exclusive AI editing in Google Photos via LSPatch",
        icon = Icons.Default.AutoFixHigh,
        color = Color(0xFF00BCD4),
        status = FeatureStatus.Available
    )
)

fun getCameraFeatures(): List<Feature> = listOf(
    Feature(
        title = "GCam (LMC 8.4)",
        description = "Optimised Google Camera port for S22 Ultra 108MP sensor with Leica colours and better dynamic range",
        icon = Icons.Default.Camera,
        color = Color(0xFFFF5722),
        status = FeatureStatus.Available
    ),
    Feature(
        title = "Camera Assistant",
        description = "Advanced Samsung settings: disable auto-lens switching, faster shutter, clean HDMI",
        icon = Icons.Default.SettingsSuggest,
        color = Color(0xFFFF5722),
        status = FeatureStatus.Available
    ),
    Feature(
        title = "Expert RAW Mods",
        description = "Unlock 50MP/108MP RAW output and community custom processing libraries",
        icon = Icons.Default.PhotoCamera,
        color = Color(0xFFFF5722),
        status = FeatureStatus.Available
    ),
    Feature(
        title = "Sketch to Image",
        description = "AI-powered Drawing Assist from One UI 6.1.1 — turns sketches into realistic objects",
        icon = Icons.Default.Edit,
        color = Color(0xFFFF5722),
        status = FeatureStatus.Available
    )
)

fun getNextGenFeatures(): List<Feature> = listOf(
    Feature(
        title = "Notification Cooldown",
        description = "Automatically lowers volume for apps that send rapid-fire notifications (Android 16)",
        icon = Icons.Default.NotificationsPaused,
        color = Color(0xFFFFC107),
        status = FeatureStatus.Available,
        featureKey = "notification_cooldown",
        requirements = listOf("Shizuku")
    ),
    Feature(
        title = "Desktop Windowing",
        description = "Unlocks Android 16 resizable windowing, previously restricted to foldables and tablets",
        icon = Icons.Default.DesktopWindows,
        color = Color(0xFFFFC107),
        status = FeatureStatus.Available,
        featureKey = "desktop_windowing",
        requirements = listOf("Shizuku")
    ),
    Feature(
        title = "Screen-Off FOD",
        description = "Allows the ultrasonic fingerprint scanner to unlock without waking the display",
        icon = Icons.Default.Fingerprint,
        color = Color(0xFFFFC107),
        status = FeatureStatus.Available,
        featureKey = "screen_off_fod",
        requirements = listOf("Shizuku")
    ),
    Feature(
        title = "Sensitive Notification Redaction",
        description = "AI automatically hides OTPs and sensitive details on the lock screen",
        icon = Icons.Default.Security,
        color = Color(0xFFFFC107),
        status = FeatureStatus.Available,
        featureKey = "notification_redaction",
        requirements = listOf("Shizuku")
    ),
    Feature(
        title = "Vertical Quick Panel",
        description = "Backports S26 vertical brightness and volume sliders to Quick Settings",
        icon = Icons.Default.ViewQuilt,
        color = Color(0xFFE91E63),
        status = FeatureStatus.Available,
        featureKey = "vertical_qs",
        requirements = listOf("Samsung", "Shizuku")
    ),
    Feature(
        title = "Priority Notifications",
        description = "Galaxy AI glow effect highlighting critical alerts from One UI 8.5",
        icon = Icons.Default.NotificationsActive,
        color = Color(0xFFE91E63),
        status = FeatureStatus.Available,
        featureKey = "priority_notifs",
        requirements = listOf("Samsung", "Shizuku")
    ),
    Feature(
        title = "3D Glassmorphism",
        description = "Floating translucent 3D design language from Android 16 / One UI 8.5",
        icon = Icons.Default.Layers,
        color = Color(0xFFE91E63),
        status = FeatureStatus.Available,
        featureKey = "glassmorphism",
        requirements = listOf("Samsung", "Shizuku")
    )
)
