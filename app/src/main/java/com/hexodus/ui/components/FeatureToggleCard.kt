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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * FeatureToggleCard - A unified component for displaying and toggling features
 */
@Composable
fun FeatureToggleCard(
    title: String,
    description: String,
    icon: ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    colorIndicator: androidx.compose.ui.graphics.Color? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min), // Ensures minimum touch target size
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp) // M3E shape standard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // Increased padding for accessibility
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with color indicator
            Box(
                modifier = Modifier
                    .size(56.dp) // Larger icon for better visibility
                    .clip(RoundedCornerShape(16.dp)), // M3E shape standard
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            colorIndicator ?: MaterialTheme.colorScheme.primaryContainer
                        )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp) // Proper icon size
                )
            }

            Spacer(modifier = Modifier.width(20.dp)) // Increased spacing

            // Title and description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge, // Larger title for better readability
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium, // Larger body text for accessibility
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3, // Limit description lines
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Toggle switch with improved accessibility
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                modifier = Modifier
                    .size(56.dp, 32.dp), // Proper touch target size
                    colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

/**
 * CategoryHeader - A header for organizing features by category
 */
@Composable
fun CategoryHeader(
    title: String,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}