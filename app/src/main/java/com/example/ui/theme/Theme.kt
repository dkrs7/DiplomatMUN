package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = UNBlueLight,
    onPrimary = Color.Black,
    primaryContainer = UNBlueDark,
    onPrimaryContainer = Color.White,
    secondary = DiplomaticGoldLight,
    onSecondary = Color.Black,
    secondaryContainer = DiplomaticGold,
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF64B5F6),
    background = DiplomaticNavy,
    surface = DiplomaticNavySurface,
    surfaceVariant = DiplomaticNavyElevated,
    onBackground = Color(0xFFECEFF4),
    onSurface = Color(0xFFECEFF4),
    onSurfaceVariant = Color(0xFFC4D1E0),
    outline = Color(0xFF3B4E6B)
)

private val LightColorScheme = lightColorScheme(
    primary = UNBlue,
    onPrimary = Color.White,
    primaryContainer = UNBlueContainer,
    onPrimaryContainer = UNOnBlueContainer,
    secondary = DiplomaticGold,
    onSecondary = Color.White,
    secondaryContainer = DiplomaticGoldContainer,
    onSecondaryContainer = Color(0xFF4A3400),
    tertiary = UNBlueDark,
    background = CommitteeParchment,
    surface = CommitteeSurface,
    surfaceVariant = Color(0xFFEFF4FA),
    onBackground = Color(0xFF0F1A2A),
    onSurface = Color(0xFF0F1A2A),
    onSurfaceVariant = Color(0xFF435368),
    outline = CommitteeBorder
)

@Composable
fun DiplomatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
