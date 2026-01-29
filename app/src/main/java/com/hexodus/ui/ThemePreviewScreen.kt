package com.hexodus.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hexodus.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePreviewScreen(
    hexColor: String,
    themeName: String,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(if (hexColor.startsWith("#")) hexColor else "#$hexColor"))
    } catch (e: Exception) {
        Color.Magenta // fallback color
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Preview: $themeName",
                        style = MaterialTheme.typography.headlineSmall // M3E typography standard
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back to theme creator", // Accessibility description
                            modifier = Modifier.size(24.dp) // Proper icon size
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp) // Increased spacing for better readability
        ) {
            // Color preview card - M3E compliant
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min), // Ensures minimum touch target size
                colors = CardDefaults.cardColors(
                    containerColor = parsedColor
                ),
                shape = RoundedCornerShape(20.dp) // M3E shape standard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp), // Increased padding for accessibility
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selected Color",
                        style = MaterialTheme.typography.headlineSmall, // M3E typography standard
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = hexColor,
                        style = MaterialTheme.typography.bodyLarge, // Larger text for accessibility
                        color = Color.White
                    )
                }
            }

            // Status bar preview - M3E compliant
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min), // Ensures minimum touch target size
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp) // M3E shape standard
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), // Increased padding for accessibility
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status bar icons simulation
                    repeat(3) { index ->
                        Canvas(
                            modifier = Modifier
                                .size(24.dp) // Larger icons for better visibility
                                .padding(end = 12.dp) // Increased spacing
                        ) {
                            drawCircle(
                                color = if (index == 0) parsedColor else Color.Gray,
                                radius = 10.dp.toPx() // Larger circles for better visibility
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "12:30",
                        style = MaterialTheme.typography.bodyMedium // Larger text for accessibility
                    )
                }
            }

            // Navigation bar preview - M3E compliant
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min), // Ensures minimum touch target size
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp) // M3E shape standard
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), // Increased padding for accessibility
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(3) { index ->
                        Canvas(
                            modifier = Modifier.size(36.dp) // Larger icons for better visibility
                        ) {
                            val path = Path().apply {
                                moveTo(size.width / 2f, size.height / 4f)
                                lineTo(size.width / 4f, size.height * 3f / 4f)
                                lineTo(size.width * 3f / 4f, size.height * 3f / 4f)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = if (index == 1) parsedColor else Color.Gray
                            )
                        }
                    }
                }
            }

            // Sample app UI preview - M3E compliant
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min), // Ensures minimum touch target size
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp) // M3E shape standard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp) // Increased padding for accessibility
                ) {
                    Text(
                        text = "App Preview",
                        style = MaterialTheme.typography.headlineSmall, // M3E typography standard
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp)) // Increased spacing

                    // Simulated app elements
                    Button(
                        onClick = { /* Handle click */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min), // Ensures minimum touch target size
                        colors = ButtonDefaults.buttonColors(
                            containerColor = parsedColor
                        )
                    ) {
                        Text(
                            "Sample Button",
                            style = MaterialTheme.typography.labelLarge // M3E typography standard
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp)) // Increased spacing

                    OutlinedButton(
                        onClick = { /* Handle click */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min) // Ensures minimum touch target size
                    ) {
                        Text(
                            "Outlined Button",
                            style = MaterialTheme.typography.labelLarge // M3E typography standard
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp)) // Increased spacing

                    TextField(
                        value = "Sample Input",
                        onValueChange = { /* Handle change */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min), // Ensures minimum touch target size
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }
            }

            // Apply theme button - M3E compliant
            Button(
                onClick = { /* Apply theme */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) // Ensures minimum touch target size
            ) {
                Text(
                    "Apply Theme",
                    style = MaterialTheme.typography.labelLarge // M3E typography standard
                )
            }
        }
    }
}