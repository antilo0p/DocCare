package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DocCarePrimaryDark,
    onPrimary = DocCareOnPrimaryDark,
    primaryContainer = DocCarePrimaryContainerDark,
    onPrimaryContainer = DocCareOnPrimaryContainerDark,
    secondary = DocCareSecondary,
    onSecondary = DocCareOnSecondary,
    background = DocCareBackgroundDark,
    onBackground = DocCareOnBackgroundDark,
    surface = DocCareSurfaceDark,
    onSurface = DocCareOnSurfaceDark,
    surfaceVariant = DocCareSurfaceDark,
    onSurfaceVariant = DocCareOnSurfaceDark,
    outline = DocCareOutline
)

private val LightColorScheme = lightColorScheme(
    primary = DocCarePrimary,
    onPrimary = DocCareOnPrimary,
    primaryContainer = DocCarePrimaryContainer,
    onPrimaryContainer = DocCareOnPrimaryContainer,
    secondary = DocCareSecondary,
    onSecondary = DocCareOnSecondary,
    secondaryContainer = DocCareSecondaryContainer,
    onSecondaryContainer = DocCareOnSecondaryContainer,
    background = DocCareBackground,
    onBackground = DocCareOnBackground,
    surface = DocCareSurface,
    onSurface = DocCareOnSurface,
    surfaceVariant = DocCareSurfaceVariant,
    onSurfaceVariant = DocCareOnSurfaceVariant,
    outline = DocCareOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // For senior readability, we use high-contrast curated colorScheme by default
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
