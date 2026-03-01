package com.hexodus.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class FeatureToggleCardTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar"
    )

    @Test
    fun featureToggleCard_enabled_light() {
        paparazzi.snapshot {
            MaterialTheme(colorScheme = lightColorScheme()) {
                FeatureToggleCard(
                    title = "System Theming",
                    description = "Apply Material You themes across all installed apps via Shizuku",
                    icon = Icons.Default.Palette,
                    isEnabled = true,
                    onToggle = {}
                )
            }
        }
    }

    @Test
    fun featureToggleCard_disabled_light() {
        paparazzi.snapshot {
            MaterialTheme(colorScheme = lightColorScheme()) {
                FeatureToggleCard(
                    title = "Network Firewall",
                    description = "Block network access per-app without root",
                    icon = Icons.Default.Security,
                    isEnabled = false,
                    onToggle = {}
                )
            }
        }
    }

    @Test
    fun featureToggleCard_enabled_dark() {
        paparazzi.snapshot {
            MaterialTheme(colorScheme = darkColorScheme()) {
                FeatureToggleCard(
                    title = "System Tuner",
                    description = "Fine-tune system-level performance and display settings",
                    icon = Icons.Default.Tune,
                    isEnabled = true,
                    onToggle = {},
                    colorIndicator = Color(0xFF3700B3)
                )
            }
        }
    }

    @Test
    fun featureToggleCard_disabled_dark() {
        paparazzi.snapshot {
            MaterialTheme(colorScheme = darkColorScheme()) {
                FeatureToggleCard(
                    title = "System Tuner",
                    description = "Fine-tune system-level performance and display settings",
                    icon = Icons.Default.Tune,
                    isEnabled = false,
                    onToggle = {}
                )
            }
        }
    }
}
