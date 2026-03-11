package com.hexodus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hexodus.utils.CrashHandler
import com.hexodus.utils.PrefsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager.getInstance(context) }

    var preferShizukuPlus by remember { mutableStateOf(prefs.preferShizukuPlus) }
    var enableDhizukuMode by remember { mutableStateOf(prefs.enableDhizukuMode) }
    var useDynamicTheming by remember { mutableStateOf(prefs.useDynamicTheming) }
    var showIncompatible by remember { mutableStateOf(prefs.showIncompatibleFeatures) }
    var overrideCompat by remember { mutableStateOf(prefs.overrideCompatibility) }
    var showDeprecated by remember { mutableStateOf(prefs.showDeprecatedTools) }
    var selectedMethod by remember { mutableStateOf(prefs.preferredPrivilegeMethod) }
    var isRestoring by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var crashLog by remember { mutableStateOf<String?>(null) }
    var showCrashDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(Unit) {
        crashLog = CrashHandler.getCrashLog(context)
    }

    if (showCrashDialog && crashLog != null) {
        AlertDialog(
            onDismissRequest = { showCrashDialog = false },
            title = { Text("Latest Crash Report") },
            text = {
                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    Text(
                        text = crashLog!!,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    CrashHandler.clearCrashLog(context)
                    crashLog = null
                    showCrashDialog = false
                }) { Text("Clear Log") }
            },
            dismissButton = {
                TextButton(onClick = { showCrashDialog = false }) { Text("Close") }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Reset All Flags?") },
            text = { Text("This will delete all customized feature flags and restart SystemUI. Use this if your device is in a crash loop. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        isRestoring = true
                        coroutineScope.launch {
                            com.hexodus.services.FeatureFlagsService.restoreSystemDefaults()
                            isRestoring = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Appearance ───────────────────────────────────────────────────
            SettingsSectionHeader("Appearance", Icons.Default.Palette)

            ListItem(
                modifier = Modifier.toggleable(
                    value = useDynamicTheming,
                    role = Role.Switch,
                    onValueChange = { useDynamicTheming = it; prefs.useDynamicTheming = it }
                ),
                headlineContent = { Text("Dynamic Theming (Monet)") },
                supportingContent = { Text("Use system accent colors from your wallpaper. Requires Android 12+.") },
                trailingContent = {
                    Switch(checked = useDynamicTheming, onCheckedChange = null)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Feature Visibility ────────────────────────────────────────────
            SettingsSectionHeader("Feature Visibility", Icons.Default.Visibility)

            ListItem(
                modifier = Modifier.toggleable(
                    value = showIncompatible,
                    role = Role.Switch,
                    onValueChange = { showIncompatible = it; prefs.showIncompatibleFeatures = it }
                ),
                headlineContent = { Text("Show Incompatible Features") },
                supportingContent = { Text("Display features your device doesn't meet requirements for.") },
                trailingContent = {
                    Switch(checked = showIncompatible, onCheckedChange = null)
                }
            )

            ListItem(
                modifier = Modifier.toggleable(
                    value = showDeprecated,
                    role = Role.Switch,
                    onValueChange = { showDeprecated = it; prefs.showDeprecatedTools = it }
                ),
                headlineContent = { Text("Show Deprecated Tools") },
                supportingContent = { Text("Show older tools that are pending removal.") },
                trailingContent = {
                    Switch(checked = showDeprecated, onCheckedChange = null)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Compatibility ─────────────────────────────────────────────────
            SettingsSectionHeader("Compatibility", Icons.Default.Tune)

            ListItem(
                modifier = Modifier.toggleable(
                    value = overrideCompat,
                    role = Role.Switch,
                    onValueChange = { overrideCompat = it; prefs.overrideCompatibility = it }
                ),
                headlineContent = { Text("Override Compatibility Checks") },
                supportingContent = { Text("Allow enabling features even when requirements aren't met. Use with caution.") },
                trailingContent = {
                    Switch(checked = overrideCompat, onCheckedChange = null)
                }
            )

            // Preferred Privilege Method
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Preferred Privilege Method",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Which method to use when multiple are available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                val methods = listOf("Auto", "Root", "Shizuku", "Shizuku+", "ADB")
                val methodValues = listOf("auto", "root", "shizuku", "shizukuplus", "adb")
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    methods.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = selectedMethod == methodValues[index],
                            onClick = {
                                selectedMethod = methodValues[index]
                                prefs.preferredPrivilegeMethod = methodValues[index]
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = methods.size)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Privilege Routing ─────────────────────────────────────────────
            SettingsSectionHeader("Privilege Routing", Icons.Default.Shield)

            ListItem(
                modifier = Modifier.toggleable(
                    value = preferShizukuPlus,
                    role = Role.Switch,
                    onValueChange = { preferShizukuPlus = it; prefs.preferShizukuPlus = it }
                ),
                headlineContent = { Text("Prefer Shizuku+ Enhanced API") },
                supportingContent = { Text("Use enhanced synchronous wrappers when Shizuku+ is detected.") },
                trailingContent = {
                    Switch(checked = preferShizukuPlus, onCheckedChange = null)
                }
            )

            ListItem(
                modifier = Modifier.toggleable(
                    value = enableDhizukuMode,
                    role = Role.Switch,
                    onValueChange = { enableDhizukuMode = it; prefs.enableDhizukuMode = it }
                ),
                headlineContent = { Text("Enable Dhizuku (Device Owner) Mode") },
                supportingContent = { Text("Freeze apps and manage device policies without shell commands. Requires Shizuku+.") },
                trailingContent = {
                    Switch(checked = enableDhizukuMode, onCheckedChange = null)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── System Recovery ───────────────────────────────────────────────
            SettingsSectionHeader("System Recovery", Icons.Default.Warning, isError = true)

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Use this if a flag or overlay causes SystemUI to crash. Deletes all customized flags and forcefully restarts SystemUI.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showResetConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.align(Alignment.End),
                            enabled = !isRestoring
                        ) {
                            Text(if (isRestoring) "Restoring..." else "Reset Flags")
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Crash Diagnostics ─────────────────────────────────────────────
            SettingsSectionHeader("Crash Diagnostics", Icons.Default.BugReport)

            ListItem(
                headlineContent = { Text("View Latest Crash Log") },
                supportingContent = {
                    Text(if (crashLog != null) "A crash was detected recently." else "No crashes detected.")
                },
                trailingContent = {
                    if (crashLog != null) {
                        IconButton(onClick = { showCrashDialog = true }) {
                            Icon(Icons.Default.Info, contentDescription = "View Log")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}
