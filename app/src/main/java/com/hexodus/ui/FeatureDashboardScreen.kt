package com.hexodus.ui

import androidx.compose.foundation.layout.*
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
import com.hexodus.ui.components.CategoryHeader
import com.hexodus.ui.components.FeatureToggleCard

import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureDashboardScreen(navController: NavController? = null) {
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

    var shizukuConnected by remember { mutableStateOf(false) }

    // Check Shizuku connection status
    LaunchedEffect(Unit) {
        // In a real implementation, we would check the actual Shizuku status
        shizukuConnected = true // Placeholder
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min), // Ensures minimum touch target size
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(28.dp) // M3E shape standard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp), // Increased padding for accessibility
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hexodus Feature Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Shizuku status indicator with improved accessibility
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (shizukuConnected) Icons.Outlined.CheckCircle else Icons.Outlined.Error,
                        contentDescription = if (shizukuConnected) "Shizuku Connected" else "Shizuku Disconnected", // Accessibility description
                        tint = if (shizukuConnected) Color.Green else Color.Red,
                        modifier = Modifier.size(24.dp) // Ensure minimum touch target
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (shizukuConnected) "Shizuku Connected" else "Shizuku Disconnected",
                        color = if (shizukuConnected) Color.Green else Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Theming Features - M3E compliant category header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController?.navigate("features/Theming") }
                .height(IntrinsicSize.Min), // Ensures minimum touch target size
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(20.dp) // M3E shape standard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = "Theming & Customization Category", // Accessibility description
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp) // Larger icon for better visibility
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Theming & Customization",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureToggleCard(
            title = "Hex-to-Overlay Compilation",
            description = "Convert hex colors to system-compatible overlays",
            icon = Icons.Default.Colorize,
            isEnabled = themingEnabled,
            onToggle = { themingEnabled = it },
            colorIndicator = Color(0xFF6200EE)
        )
        
        FeatureToggleCard(
            title = "Material You Override",
            description = "Bypass One UI 8's aggressive Monet enforcement",
            icon = Icons.Default.Style,
            isEnabled = themingEnabled,
            onToggle = { themingEnabled = it },
            colorIndicator = Color(0xFF03DAC6)
        )
        
        FeatureToggleCard(
            title = "High Contrast Injection",
            description = "Exploit accessibility themes for deeper customization",
            icon = Icons.Default.Visibility,
            isEnabled = themingEnabled,
            onToggle = { themingEnabled = it },
            colorIndicator = Color(0xFFFF9800)
        )
        
        // System Integration Features - M3E compliant category header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController?.navigate("features/System") }
                .height(IntrinsicSize.Min), // Ensures minimum touch target size
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(20.dp) // M3E shape standard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "System Integration Category", // Accessibility description
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp) // Larger icon for better visibility
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "System Integration",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureToggleCard(
            title = "System Tuner",
            description = "Access and modify hidden system settings",
            icon = Icons.Default.Tune,
            isEnabled = systemTunerEnabled,
            onToggle = { systemTunerEnabled = it },
            colorIndicator = Color(0xFF9C27B0)
        )
        
        FeatureToggleCard(
            title = "Overlay Management",
            description = "Manage system overlays without root",
            icon = Icons.Default.ViewCompact,
            isEnabled = systemTunerEnabled,
            onToggle = { systemTunerEnabled = it },
            colorIndicator = Color(0xFF9C27B0)
        )
        
        // App Management Features - M3E compliant category header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController?.navigate("features/App Management") }
                .height(IntrinsicSize.Min), // Ensures minimum touch target size
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(20.dp) // M3E shape standard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "App Management Category", // Accessibility description
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp) // Larger icon for better visibility
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "App Management",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureToggleCard(
            title = "App Themer",
            description = "Per-app theming and dark mode control",
            icon = Icons.Default.InvertColors,
            isEnabled = appThemerEnabled,
            onToggle = { appThemerEnabled = it },
            colorIndicator = Color(0xFF4CAF50)
        )
        
        FeatureToggleCard(
            title = "App Manager",
            description = "Freeze, hide, and manage apps",
            icon = Icons.Default.HideSource,
            isEnabled = appManagerEnabled,
            onToggle = { appManagerEnabled = it },
            colorIndicator = Color(0xFF4CAF50)
        )
        
        // Privacy & Security Features - M3E compliant category header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController?.navigate("features/Privacy") }
                .height(IntrinsicSize.Min), // Ensures minimum touch target size
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(20.dp) // M3E shape standard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Privacy & Security Category", // Accessibility description
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp) // Larger icon for better visibility
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Privacy & Security",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureToggleCard(
            title = "App Lock",
            description = "Secure apps with PIN or biometric",
            icon = Icons.Default.Lock,
            isEnabled = privacySecurityEnabled,
            onToggle = { privacySecurityEnabled = it },
            colorIndicator = Color(0xFFF44336)
        )
        
        FeatureToggleCard(
            title = "File Hider",
            description = "Hide sensitive files from other apps",
            icon = Icons.Default.VisibilityOff,
            isEnabled = privacySecurityEnabled,
            onToggle = { privacySecurityEnabled = it },
            colorIndicator = Color(0xFFF44336)
        )
        
        // Network & Power Features - M3E compliant category header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController?.navigate("features/Network") }
                .height(IntrinsicSize.Min), // Ensures minimum touch target size
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(20.dp) // M3E shape standard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = "Network & Power Category", // Accessibility description
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp) // Larger icon for better visibility
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Network & Power",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureToggleCard(
            title = "App Firewall",
            description = "Block network access for specific apps",
            icon = Icons.Default.Shield,
            isEnabled = networkFirewallEnabled,
            onToggle = { networkFirewallEnabled = it },
            colorIndicator = Color(0xFF2196F3)
        )
        
        FeatureToggleCard(
            title = "Power Manager",
            description = "Battery optimization and doze control",
            icon = Icons.Default.BatterySaver,
            isEnabled = powerManagerEnabled,
            onToggle = { powerManagerEnabled = it },
            colorIndicator = Color(0xFF2196F3)
        )
        
        // Audio & Media Features - M3E compliant category header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController?.navigate("features/Audio") }
                .height(IntrinsicSize.Min), // Ensures minimum touch target size
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(20.dp) // M3E shape standard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Audiotrack,
                    contentDescription = "Audio & Media Category", // Accessibility description
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp) // Larger icon for better visibility
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Audio & Media",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureToggleCard(
            title = "Audio Enhancer",
            description = "Equalizer and audio effects",
            icon = Icons.Default.Equalizer,
            isEnabled = audioManagerEnabled,
            onToggle = { audioManagerEnabled = it },
            colorIndicator = Color(0xFF3F51B5)
        )
        
        FeatureToggleCard(
            title = "Now Playing",
            description = "Media information display",
            icon = Icons.Default.PlayCircle,
            isEnabled = mediaNotificationEnabled,
            onToggle = { mediaNotificationEnabled = it },
            colorIndicator = Color(0xFF3F51B5)
        )
        
        // Interaction Features - M3E compliant category header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController?.navigate("features/Interaction") }
                .height(IntrinsicSize.Min), // Ensures minimum touch target size
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(20.dp) // M3E shape standard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = "Interaction Category", // Accessibility description
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp) // Larger icon for better visibility
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Interaction",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureToggleCard(
            title = "Gesture Manager",
            description = "Customize device gestures",
            icon = Icons.Default.TouchApp,
            isEnabled = gestureManagerEnabled,
            onToggle = { gestureManagerEnabled = it },
            colorIndicator = Color(0xFF9E9E9E)
        )
        
        // Foldable Device Features - M3E compliant category header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController?.navigate("features/Theming") }
                .height(IntrinsicSize.Min), // Ensures minimum touch target size
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(20.dp) // M3E shape standard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DeviceHub,
                    contentDescription = "Foldable Support Category", // Accessibility description
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp) // Larger icon for better visibility
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Foldable Support",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureToggleCard(
            title = "Foldable Display",
            description = "Optimize for Z Flip 5 and foldables",
            icon = Icons.Default.DeviceHub,
            isEnabled = themingEnabled, // Uses theming engine
            onToggle = { themingEnabled = it },
            colorIndicator = Color(0xFF795548)
        )
    }
}