package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmeraldPrimary,
    onPrimary = DarkEmeraldOnPrimary,
    primaryContainer = DarkEmeraldContainer,
    onPrimaryContainer = DarkEmeraldOnContainer,
    secondary = DarkGoldSecondary,
    onSecondary = DarkGoldOnSecondary,
    secondaryContainer = DarkGoldContainer,
    onSecondaryContainer = DarkGoldOnContainer,
    tertiary = DarkNavyTertiary,
    onTertiary = DarkNavyOnTertiary,
    tertiaryContainer = DarkNavyContainer,
    onTertiaryContainer = DarkNavyOnContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = SaudiEmeraldPrimary,
    onPrimary = SaudiEmeraldOnPrimary,
    primaryContainer = SaudiEmeraldContainer,
    onPrimaryContainer = SaudiEmeraldOnContainer,
    secondary = DesertGoldSecondary,
    onSecondary = DesertGoldOnSecondary,
    secondaryContainer = DesertGoldContainer,
    onSecondaryContainer = DesertGoldOnContainer,
    tertiary = AcademicNavyTertiary,
    onTertiary = AcademicNavyOnTertiary,
    tertiaryContainer = AcademicNavyContainer,
    onTertiaryContainer = AcademicNavyOnContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
