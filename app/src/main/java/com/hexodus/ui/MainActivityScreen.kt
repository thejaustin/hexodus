package com.hexodus.ui

import androidx.navigation.NavController
import androidx.compose.foundation.background
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
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hexodus.services.ShizukuBridge
import com.hexodus.services.ThemeManager
import com.hexodus.utils.PrefsManager
import kotlinx.coroutines.launch

private data class StatusSnackbarVisuals(
    override val message: String,
    val isError: Boolean,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short
) : SnackbarVisuals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PrefsManager.getInstance(context) }
    var hexColor by remember { mutableStateOf(prefs.getThemeColor()) }
    var themeName by remember { mutableStateOf(prefs.getThemeName()) }
    var isStatusBarThemed by remember { mutableStateOf(prefs.getComponentThemed("status_bar")) }
    var isNavigationBarThemed by remember { mutableStateOf(prefs.getComponentThemed("navigation_bar")) }
    var isSystemUIThemed by remember { mutableStateOf(prefs.getComponentThemed("system_ui")) }
    var isSettingsThemed by remember { mutableStateOf(prefs.getComponentThemed("settings")) }
    var isLauncherThemed by remember { mutableStateOf(prefs.getComponentThemed("launcher")) }
    var isSaving by remember { mutableStateOf(false) }
    var isApplying by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var shizukuConnected by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val isHexError = remember(hexColor) {
        hexColor.isNotEmpty() && try {
            android.graphics.Color.parseColor(
                if (hexColor.startsWith("#")) hexColor else "#$hexColor"
            )
            false
        } catch (e: Exception) { true }
    }

    LaunchedEffect(Unit) {
        shizukuConnected = ShizukuBridge.isReady()
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                StatusSnackbarVisuals(message = msg, isError = msg.startsWith("Failed"))
            )
            statusMessage = null
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val isError = (data.visuals as? StatusSnackbarVisuals)?.isError == true
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isError) MaterialTheme.colorScheme.errorContainer
                                     else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer
                                   else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                        } catch (e: Exception) { Color.Gray }
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
                                    prefs.setThemeColor(it.uppercase())
                                }
                            },
                            label = { Text("Hex Color") },
                            placeholder = { Text("#FF6200EE") },
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    if (isHexError) error("Invalid hex color. Use #RRGGBB or #AARRGGBB format.")
                                },
                            singleLine = true,
                            isError = isHexError,
                            supportingText = if (isHexError) {
                                { Text("Use #RRGGBB or #AARRGGBB format") }
                            } else null
                        )
                    }
                }
            }

            // Theme Name Input
            TextField(
                value = themeName,
                onValueChange = { themeName = it; prefs.setThemeName(it) },
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
                        onSelectedChange = { isStatusBarThemed = it; prefs.setComponentThemed("status_bar", it) }
                    )
                    ComponentSelectionItem(
                        name = "Navigation Bar",
                        isSelected = isNavigationBarThemed,
                        onSelectedChange = { isNavigationBarThemed = it; prefs.setComponentThemed("navigation_bar", it) }
                    )
                    ComponentSelectionItem(
                        name = "System UI",
                        isSelected = isSystemUIThemed,
                        onSelectedChange = { isSystemUIThemed = it; prefs.setComponentThemed("system_ui", it) }
                    )
                    ComponentSelectionItem(
                        name = "Settings App",
                        isSelected = isSettingsThemed,
                        onSelectedChange = { isSettingsThemed = it; prefs.setComponentThemed("settings", it) }
                    )
                    ComponentSelectionItem(
                        name = "Launcher",
                        isSelected = isLauncherThemed,
                        onSelectedChange = { isLauncherThemed = it; prefs.setComponentThemed("launcher", it) }
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
                        onClick = { navController?.navigate(NavRoutes.Preview(hexColor, themeName)) },
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
                            isSaving = true
                            statusMessage = null
                            scope.launch {
                                ThemeManager.createTheme(hexColor, themeName, themedComponents)
                                statusMessage = "Theme saved!"
                                isSaving = false
                            }
                        },
                        enabled = !isSaving && !isApplying,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSaving) "Saving..." else "Save")
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
                        isApplying = true
                        statusMessage = null
                        scope.launch {
                            ThemeManager.createTheme(hexColor, themeName, themedComponents)
                            val path = ThemeManager.lastCreatedThemePath
                            if (path != null) {
                                val success = ThemeManager.applyTheme(path)
                                statusMessage = if (success) "Theme applied!" else "Failed to apply theme."
                            } else {
                                statusMessage = "Failed to create theme."
                            }
                            isApplying = false
                        }
                    },
                    enabled = !isSaving && !isApplying,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isApplying) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.ColorLens, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (isApplying) "Applying..." else "Apply Theme")
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
