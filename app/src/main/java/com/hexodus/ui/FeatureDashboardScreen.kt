package com.hexodus.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hexodus.services.CapabilityManager
import com.hexodus.services.FeatureFlagsService
import com.hexodus.services.ShizukuBridge
import com.hexodus.ui.components.DeprecationInfo
import com.hexodus.ui.components.FeatureToggleCard
import com.hexodus.utils.PrefsManager
import com.hexodus.utils.RedundancyEngine

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeatureDashboardScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val capabilityManager = remember { CapabilityManager(context) }
    val prefs = remember { PrefsManager.getInstance(context) }

    // State for enter animations
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Quick Actions
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

    val showIncompatible = prefs.showIncompatibleFeatures
    val overrideCompat = prefs.overrideCompatibility
    val showDeprecated = prefs.showDeprecatedTools

    LaunchedEffect(Unit) {
        caps = capabilityManager.detectCapabilities()
    }

    fun applyFlag(key: String, enabled: Boolean) {
        if (!ShizukuBridge.isReady()) {
            Toast.makeText(context, "Shizuku not connected — toggle saved locally", Toast.LENGTH_SHORT).show()
        }
        FeatureFlagsService.toggleFeature(key, enabled)
    }

    @Composable
    fun AnimatedSection(delay: Int, content: @Composable () -> Unit) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(animationSpec = tween(400, delayMillis = delay)) { it / 2 } +
                    fadeIn(animationSpec = tween(400, delayMillis = delay)),
            content = { content() }
        )
    }

    @Composable
    fun FrameworkChip(label: String, isActive: Boolean, icon: ImageVector) {
        Surface(
            color = if (isActive) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.padding(2.dp).clickable {
                Toast.makeText(context, "$label status: ${if(isActive) "Active" else "Inactive"}", Toast.LENGTH_SHORT).show()
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (isActive) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
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
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Redundancy Detected: ${installedConflicts.joinToString()} installed.",
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
                colorIndicator = if (isCompatible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.alpha(if (isCompatible) 1f else 0.65f),
                deprecationInfo = deprecationInfo
            )
        }
    }

    @Composable
    fun SectionLabel(text: String) {
        if (searchQuery.isNotEmpty()) return
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
        )
    }

    @Composable
    fun BentoCategory(title: String, icon: ImageVector, color: Color, isLarge: Boolean = false, onNavigate: () -> Unit) {
        if (searchQuery.isNotEmpty() && !title.contains(searchQuery, ignoreCase = true)) return
        
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(if (isLarge) 1f else 0.485f)
                .height(if (isLarge) 84.dp else 104.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigate()
                    }
                ),
            colors = CardDefaults.elevatedCardColors(containerColor = color.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (isLarge) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                        Surface(
                            color = color.copy(alpha = 0.25f), 
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(icon, null, tint = color, modifier = Modifier.padding(10.dp).size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowForward, null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                    }
                } else {
                    Column(horizontalAlignment = Alignment.Start) {
                        Surface(
                            color = color.copy(alpha = 0.2f), 
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(icon, null, tint = color, modifier = Modifier.padding(6.dp).size(20.dp))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Hero Header with Gradient
        AnimatedSection(0) {
            val heroGradient = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                )
            )
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(heroGradient)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hexodus",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Expressive Design Edition",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                            )
                        }
                        IconButton(
                            onClick = { navController?.navigate(NavRoutes.Settings) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        maxItemsInEachRow = 3
                    ) {
                        FrameworkChip("Shizuku", caps?.isShizukuReady == true, Icons.Default.Bolt)
                        FrameworkChip("Dhizuku", caps?.isDhizukuReady == true, Icons.Default.Shield)
                        FrameworkChip("Vector", caps?.isVectorActive == true, Icons.Default.Hub)
                        FrameworkChip("Root", caps?.isRooted == true, Icons.Default.Terminal)
                        FrameworkChip("Xposed", caps?.isXposedActive == true, Icons.Default.Code)
                    }
                }
            }
        }

        // Search Bar - Floating Style with Focus Animation
        var isSearchFocused by remember { mutableStateOf(false) }
        val searchScale by animateFloatAsState(if (isSearchFocused) 1.02f else 1f, label = "searchScale")
        val searchElevation by animateDpAsState(if (isSearchFocused) 4.dp else 0.dp, label = "searchElevation")

        AnimatedSection(100) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = searchScale
                        scaleY = searchScale
                    }
                    .onFocusChanged { isSearchFocused = it.isFocused },
                placeholder = { Text("Search repo & features...") },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        null, 
                        tint = if (isSearchFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }

        if (searchQuery.isEmpty()) {
            AnimatedSection(200) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionLabel("Discovery Hub")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BentoCategory("Device Mods", Icons.Default.Smartphone, Color(0xFF2196F3)) { navController?.navigate(NavRoutes.Features("Device Specific")) }
                        BentoCategory("Camera Pro", Icons.Default.CameraAlt, Color(0xFFFF5722)) { navController?.navigate(NavRoutes.Features("Camera")) }
                    }
                    BentoCategory("Discovery & Repos", Icons.Default.Explore, MaterialTheme.colorScheme.tertiary, isLarge = true) { navController?.navigate(NavRoutes.AwesomeShizuku) }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BentoCategory("Theming", Icons.Default.ColorLens, Color(0xFF9C27B0)) { navController?.navigate(NavRoutes.Features("Theming")) }
                        BentoCategory("Extensions", Icons.Default.Extension, Color(0xFF4CAF50)) { navController?.navigate(NavRoutes.CustomMods) }
                    }
                    BentoCategory("System Tuning", Icons.Default.Tune, Color(0xFF795548), isLarge = true) { navController?.navigate(NavRoutes.Features("System")) }
                }
            }
        }

        // Quick Actions
        AnimatedSection(300) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Standard Toggles")

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

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
