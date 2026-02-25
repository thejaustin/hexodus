package com.hexodus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hexodus.services.ModExtensionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomModsScreen(navController: NavController) {
    val context = LocalContext.current
    val modManager = remember { ModExtensionManager(context) }
    var mods by remember { mutableStateOf<List<ModExtensionManager.ModExtension>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        mods = modManager.discoverMods()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Mod Extensions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isLoading = true
                        mods = modManager.discoverMods()
                        isLoading = false
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (mods.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Extension, contentDescription = null, size = 48.dp, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No custom mods discovered.")
                    Text("Install mod APKs with 'com.hexodus.intent.action.MOD_EXTENSION' support.", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(mods) { mod ->
                    ListItem(
                        headlineContent = { Text(mod.appName) },
                        supportingContent = { Text("Version: ${mod.version} | Author: ${mod.author}") },
                        leadingContent = { 
                            Icon(
                                Icons.Default.Extension, 
                                contentDescription = null,
                                tint = if (mod.isVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        },
                        trailingContent = {
                            if (!mod.isVerified) {
                                Icon(Icons.Default.Warning, contentDescription = "Unverified", tint = MaterialTheme.colorScheme.error)
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.clickable { 
                            // Open mod settings or details
                        }
                    )
                    Divider()
                }
            }
        }
    }
}
