package com.hexodus.ui

import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hexodus.utils.PrefsManager
import com.hexodus.utils.CrashHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager.getInstance(context) }
    
    var preferShizukuPlus by remember { mutableStateOf(prefs.preferShizukuPlus) }
    var enableDhizukuMode by remember { mutableStateOf(prefs.enableDhizukuMode) }
    var isRestoring by remember { mutableStateOf(false) }
    var crashLog by remember { mutableStateOf<String?>(null) }
    var showCrashDialog by remember { mutableStateOf(false) }

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
                }) {
                    Text("Clear Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCrashDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Privilege Routing",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            ListItem(
                headlineContent = { Text("Prefer Shizuku+ Enhanced API") },
                supportingContent = { Text("Use enhanced synchronous wrappers when Shizuku+ is detected.") },
                trailingContent = {
                    Switch(
                        checked = preferShizukuPlus,
                        onCheckedChange = {
                            preferShizukuPlus = it
                            prefs.preferShizukuPlus = it
                        }
                    )
                }
            )
            
            ListItem(
                headlineContent = { Text("Enable Dhizuku (Device Owner) Mode") },
                supportingContent = { Text("Instantly freezes apps and manages device policies without shell commands. Requires the Shizuku+ manager app.") },
                trailingContent = {
                    Switch(
                        checked = enableDhizukuMode,
                        onCheckedChange = {
                            enableDhizukuMode = it
                            prefs.enableDhizukuMode = it
                        }
                    )
                }
            )
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "System Recovery",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Restore System Defaults",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Use this if a flag or overlay causes SystemUI to crash. This will delete all customized flags (Now Bar, Performance Mode, etc.) and forcefully restart the SystemUI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isRestoring = true
                            com.hexodus.services.FeatureFlagsService.restoreSystemDefaults()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.align(Alignment.End),
                        enabled = !isRestoring
                    ) {
                        Text(if (isRestoring) "Restoring..." else "Reset Flags")
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Crash Diagnostics",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("View Latest Crash Log") },
                supportingContent = { Text(if (crashLog != null) "A crash was detected recently." else "No crashes detected.") },
                trailingContent = {
                    if (crashLog != null) {
                        IconButton(onClick = { showCrashDialog = true }) {
                            Icon(Icons.Default.Info, contentDescription = "View Log")
                        }
                    }
                }
            )
        }
    }
}
