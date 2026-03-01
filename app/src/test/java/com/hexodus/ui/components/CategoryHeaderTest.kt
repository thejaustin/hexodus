package com.hexodus.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class CategoryHeaderTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar"
    )

    @Test
    fun categoryHeader_with_icon_light() {
        paparazzi.snapshot {
            MaterialTheme(colorScheme = lightColorScheme()) {
                CategoryHeader(
                    title = "Core Features",
                    icon = Icons.Default.Dashboard
                )
            }
        }
    }

    @Test
    fun categoryHeader_without_icon_light() {
        paparazzi.snapshot {
            MaterialTheme(colorScheme = lightColorScheme()) {
                CategoryHeader(title = "Advanced Settings")
            }
        }
    }

    @Test
    fun categoryHeader_with_icon_dark() {
        paparazzi.snapshot {
            MaterialTheme(colorScheme = darkColorScheme()) {
                CategoryHeader(
                    title = "System Configuration",
                    icon = Icons.Default.Settings
                )
            }
        }
    }
}
