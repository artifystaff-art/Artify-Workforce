package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val SophisticatedDarkColorScheme = darkColorScheme(
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

val SophisticatedLightColorScheme = lightColorScheme(
    primary = SophisticatedLightPrimary,
    onPrimary = SophisticatedLightOnPrimary,
    primaryContainer = SophisticatedLightPrimaryContainer,
    onPrimaryContainer = SophisticatedLightOnPrimaryContainer,
    secondary = SophisticatedLightSecondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = SophisticatedLightTertiary,
    onTertiary = Color(0xFFFFFFFF),
    background = SophisticatedLightBg,
    onBackground = SophisticatedLightTextPrimary,
    surface = SophisticatedLightSurface,
    onSurface = SophisticatedLightTextPrimary,
    surfaceVariant = SophisticatedLightSurfaceHigh,
    onSurfaceVariant = SophisticatedLightTextSecondary,
    surfaceContainer = SophisticatedLightSurface,
    surfaceContainerHigh = SophisticatedLightSurfaceHigh,
    outline = SophisticatedLightBorder,
    outlineVariant = SophisticatedLightBorderLight,
    error = SophisticatedLightError,
    onError = Color(0xFFFFFFFF),
    errorContainer = SophisticatedLightErrorContainer,
    onErrorContainer = Color(0xFF410E0B)
)

val LocalIsDarkTheme = staticCompositionLocalOf { true }
val LocalThemePreferences = staticCompositionLocalOf<ThemePreferences?> { null }

@Composable
fun ArtifyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    },
    themePreferences: ThemePreferences? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SophisticatedDarkColorScheme
        else -> SophisticatedLightColorScheme
    }

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalThemePreferences provides themePreferences
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}


