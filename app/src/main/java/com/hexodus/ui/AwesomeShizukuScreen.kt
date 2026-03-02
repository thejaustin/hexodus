package com.hexodus.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    var isRefreshing by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedTags by rememberSaveable(
        saver = Saver(
            save = { ArrayList<String>(it) },
            restore = { it.toSet() }
        )
    ) { mutableStateOf<Set<String>>(emptySet()) }
    var showIncompatible by rememberSaveable { mutableStateOf(false) }
    var caps by remember { mutableStateOf<CapabilityManager.DeviceCapabilities?>(null) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val allTags = remember(apps) {
        apps.flatMap { it.tags }.toSet().sorted()
    }

    fun loadData(isRefresh: Boolean = false) {
        coroutineScope.launch {
            if (isRefresh) isRefreshing = true else isLoading = true
            loadError = false
            try {
                caps = capabilityManager.detectCapabilities()
                apps = ShizukuRepoParser.fetchAwesomeShizukuList()
            } catch (e: Exception) {
                loadError = true
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) { loadData() }

    val filteredApps = remember(apps, selectedTags, showIncompatible, caps, searchQuery) {
        var base = if (selectedTags.isEmpty()) apps
                   else apps.filter { app -> selectedTags.all { tag -> app.tags.contains(tag) } }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            base = base.filter {
                it.name.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.author.lowercase().contains(q)
            }
        }

        if (showIncompatible || caps == null) base
        else base.filter { capabilityManager.isCompatible(it.tags, caps!!) }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Awesome Shizuku") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { loadData(isRefresh = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { loadData(isRefresh = true) },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (loadError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Couldn't load app list", style = MaterialTheme.typography.titleMedium)
                    Text("Check your internet connection.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
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
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search apps...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
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
                items(filteredApps.filter { it.category.contains("Backport") }, key = { it.name }) { app ->
                    val isCompatible = caps?.let { capabilityManager.isCompatible(app.tags, it) } ?: true
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .alpha(if (isCompatible) 1f else 0.6f)
                            .clickable(onClickLabel = "View ${app.name} details") { navController.navigate(NavRoutes.ShizukuDetail(app.name)) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCompatible) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
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
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        text = "All Shizuku Apps",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (filteredApps.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    if (searchQuery.isNotBlank()) "No results for \"$searchQuery\""
                                    else "No compatible apps found.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(filteredApps.filter { !it.category.contains("Backport") }, key = { it.name }) { app ->
                    val isCompatible = caps?.let { capabilityManager.isCompatible(app.tags, it) } ?: true
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .alpha(if (isCompatible) 1f else 0.6f)
                            .clickable(onClickLabel = "View ${app.name} details") { navController.navigate(NavRoutes.ShizukuDetail(app.name)) },
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
        } // end else (loaded)
        } // end PullToRefreshBox
    }
}
