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
        shizukuConnected = ShizukuBridge.isReady()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hexodus Theming Engine",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create custom themes using hex colors",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Shizuku status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (shizukuConnected) Icons.Outlined.CheckCircle else Icons.Outlined.Error,
                        contentDescription = if (shizukuConnected) "Shizuku Connected" else "Shizuku Disconnected",
                        tint = if (shizukuConnected) Color.Green else Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (shizukuConnected) "Shizuku Connected" else "Shizuku Disconnected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (shizukuConnected) Color.Green else Color.Red
                    )
                }
            }
        }
        
        // Hex Color Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Theme Color",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color preview box
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    try {
                                        Color(android.graphics.Color.parseColor(
                                            if (hexColor.startsWith("#")) hexColor else "#$hexColor"
                                        ))
                                    } catch (e: Exception) {
                                        Color.Gray
                                    }
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
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
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // Theme Name Input
        TextField(
            value = themeName,
            onValueChange = { themeName = it },
            label = { Text("Theme Name") },
            placeholder = { Text("My Custom Theme") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Components to Theme Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Components to Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                ComponentSelectionItem(
                    name = "Status Bar",
                    isSelected = isStatusBarThemed,
                    onSelectedChange = { isStatusBarThemed = it }
                )
                ComponentSelectionItem(
                    name = "Navigation Bar",
                    isSelected = isNavigationBarThemed,
                    onSelectedChange = { isNavigationBarThemed = it }
                )
                ComponentSelectionItem(
                    name = "System UI",
                    isSelected = isSystemUIThemed,
                    onSelectedChange = { isSystemUIThemed = it }
                )
                ComponentSelectionItem(
                    name = "Settings App",
                    isSelected = isSettingsThemed,
                    onSelectedChange = { isSettingsThemed = it }
                )
                ComponentSelectionItem(
                    name = "Launcher",
                    isSelected = isLauncherThemed,
                    onSelectedChange = { isLauncherThemed = it }
                )
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        navController?.navigate("preview/$hexColor/$themeName")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Preview")
                }

                Button(
                    onClick = {
                        val themedComponents = hashMapOf(
                            "status_bar" to isStatusBarThemed,
                            "navigation_bar" to isNavigationBarThemed,
                            "system_ui" to isSystemUIThemed,
                            "settings" to isSettingsThemed,
                            "launcher" to isLauncherThemed
                        )
                        scope.launch {
                            ThemeManager.createTheme(hexColor, themeName, themedComponents)
                        }
                        Toast.makeText(context, "Theme saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }
            }

            Button(
                onClick = {
                    val themedComponents = hashMapOf(
                        "status_bar" to isStatusBarThemed,
                        "navigation_bar" to isNavigationBarThemed,
                        "system_ui" to isSystemUIThemed,
                        "settings" to isSettingsThemed,
                        "launcher" to isLauncherThemed
                    )
                    scope.launch {
                        ThemeManager.createTheme(hexColor, themeName, themedComponents)
                        val themeFile = java.io.File(context.filesDir, "${themeName}_${System.currentTimeMillis()}.apk").absolutePath
                        ThemeManager.applyTheme(themeFile)
                    }
                    Toast.makeText(context, "Applying theme...", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.ColorLens, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Apply Theme")
            }
        }

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Info", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This app uses Shizuku for system-level theming without root access. Make sure Shizuku is installed and properly configured.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentSelectionItem(
    name: String,
    isSelected: Boolean,
    onSelectedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        onClick = { onSelectedChange(!isSelected) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectedChange,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
