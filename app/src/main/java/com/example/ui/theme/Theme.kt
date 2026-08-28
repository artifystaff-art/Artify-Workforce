package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SophisticatedDarkColorScheme = darkColorScheme(
    primary = SophisticatedPrimary,
    onPrimary = SophisticatedOnPrimary,
    primaryContainer = SophisticatedPrimaryContainer,
    onPrimaryContainer = SophisticatedOnPrimaryContainer,
    secondary = SophisticatedSecondary,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = SophisticatedTertiary,
    onTertiary = Color(0xFF492532),
    background = SophisticatedDarkBg,
    onBackground = SophisticatedTextPrimary,
    surface = SophisticatedDarkSurface,
    onSurface = SophisticatedTextPrimary,
    surfaceVariant = SophisticatedDarkSurfaceHigh,
    onSurfaceVariant = SophisticatedTextSecondary,
    surfaceContainer = SophisticatedDarkSurface,
    surfaceContainerHigh = SophisticatedDarkSurfaceHigh,
    outline = SophisticatedDarkBorder,
    outlineVariant = Color(0xFF37353C),
    error = SophisticatedError,
    onError = Color(0xFF601410),
    errorContainer = SophisticatedErrorContainer,
    onErrorContainer = Color(0xFFF9DEDC)
)

@Composable
fun ArtifyTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SophisticatedDarkColorScheme,
        typography = Typography,
        content = content
    )
}

