package com.hexodus.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hexodus.services.CapabilityManager
import com.hexodus.utils.ShizukuRepoParser
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AwesomeShizukuScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val capabilityManager = remember { CapabilityManager(context) }
    
    var apps by remember { mutableStateOf<List<ShizukuRepoParser.ShizukuApp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showIncompatible by remember { mutableStateOf(false) }
    var caps by remember { mutableStateOf<CapabilityManager.DeviceCapabilities?>(null) }

    val allTags = remember(apps) {
        apps.flatMap { it.tags }.toSet().sorted()
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            caps = capabilityManager.detectCapabilities()
            apps = ShizukuRepoParser.fetchAwesomeShizukuList()
            isLoading = false
        }
    }

    val filteredApps = remember(apps, selectedTags, showIncompatible, caps) {
        val base = if (selectedTags.isEmpty()) {
            apps
        } else {
            apps.filter { app -> selectedTags.all { tag -> app.tags.contains(tag) } }
        }
        
        if (showIncompatible || caps == null) {
            base
        } else {
            base.filter { capabilityManager.isCompatible(it.tags, caps!!) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Awesome Shizuku") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Show All", style = MaterialTheme.typography.labelSmall)
                        Switch(
                            checked = showIncompatible,
                            onCheckedChange = { showIncompatible = it },
                            modifier = Modifier.scale(0.7f)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Premium Device Backports & Mods",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Filter by requirements or categories below:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allTags.toList()) { tag ->
                                FilterChip(
                                    selected = selectedTags.contains(tag),
                                    onClick = {
                                        selectedTags = if (selectedTags.contains(tag)) {
                                            selectedTags - tag
                                        } else {
                                            selectedTags + tag
                                        }
                                    },
                                    label = { Text(tag) },
                                    leadingIcon = if (selectedTags.contains(tag)) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }

                // Featured/Backport apps
                items(filteredApps.filter { it.category.contains("Backport") }) { app ->
                    val isCompatible = caps?.let { capabilityManager.isCompatible(app.tags, it) } ?: true
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .alpha(if (isCompatible) 1f else 0.6f)
                            .clickable(enabled = isCompatible || showIncompatible) {
                                navController.navigate("shizuku_detail/${app.name}")
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCompatible) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isCompatible) Icons.Default.AutoAwesome else Icons.Default.Block, 
                                    contentDescription = null, 
                                    tint = if (isCompatible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = app.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = app.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (!isCompatible) {
                                Text(
                                    text = "Incompatible with your current setup",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(app.tags) { tag ->
                                    val isShizukuPlus = tag == "Shizuku+"
                                    Surface(
                                        color = if (isShizukuPlus) MaterialTheme.colorScheme.primary else if (isCompatible) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.outlineVariant,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isShizukuPlus) MaterialTheme.colorScheme.onPrimary else if (isCompatible) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        text = "All Shizuku Apps",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                items(filteredApps.filter { !it.category.contains("Backport") }) { app ->
                    val isCompatible = caps?.let { capabilityManager.isCompatible(app.tags, it) } ?: true
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .alpha(if (isCompatible) 1f else 0.6f)
                            .clickable(enabled = isCompatible || showIncompatible) {
                                navController.navigate("shizuku_detail/${app.name}")
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = app.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCompatible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = app.description,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2
                            )
                            if (!isCompatible) {
                                Text(
                                    text = "Incompatible with your current setup",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(app.tags) { tag ->
                                    val isShizukuPlus = tag == "Shizuku+"
                                    Surface(
                                        color = if (isShizukuPlus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isShizukuPlus) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "By ${app.author}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
