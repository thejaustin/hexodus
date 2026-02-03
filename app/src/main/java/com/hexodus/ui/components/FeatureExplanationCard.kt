package com.hexodus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * FeatureExplanationCard - A card that explains a feature with visual indicators
 */
@Composable
fun FeatureExplanationCard(
    title: String,
    description: String,
    icon: ImageVector,
    colorIndicator: Color,
    status: FeatureStatus = FeatureStatus.Available,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null)
            Modifier
                .clickable { onClick() }
                .height(IntrinsicSize.Min) // Ensures minimum touch target size
            else
            Modifier
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
                        .background(colorIndicator),
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp) // Proper icon size
                )
            }

            Spacer(modifier = Modifier.width(20.dp)) // Increased spacing

            // Title and description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge, // Larger title for better readability
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    StatusIndicator(status = status)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium, // Larger body text for accessibility
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3, // Limit description lines
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * StatusIndicator - Visual indicator for feature status
 */
@Composable
fun StatusIndicator(status: FeatureStatus) {
    when (status) {
        FeatureStatus.Available -> {
            Box(
                modifier = Modifier
                    .size(24.dp) // Larger size for better visibility
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Available",
                    tint = Color.Green,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        FeatureStatus.Enabled -> {
            Box(
                modifier = Modifier
                    .size(24.dp) // Larger size for better visibility
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ToggleOn,
                    contentDescription = "Enabled",
                    tint = Color.Blue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        FeatureStatus.Disabled -> {
            Box(
                modifier = Modifier
                    .size(24.dp) // Larger size for better visibility
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ToggleOff,
                    contentDescription = "Disabled",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        FeatureStatus.RequiresSetup -> {
            Box(
                modifier = Modifier
                    .size(24.dp) // Larger size for better visibility
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Requires Setup",
                    tint = Color.Yellow,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * FeatureStatus - Enum for feature statuses
 */
enum class FeatureStatus {
    Available,
    Enabled,
    Disabled,
    RequiresSetup
}

/**
 * FeatureCategoryCard - A card for organizing features by category
 */
@Composable
fun FeatureCategoryCard(
    title: String,
    icon: ImageVector,
    featureCount: Int,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null)
            Modifier
                .clickable { onClick() }
                .height(IntrinsicSize.Min) // Ensures minimum touch target size
            else
            Modifier
                .height(IntrinsicSize.Min), // Ensures minimum touch target size
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(28.dp) // M3E shape standard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp), // Increased padding for accessibility
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title, // Accessibility description
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp) // Larger icon for better visibility
            )

            Spacer(modifier = Modifier.width(20.dp)) // Increased spacing

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge, // Larger title for better readability
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$featureCount features",
                    style = MaterialTheme.typography.bodyMedium, // Larger text for accessibility
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp) // Larger icon for better visibility
            )
        }
    }
}