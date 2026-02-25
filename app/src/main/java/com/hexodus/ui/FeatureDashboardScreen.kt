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
import com.hexodus.services.CapabilityManager
import com.hexodus.utils.RedundancyEngine
import com.hexodus.ui.components.FeatureToggleCard

import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureDashboardScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val capabilityManager = remember { CapabilityManager(context) }
    
    // State variables for all features
    var themingEnabled by remember { mutableStateOf(true) }
    var systemTunerEnabled by remember { mutableStateOf(false) }
    var appThemerEnabled by remember { mutableStateOf(false) }
    var gestureManagerEnabled by remember { mutableStateOf(false) }
    var mediaNotificationEnabled by remember { mutableStateOf(false) }
    var audioManagerEnabled by remember { mutableStateOf(false) }
    var appManagerEnabled by remember { mutableStateOf(false) }
    var privacySecurityEnabled by remember { mutableStateOf(false) }
    var networkFirewallEnabled by remember { mutableStateOf(false) }
    var powerManagerEnabled by remember { mutableStateOf(false) }

    // 2026 Experimental Features
    var notificationCooldownEnabled by remember { mutableStateOf(false) }
    var desktopModeEnabled by remember { mutableStateOf(false) }
    var screenOffFodEnabled by remember { mutableStateOf(false) }
    var verticalQsEnabled by remember { mutableStateOf(false) }
    var priorityNotifsEnabled by remember { mutableStateOf(false) }
    var glassmorphismEnabled by remember { mutableStateOf(false) }

    var showAllFeatures by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var caps by remember { mutableStateOf<CapabilityManager.Capabilities?>(null) }

    // Check capabilities
    LaunchedEffect(Unit) {
        caps = capabilityManager.detectCapabilities()
    }

    @Composable
    fun FrameworkChip(label: String, isActive: Boolean, icon: ImageVector) {
        Surface(
            color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
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
        requirements: List<String>
    ) {
        // Search filtering
        if (searchQuery.isNotEmpty() && !title.contains(searchQuery, ignoreCase = true) && !description.contains(searchQuery, ignoreCase = true)) {
            return
        }

        val isCompatible = caps?.let { capabilityManager.isCompatible(requirements, it) } ?: true
        val conflictingApps = RedundancyEngine.getConflictingApps(title)
        val installedConflicts = remember(conflictingApps) {
            conflictingApps.filter { appName ->
                try {
                    context.packageManager.getPackageInfo(appName, 0)
                    true
                } catch (e: Exception) {
                    false 
                }
            }
        }

        if (isCompatible || showAllFeatures) {
            Column {
                if (installedConflicts.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Conflict: ${installedConflicts.joinToString()} detected. Hexodus handles this natively.",
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
                    colorIndicator = MaterialTheme.colorScheme.primary, // Switched to Material Theme Color
                    modifier = Modifier.alpha(if (isCompatible) 1f else 0.5f)
                )
            }
        }
    }

    @Composable
    fun CategoryButton(title: String, icon: ImageVector, route: String) {
        if (searchQuery.isNotEmpty() && !title.contains(searchQuery, ignoreCase = true)) return
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController?.navigate(route) }
                .height(IntrinsicSize.Min),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hexodus Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Row {
                        IconButton(onClick = { showAllFeatures = !showAllFeatures }) {
                            Icon(
                                if (showAllFeatures) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle hidden features"
                            )
                        }
                        IconButton(onClick = { navController?.navigate("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Framework Status Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FrameworkChip("Shizuku", caps?.isShizukuReady == true, Icons.Default.Bolt)
                    FrameworkChip("Dhizuku", caps?.isDhizukuReady == true, Icons.Default.Shield)
                    FrameworkChip("Vector", caps?.isVectorActive == true, Icons.Default.Hub)
                    FrameworkChip("Root", caps?.isRooted == true, Icons.Default.Terminal)
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search toggles, features, or mods...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // --- Quick Actions / Featured Toggles ---
        if (searchQuery.isEmpty()) {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        FeatureCard(
            title = "Circle to Search",
            description = "AI Search by circling anything on screen",
            icon = Icons.Default.Search,
            isEnabled = systemTunerEnabled,
            onToggle = { systemTunerEnabled = it },
            requirements = listOf("Samsung", "Shizuku")
        )

        FeatureCard(
            title = "Vertical App Drawer",
            description = "Switch to vertical scroll for app drawer",
            icon = Icons.Default.ViewStream,
            isEnabled = systemTunerEnabled,
            onToggle = { systemTunerEnabled = it },
            requirements = listOf("Samsung", "Shizuku")
        )

        FeatureCard(
            title = "Now Brief (S25 Exclusive)",
            description = "Enable hidden Smart Suggestions summary",
            icon = Icons.Default.AutoAwesome,
            isEnabled = systemTunerEnabled,
            onToggle = { systemTunerEnabled = it },
            requirements = listOf("S22Ultra", "Samsung")
        )

        FeatureCard(
            title = "Advanced Battery Stats",
            description = "View real cycle count and actual health",
            icon = Icons.Default.BatteryChargingFull,
            isEnabled = powerManagerEnabled,
            onToggle = { powerManagerEnabled = it },
            requirements = listOf("Shizuku")
        )

        FeatureCard(
            title = "Enhanced Processing",
            description = "Boost CPU responsiveness for smoother UI",
            icon = Icons.Default.Bolt,
            isEnabled = systemTunerEnabled,
            onToggle = { systemTunerEnabled = it },
            requirements = listOf("Samsung", "Shizuku")
        )

        if (searchQuery.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Android 16 (Next-Gen)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        FeatureCard(
            title = "Notification Cooldown",
            description = "Android 16 Baklava feature to quiet rapid-fire alerts",
            icon = Icons.Default.NotificationsPaused,
            isEnabled = notificationCooldownEnabled,
            onToggle = { notificationCooldownEnabled = it },
            requirements = listOf("Shizuku")
        )

        FeatureCard(
            title = "Desktop Windowing",
            description = "Android 16 Freeform Resizable Desktop Mode",
            icon = Icons.Default.DesktopWindows,
            isEnabled = desktopModeEnabled,
            onToggle = { desktopModeEnabled = it },
            requirements = listOf("Shizuku")
        )

        FeatureCard(
            title = "Screen-Off FOD",
            description = "Unlock device without waking display (requires ultrasonic sensor)",
            icon = Icons.Default.Fingerprint,
            isEnabled = screenOffFodEnabled,
            onToggle = { screenOffFodEnabled = it },
            requirements = listOf("Shizuku")
        )

        FeatureCard(
            title = "Vertical Quick Panel (UI 8.5)",
            description = "S26/One UI 8.5 redesigned vertical volume/brightness sliders",
            icon = Icons.Default.ViewQuilt,
            isEnabled = verticalQsEnabled,
            onToggle = { verticalQsEnabled = it },
            requirements = listOf("Samsung", "Shizuku")
        )

        FeatureCard(
            title = "Priority AI Notifications",
            description = "One UI 8.5 smart glow effect for critical alerts",
            icon = Icons.Default.NotificationsActive,
            isEnabled = priorityNotifsEnabled,
            onToggle = { priorityNotifsEnabled = it },
            requirements = listOf("Samsung", "Shizuku")
        )

        FeatureCard(
            title = "3D Glassmorphism Icons",
            description = "One UI 8.5 floating translucent design language",
            icon = Icons.Default.Layers,
            isEnabled = glassmorphismEnabled,
            onToggle = { glassmorphismEnabled = it },
            requirements = listOf("Samsung", "Shizuku")
        )

        if (searchQuery.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Exploration & Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        // Awesome Shizuku & Frameworks Repo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController?.navigate("awesome_shizuku") }
                .height(IntrinsicSize.Min),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Awesome Shizuku & Repos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        CategoryButton("Device Specific Options", Icons.Default.Smartphone, "features/Device Specific")
        CategoryButton("Camera Enhancements", Icons.Default.CameraAlt, "features/Camera")
        CategoryButton("Custom Mod Extensions", Icons.Default.Extension, "custom_mods")
        CategoryButton("Theming & Customization", Icons.Default.ColorLens, "features/Theming")
        CategoryButton("System Integration", Icons.Default.Settings, "features/System")
        
    }
}
