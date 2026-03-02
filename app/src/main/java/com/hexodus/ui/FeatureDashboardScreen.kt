package com.hexodus.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.widget.Toast
import com.hexodus.services.CapabilityManager
import com.hexodus.services.FeatureFlagsService
import com.hexodus.services.ShizukuBridge
import com.hexodus.utils.PrefsManager
import com.hexodus.utils.RedundancyEngine
import com.hexodus.ui.components.FeatureToggleCard
import com.hexodus.ui.components.DeprecationInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureDashboardScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val capabilityManager = remember { CapabilityManager(context) }
    val prefs = remember { PrefsManager.getInstance(context) }

    // Quick Actions — initial state loaded from prefs
    var circleToSearchEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("circle_to_search")) }
    var verticalDrawerEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("vertical_drawer")) }
    var nowBriefEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("now_brief")) }
    var batteryStatsEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("battery_stats")) }
    var enhancedProcessingEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("enhanced_processing")) }

    // Android 16 features
    var notificationCooldownEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("notification_cooldown")) }
    var desktopModeEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("desktop_windowing")) }
    var screenOffFodEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("screen_off_fod")) }
    var verticalQsEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("vertical_qs")) }
    var priorityNotifsEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("priority_notifs")) }
    var glassmorphismEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("glassmorphism")) }

    // Deprecated tools
    var legacyThemeEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("legacy_theme")) }
    var substratumEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("substratum")) }
    var gravityBoxEnabled by remember { mutableStateOf(prefs.getFeatureEnabled("gravitybox")) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var caps by remember { mutableStateOf<CapabilityManager.DeviceCapabilities?>(null) }

    // Read visibility prefs
    val showIncompatible = prefs.showIncompatibleFeatures
    val overrideCompat = prefs.overrideCompatibility
    val showDeprecated = prefs.showDeprecatedTools

    LaunchedEffect(Unit) {
        caps = capabilityManager.detectCapabilities()
    }

    // Wrapper that shows a Toast if Shizuku isn't available before applying the flag
    fun applyFlag(key: String, enabled: Boolean) {
        if (!ShizukuBridge.isReady()) {
            Toast.makeText(context, "Shizuku not connected — toggle saved locally", Toast.LENGTH_SHORT).show()
        }
        FeatureFlagsService.toggleFeature(key, enabled)
    }

    @Composable
    fun FrameworkChip(label: String, isActive: Boolean, icon: ImageVector) {
        Surface(
            color = if (isActive) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = if (isActive) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    fun FeatureCard(
        title: String,
        description: String,
        icon: ImageVector,
        isEnabled: Boolean,
        onToggle: (Boolean) -> Unit,
        requirements: List<String>,
        deprecationInfo: DeprecationInfo? = null
    ) {
        if (searchQuery.isNotEmpty() &&
            !title.contains(searchQuery, ignoreCase = true) &&
            !description.contains(searchQuery, ignoreCase = true)
        ) return

        val isCompatible = caps?.let { capabilityManager.isCompatible(requirements, it) } ?: true
        if (!isCompatible && !showIncompatible) return

        val conflictingApps = RedundancyEngine.getConflictingApps(title)
        val installedConflicts = remember(conflictingApps) {
            conflictingApps.filter { pkg ->
                try { context.packageManager.getPackageInfo(pkg, 0); true }
                catch (e: Exception) { false }
            }
        }

        val switchEnabled = isCompatible || overrideCompat

        Column {
            if (installedConflicts.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Conflict: ${installedConflicts.joinToString()} detected — Hexodus handles this natively.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            FeatureToggleCard(
                title = title,
                description = description,
                icon = icon,
                isEnabled = isEnabled,
                onToggle = onToggle,
                switchEnabled = switchEnabled,
                colorIndicator = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(if (isCompatible) 1f else 0.55f),
                deprecationInfo = deprecationInfo
            )
        }
    }

    @Composable
    fun SectionLabel(text: String) {
        if (searchQuery.isNotEmpty()) return
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
        )
    }

    @Composable
    fun CategoryButton(title: String, icon: ImageVector, onNavigate: () -> Unit) {
        if (searchQuery.isNotEmpty() && !title.contains(searchQuery, ignoreCase = true)) return
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = "Open $title") { onNavigate() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hexodus",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    IconButton(onClick = { navController?.navigate(NavRoutes.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FrameworkChip("Shizuku", caps?.isShizukuReady == true, Icons.Default.Bolt)
                    FrameworkChip("Dhizuku", caps?.isDhizukuReady == true, Icons.Default.Shield)
                    FrameworkChip("Vector", caps?.isVectorActive == true, Icons.Default.Hub)
                    FrameworkChip("Root", caps?.isRooted == true, Icons.Default.Terminal)
                    FrameworkChip("Xposed", caps?.isXposedActive == true, Icons.Default.Code)
                }
            }
        }

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search features...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            )
        )

        // Quick Actions
        SectionLabel("Quick Actions")

        FeatureCard(
            title = "Circle to Search",
            description = "AI search by circling anything on screen",
            icon = Icons.Default.Search,
            isEnabled = circleToSearchEnabled,
            onToggle = { circleToSearchEnabled = it; prefs.setFeatureEnabled("circle_to_search", it); applyFlag("circle_to_search", it) },
            requirements = listOf("Samsung", "Shizuku")
        )
        FeatureCard(
            title = "Vertical App Drawer",
            description = "Switch to vertical scroll for the app drawer",
            icon = Icons.Default.ViewStream,
            isEnabled = verticalDrawerEnabled,
            onToggle = { verticalDrawerEnabled = it; prefs.setFeatureEnabled("vertical_drawer", it); applyFlag("vertical_drawer", it) },
            requirements = listOf("Samsung", "Shizuku")
        )
        FeatureCard(
            title = "Now Brief",
            description = "Hidden smart suggestions summary (S25 exclusive)",
            icon = Icons.Default.AutoAwesome,
            isEnabled = nowBriefEnabled,
            onToggle = { nowBriefEnabled = it; prefs.setFeatureEnabled("now_brief", it); applyFlag("now_brief", it) },
            requirements = listOf("S22Ultra", "Samsung")
        )
        FeatureCard(
            title = "Advanced Battery Stats",
            description = "View real cycle count and actual health",
            icon = Icons.Default.BatteryChargingFull,
            isEnabled = batteryStatsEnabled,
            onToggle = { batteryStatsEnabled = it; prefs.setFeatureEnabled("battery_stats", it); applyFlag("battery_stats", it) },
            requirements = listOf("Shizuku")
        )
        FeatureCard(
            title = "Enhanced Processing",
            description = "Boost CPU responsiveness for smoother UI",
            icon = Icons.Default.Bolt,
            isEnabled = enhancedProcessingEnabled,
            onToggle = { enhancedProcessingEnabled = it; prefs.setFeatureEnabled("enhanced_processing", it); applyFlag("enhanced_processing", it) },
            requirements = listOf("Samsung", "Shizuku")
        )

        // Android 16
        SectionLabel("Android 16 / Next-Gen")

        FeatureCard(
            title = "Notification Cooldown",
            description = "Android 16 Baklava — quiet rapid-fire alerts",
            icon = Icons.Default.NotificationsPaused,
            isEnabled = notificationCooldownEnabled,
            onToggle = { notificationCooldownEnabled = it; prefs.setFeatureEnabled("notification_cooldown", it); applyFlag("notification_cooldown", it) },
            requirements = listOf("Shizuku")
        )
        FeatureCard(
            title = "Desktop Windowing",
            description = "Android 16 freeform resizable desktop mode",
            icon = Icons.Default.DesktopWindows,
            isEnabled = desktopModeEnabled,
            onToggle = { desktopModeEnabled = it; prefs.setFeatureEnabled("desktop_windowing", it); applyFlag("desktop_windowing", it) },
            requirements = listOf("Shizuku")
        )
        FeatureCard(
            title = "Screen-Off FOD",
            description = "Unlock without waking display (ultrasonic sensor required)",
            icon = Icons.Default.Fingerprint,
            isEnabled = screenOffFodEnabled,
            onToggle = { screenOffFodEnabled = it; prefs.setFeatureEnabled("screen_off_fod", it); applyFlag("screen_off_fod", it) },
            requirements = listOf("Shizuku")
        )
        FeatureCard(
            title = "Vertical Quick Panel",
            description = "One UI 8.5 vertical volume/brightness sliders",
            icon = Icons.Default.ViewQuilt,
            isEnabled = verticalQsEnabled,
            onToggle = { verticalQsEnabled = it; prefs.setFeatureEnabled("vertical_qs", it); applyFlag("vertical_qs", it) },
            requirements = listOf("Samsung", "Shizuku")
        )
        FeatureCard(
            title = "Priority AI Notifications",
            description = "One UI 8.5 smart glow effect for critical alerts",
            icon = Icons.Default.NotificationsActive,
            isEnabled = priorityNotifsEnabled,
            onToggle = { priorityNotifsEnabled = it; prefs.setFeatureEnabled("priority_notifs", it); applyFlag("priority_notifs", it) },
            requirements = listOf("Samsung", "Shizuku")
        )
        FeatureCard(
            title = "3D Glassmorphism Icons",
            description = "One UI 8.5 floating translucent icon design",
            icon = Icons.Default.Layers,
            isEnabled = glassmorphismEnabled,
            onToggle = { glassmorphismEnabled = it; prefs.setFeatureEnabled("glassmorphism", it); applyFlag("glassmorphism", it) },
            requirements = listOf("Samsung", "Shizuku")
        )

        // Deprecated
        if (showDeprecated && searchQuery.isEmpty()) {
            SectionLabel("Deprecated Tools")
        }
        if (showDeprecated) {
            FeatureCard(
                title = "Legacy Theme Engine",
                description = "Old theme system via Xposed framework",
                icon = Icons.Default.Palette,
                isEnabled = legacyThemeEnabled,
                onToggle = { legacyThemeEnabled = it },
                requirements = listOf("Root"),
                deprecationInfo = DeprecationInfo(
                    message = "Use Hexodus Theme Engine v2 instead",
                    replacement = "Hexodus Theme Engine v2",
                    deprecatedSince = "2025.1",
                    removeInVersion = "2026.2"
                )
            )
            FeatureCard(
                title = "Substratum Overlay Manager",
                description = "Classic overlay management system",
                icon = Icons.Default.Layers,
                isEnabled = substratumEnabled,
                onToggle = { substratumEnabled = it },
                requirements = listOf("Root"),
                deprecationInfo = DeprecationInfo(
                    message = "No longer maintained",
                    replacement = "RRO Overlay System",
                    deprecatedSince = "2024.3",
                    removeInVersion = "2026.1"
                )
            )
            FeatureCard(
                title = "GravityBox Tweaks",
                description = "System tweaks via GravityBox module",
                icon = Icons.Default.Tune,
                isEnabled = gravityBoxEnabled,
                onToggle = { gravityBoxEnabled = it },
                requirements = listOf("Xposed"),
                deprecationInfo = DeprecationInfo(
                    message = "Use native implementation instead",
                    replacement = "System Tuner (Native)",
                    deprecatedSince = "2025.2"
                )
            )
        }

        // Explore
        SectionLabel("Explore")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = "Browse Awesome Shizuku apps") { navController?.navigate(NavRoutes.AwesomeShizuku) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Awesome Shizuku & Repos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }

        CategoryButton("Device Specific", Icons.Default.Smartphone) { navController?.navigate(NavRoutes.Features("Device Specific")) }
        CategoryButton("Camera Enhancements", Icons.Default.CameraAlt) { navController?.navigate(NavRoutes.Features("Camera")) }
        CategoryButton("Custom Mod Extensions", Icons.Default.Extension) { navController?.navigate(NavRoutes.CustomMods) }
        CategoryButton("Theming & Customization", Icons.Default.ColorLens) { navController?.navigate(NavRoutes.Features("Theming")) }
        CategoryButton("System Integration", Icons.Default.Settings) { navController?.navigate(NavRoutes.Features("System")) }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
