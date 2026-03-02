package com.hexodus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

/**
 * Data class to hold deprecation information for a feature/tool
 */
data class DeprecationInfo(
    val message: String = "Deprecated",
    val replacement: String? = null,
    val deprecatedSince: String? = null,
    val removeInVersion: String? = null,
    val migrationGuide: String? = null
)

/**
 * DeprecationBar - A visual indicator that shows a feature is deprecated
 * with optional replacement information
 */
@Composable
fun DeprecationBar(
    deprecationInfo: DeprecationInfo,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header with warning icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = deprecationInfo.message,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Deprecated since / Remove in version info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                deprecationInfo.deprecatedSince?.let { since ->
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "Deprecated since: $since",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                deprecationInfo.removeInVersion?.let { version ->
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "Remove in: $version",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Replacement suggestion
            deprecationInfo.replacement?.let { replacement ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Use instead:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = replacement,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }
                }
            }

            // Migration guide link (if provided)
            deprecationInfo.migrationGuide?.let { guide ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📖 Migration Guide: $guide",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

/**
 * SoftDeprecationBar - A less severe deprecation notice for features in maintenance
 */
@Composable
fun SoftDeprecationBar(
    deprecationInfo: DeprecationInfo,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "⚠️ ${deprecationInfo.message}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                deprecationInfo.replacement?.let { replacement ->
                    Text(
                        text = "→ Use: $replacement",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

/**
 * VersionChip - Shows version information for a feature
 */
@Composable
fun VersionChip(
    version: String,
    type: String = "version", // "version", "api", "target"
    modifier: Modifier = Modifier
) {
    Surface(
        color = when (type) {
            "api" -> MaterialTheme.colorScheme.secondaryContainer
            "target" -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = if (type == "version") "v$version" else "$type: $version",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
