package com.hexodus.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hexodus.services.CapabilityManager
import com.hexodus.services.ShizukuInstallerService
import com.hexodus.utils.ShizukuRepoParser
import com.hexodus.utils.RedundancyEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShizukuAppDetailScreen(navController: NavController, appName: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val capabilityManager = remember { CapabilityManager(context) }
    
    var app by remember { mutableStateOf<ShizukuRepoParser.ShizukuApp?>(null) }
    var similarApps by remember { mutableStateOf<List<ShizukuRepoParser.ShizukuApp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var caps by remember { mutableStateOf<CapabilityManager.Capabilities?>(null) }
    
    // Installation state
    var isInstalling by remember { mutableStateOf(false) }
    var installProgress by remember { mutableStateOf(0) }
    var installStatus by remember { mutableStateOf("Downloading...") }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    "APK_INSTALLATION_PROGRESS" -> {
                        val name = intent.getStringExtra("app_name")
                        if (name == appName) {
                            installProgress = intent.getIntExtra("progress", 0)
                            intent.getStringExtra("status")?.let { installStatus = it }
                        }
                    }
                    "APK_INSTALLATION_RESULT" -> {
                        val name = intent.getStringExtra("app_name")
                        if (name == appName || name == null) {
                            isInstalling = false
                            val success = intent.getBooleanExtra("success", false)
                            // Here we could show a snackbar or toast
                        }
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction("APK_INSTALLATION_PROGRESS")
            addAction("APK_INSTALLATION_RESULT")
        }
        context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    LaunchedEffect(appName) {
        coroutineScope.launch {
            val currentCaps = capabilityManager.detectCapabilities()
            caps = currentCaps
            
            val apps = ShizukuRepoParser.fetchAwesomeShizukuList()
            val foundApp = apps.find { it.name == appName }
            app = foundApp
            
            if (foundApp != null) {
                similarApps = apps.filter { it.name != foundApp.name }
                    .sortedByDescending { other ->
                        var score = 0
                        if (other.category == foundApp.category) score += 5
                        score += other.tags.count { foundApp.tags.contains(it) }
                        if (capabilityManager.isCompatible(other.tags, currentCaps)) score += 10
                        score
                    }.take(6)
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app?.name ?: "App Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (app == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("App not found.")
            }
        } else {
            val isCompatible = caps?.let { capabilityManager.isCompatible(app!!.tags, it) } ?: true
            val replacementFeature = RedundancyEngine.getReplacementFeature(app!!.name)
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = app!!.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (replacementFeature != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Native Replacement Available",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Hexodus can now handle this functionality natively using the Shizuku+ API. You do not need to install this standalone app.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(
                                onClick = { navController.navigate("dashboard") },
                                modifier = Modifier.align(androidx.compose.ui.Alignment.End)
                            ) {
                                Text("Go to Hexodus Features")
                            }
                        }
                    }
                }

                if (!isCompatible) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "This app requires permissions (${app!!.tags.joinToString()}) not currently active on your device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Category: ${app!!.category}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    app!!.tags.forEach { tag ->
                        val isShizukuPlus = tag == "Shizuku+"
                        Surface(
                            modifier = Modifier.padding(end = 4.dp),
                            color = if (isShizukuPlus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isShizukuPlus) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Author: ${app!!.author}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Repository Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = app!!.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "GitHub: ${app!!.repoUrl}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (isInstalling) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text(installStatus, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = installProgress / 100f,
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            isInstalling = true
                            installProgress = 0
                            installStatus = "Downloading..."
                            installApp(coroutineScope, context, app!!)
                        },
                        enabled = isCompatible,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = "Install")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isCompatible) "Download & Install" else "Incompatible Setup")
                    }
                }

                if (similarApps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = "Alternative / Similar Apps",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Showing apps with similar functions",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(similarApps) { sim ->
                            val simCompatible = caps?.let { capabilityManager.isCompatible(sim.tags, it) } ?: true
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable {
                                        navController.navigate("shizuku_detail/${sim.name}")
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (simCompatible) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (simCompatible) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        Text(
                                            text = sim.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (simCompatible) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Compatible", tint = androidx.compose.ui.graphics.Color.Green, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = sim.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = sim.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row {
                                        sim.tags.take(2).forEach { tag ->
                                            Text(
                                                text = tag,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(end = 4.dp),
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun installApp(scope: kotlinx.coroutines.CoroutineScope, context: Context, app: ShizukuRepoParser.ShizukuApp) {
    val downloadUrl = if (app.downloadUrl.endsWith("/releases/latest")) {
        app.downloadUrl
    } else {
        app.downloadUrl
    }

    scope.launch {
        com.hexodus.services.ShizukuInstaller.downloadAndInstall(downloadUrl, app.name)
    }
}
