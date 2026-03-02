package com.hexodus.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hexodus.services.ModExtensionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomModsScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var mods by remember { mutableStateOf<List<ModExtensionManager.ModExtension>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var pendingUnverifiedMod by remember { mutableStateOf<ModExtensionManager.ModExtension?>(null) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    fun refresh(isRefresh: Boolean = false) {
        coroutineScope.launch {
            if (isRefresh) isRefreshing = true else isLoading = true
            mods = ModExtensionManager.discoverMods()
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    pendingUnverifiedMod?.let { mod ->
        AlertDialog(
            onDismissRequest = { pendingUnverifiedMod = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Unverified Mod") },
            text = { Text("\"${mod.name}\" has not been verified by Hexodus. It may behave unexpectedly or request dangerous permissions. Launch anyway?") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingUnverifiedMod = null
                        val launchIntent = context.packageManager.getLaunchIntentForPackage(mod.packageName)
                        if (launchIntent != null) context.startActivity(launchIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Launch Anyway") }
            },
            dismissButton = {
                TextButton(onClick = { pendingUnverifiedMod = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Custom Mod Extensions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refresh(isRefresh = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { refresh(isRefresh = true) },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (mods.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Extension, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No custom mods discovered.")
                    Text("Install mod APKs with 'com.hexodus.intent.action.MOD_EXTENSION' support.", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(mods) { mod ->
                    ListItem(
                        headlineContent = { Text(mod.name) },
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
                            if (mod.isVerified) {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(mod.packageName)
                                if (launchIntent != null) context.startActivity(launchIntent)
                            } else {
                                pendingUnverifiedMod = mod
                            }
                        }
                    )
                }
            }
        }
        } // end PullToRefreshBox
    }
}
