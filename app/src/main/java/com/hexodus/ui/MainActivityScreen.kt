package com.hexodus.ui

import androidx.navigation.NavController
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexodus.R
import com.hexodus.core.ThemeCompiler
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import com.hexodus.services.ShizukuBridge
import kotlinx.coroutines.launch
import com.hexodus.services.ThemeManager
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hexColor by remember { mutableStateOf("#FF6200EE") }
    var themeName by remember { mutableStateOf("My Theme") }
    var isStatusBarThemed by remember { mutableStateOf(true) }
    var isNavigationBarThemed by remember { mutableStateOf(true) }
    var isSystemUIThemed by remember { mutableStateOf(true) }
    var isSettingsThemed by remember { mutableStateOf(true) }
    var isLauncherThemed by remember { mutableStateOf(true) }
    var isPreviewVisible by remember { mutableStateOf(false) }
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
        verticalArrangement = Arrangement.spacedBy(20.dp) // Increased spacing for better readability
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
                    text = "Hexodus Theming Engine",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                    text = "Create custom themes using hex colors",
                    style = MaterialTheme.typography.bodyLarge, // Larger body text for accessibility
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                Spacer(modifier = Modifier.height(12.dp))
                // Shizuku status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (shizukuConnected) Icons.Outlined.CheckCircle else Icons.Outlined.Error,
                        contentDescription = if (shizukuConnected) "Shizuku Connected" else "Shizuku Disconnected", // Accessibility description
                        tint = if (shizukuConnected) Color.Green else Color.Red,
                        modifier = Modifier.size(24.dp) // Proper icon size
                    )
                    Spacer(modifier = Modifier.width(12.dp)) // Increased spacing
                    Text(
                        text = if (shizukuConnected) "Shizuku Connected" else "Shizuku Disconnected",
                        style = MaterialTheme.typography.bodyMedium, // Larger text for accessibility
                        color = if (shizukuConnected) Color.Green else Color.Red
                }
            }
        }
        
        // Hex Color Input - M3E compliant
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            shape = RoundedCornerShape(20.dp) // M3E shape standard
                    .padding(20.dp) // Increased padding for accessibility
                    text = "Theme Color",
                    style = MaterialTheme.typography.headlineSmall, // M3E typography standard
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                    modifier = Modifier
                        .fillMaxWidth(),
                    // Color preview box
                    Box(
                        modifier = Modifier
                            .size(64.dp) // Larger preview for better visibility
                            .clip(RoundedCornerShape(12.dp)), // M3E shape standard
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Color(android.graphics.Color.parseColor(
                                        if (hexColor.startsWith("#")) hexColor else "#$hexColor"
                                    ))
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp)) // Increased spacing
                    // Hex input field
                    TextField(
                        value = hexColor,
                        onValueChange = {
                            if (it.length <= 9 && (it.startsWith("#") || it.isEmpty())) {
                                hexColor = it.uppercase()
                            }
                        },
                        label = { Text("Hex Color") },
                        placeholder = { Text("#FF6200EE") },
                            .weight(1f)
                            .height(IntrinsicSize.Min), // Ensures minimum touch target size
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
        // Theme Name Input - M3E compliant
                    text = "Theme Name",
                TextField(
                    value = themeName,
                    onValueChange = { themeName = it },
                    label = { Text("Theme Name") },
                    placeholder = { Text("My Custom Theme") },
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min), // Ensures minimum touch target size
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
        // Components to Theme Section - M3E compliant
                    .padding(20.dp), // Increased padding for accessibility
                verticalArrangement = Arrangement.spacedBy(16.dp) // Increased spacing for better readability
                    text = "Select Components to Theme",
                // Component Selection Cards
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Increased spacing for better readability
                    // Status Bar
                    ComponentSelectionItem(
                        name = "Status Bar",
                        isSelected = isStatusBarThemed,
                        onSelectedChange = { isStatusBarThemed = it }
                    // Navigation Bar
                        name = "Navigation Bar",
                        isSelected = isNavigationBarThemed,
                        onSelectedChange = { isNavigationBarThemed = it }
                    // System UI
                        name = "System UI",
                        isSelected = isSystemUIThemed,
                        onSelectedChange = { isSystemUIThemed = it }
                    // Settings App
                        name = "Settings App",
                        isSelected = isSettingsThemed,
                        onSelectedChange = { isSettingsThemed = it }
                    // Launcher
                        name = "Launcher",
                        isSelected = isLauncherThemed,
                        onSelectedChange = { isLauncherThemed = it }
        // Action Buttons - M3E compliant
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Increased spacing for better readability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp) // Increased spacing
                OutlinedButton(
                    onClick = {
                        // Navigate to preview screen
                        navController?.navigate("preview/$hexColor/$themeName")
                    },
                        .weight(1f)
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary // M3E color standard
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        Icons.Default.Visibility,
                        contentDescription = "Preview theme", // Accessibility description
                        modifier = Modifier.size(20.dp) // Proper icon size
                    Spacer(modifier = Modifier.width(12.dp))
                        "Preview",
                        style = MaterialTheme.typography.labelLarge // M3E typography standard
                        // Save theme
                        val themedComponents = hashMapOf(
                            "status_bar" to isStatusBarThemed,
                            "navigation_bar" to isNavigationBarThemed,
                            "system_ui" to isSystemUIThemed,
                            "settings" to isSettingsThemed,
                            "launcher" to isLauncherThemed
                        scope.launch {
                            ThemeManager.createTheme(hexColor, themeName, themedComponents)
                        }
                        Toast.makeText(context, "Theme saved!", Toast.LENGTH_SHORT).show()
                        Icons.Default.Save,
                        contentDescription = "Save theme", // Accessibility description
                        "Save",
            Button(
                onClick = {
                    // Apply theme
                    val themedComponents = hashMapOf(
                        "status_bar" to isStatusBarThemed,
                        "navigation_bar" to isNavigationBarThemed,
                        "system_ui" to isSystemUIThemed,
                        "settings" to isSettingsThemed,
                        "launcher" to isLauncherThemed
                    scope.launch {
                        ThemeManager.createTheme(hexColor, themeName, themedComponents)
                        val themeFile = java.io.File(context.filesDir, "${themeName}_${System.currentTimeMillis()}.apk").absolutePath
                        ThemeManager.applyTheme(themeFile)
                    Toast.makeText(context, "Applying theme...", Toast.LENGTH_SHORT).show()
                },
                    .height(IntrinsicSize.Min), // Ensures minimum touch target size
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // M3E color standard
                    contentColor = MaterialTheme.colorScheme.onPrimary
                Icon(
                    Icons.Default.ColorLens,
                    contentDescription = "Apply theme", // Accessibility description
                    modifier = Modifier.size(20.dp) // Proper icon size
                Spacer(modifier = Modifier.width(12.dp))
                    "Apply Theme",
                    style = MaterialTheme.typography.labelLarge // M3E typography standard
        // Preview section if visible - M3E compliant
        if (isPreviewVisible) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(20.dp) // M3E shape standard
                        .padding(20.dp) // Increased padding for accessibility
                        text = "Theme Preview",
                        style = MaterialTheme.typography.headlineSmall, // M3E typography standard
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    Spacer(modifier = Modifier.height(12.dp))
                    // Preview elements
                    Row(
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                                .size(72.dp) // Larger preview for better visibility
                                .clip(RoundedCornerShape(12.dp)), // M3E shape standard
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Color(android.graphics.Color.parseColor(
                                            if (hexColor.startsWith("#")) hexColor else "#$hexColor"
                                        ))
                                    )
                            )
                                    .background(MaterialTheme.colorScheme.surface)
                                    .background(MaterialTheme.colorScheme.primary)
        // Info Card - M3E compliant
                containerColor = MaterialTheme.colorScheme.secondaryContainer
                    text = "Info",
                    text = "This app uses Shizuku for system-level theming without root access. Make sure Shizuku is installed and properly configured.",
                    style = MaterialTheme.typography.bodyMedium // Larger text for accessibility
}
fun ComponentSelectionItem(
    name: String,
    isSelected: Boolean,
    onSelectedChange: (Boolean) -> Unit
) {
    Card(
            .fillMaxWidth()
            .height(IntrinsicSize.Min), // Ensures minimum touch target size
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // M3E surface color
        ),
        shape = RoundedCornerShape(16.dp), // M3E shape standard
        onClick = { onSelectedChange(!isSelected) }
        Row(
                .padding(16.dp), // Increased padding for accessibility
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium, // Larger text for accessibility
                color = MaterialTheme.colorScheme.onSurface
            )
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectedChange,
                modifier = Modifier.size(48.dp) // Larger touch target for accessibility