package com.example.resqmesh.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RescueBlueLight,
    secondary = SafeGreen,
    tertiary = EmergencyRed,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    primaryContainer = GrayPrimaryContainerDark,
    onPrimaryContainer = Color.White,
    surfaceVariant = GraySurfaceVariantDark,
    onSurfaceVariant = OnBackgroundDark
)

private val LightColorScheme = lightColorScheme(
    primary = RescueBlue,
    secondary = SafeGreen,
    tertiary = EmergencyRed,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,
    primaryContainer = GrayPrimaryContainerLight,
    onPrimaryContainer = Color.Black,
    surfaceVariant = GraySurfaceVariantLight,
    onSurfaceVariant = OnBackgroundLight
)


@Composable
fun ResQMeshTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled for strict design consistency across devices
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}