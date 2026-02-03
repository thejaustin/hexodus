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
            title = "Immersive Mode Toggle",
            description = "Enable/disable immersive mode programmatically",
            icon = Icons.Default.Fullscreen,
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