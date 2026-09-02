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

// -------------------------------------------------------------
// Cobalt Enterprise (Default)
// -------------------------------------------------------------
val CobaltDarkColorScheme = darkColorScheme(
    primary = SophisticatedPrimary,
    onPrimary = SophisticatedOnPrimary,
    primaryContainer = SophisticatedPrimaryContainer,
    onPrimaryContainer = SophisticatedOnPrimaryContainer,
    secondary = SophisticatedSecondary,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF334155),
    onSecondaryContainer = Color(0xFFF1F5F9),
    tertiary = SophisticatedTertiary,
    onTertiary = Color(0xFF0F172A),
    tertiaryContainer = Color(0xFF075985),
    onTertiaryContainer = Color(0xFFE0F2FE),
    background = SophisticatedDarkBg,
    onBackground = SophisticatedTextPrimary,
    surface = SophisticatedDarkSurface,
    onSurface = SophisticatedTextPrimary,
    surfaceVariant = SophisticatedDarkSurfaceHigh,
    onSurfaceVariant = SophisticatedTextSecondary,
    surfaceContainer = SophisticatedDarkSurface,
    surfaceContainerHigh = SophisticatedDarkSurfaceHigh,
    outline = SophisticatedDarkBorder,
    outlineVariant = SophisticatedDarkBorderLight,
    error = SophisticatedError,
    onError = Color(0xFF450A0A),
    errorContainer = SophisticatedErrorContainer,
    onErrorContainer = Color(0xFFFEE2E2)
)

val CobaltLightColorScheme = lightColorScheme(
    primary = SophisticatedLightPrimary,
    onPrimary = SophisticatedLightOnPrimary,
    primaryContainer = SophisticatedLightPrimaryContainer,
    onPrimaryContainer = SophisticatedLightOnPrimaryContainer,
    secondary = SophisticatedLightSecondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = SophisticatedLightTertiary,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF075985),
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
    onErrorContainer = Color(0xFF7F1D1D)
)

// -------------------------------------------------------------
// Emerald Field
// -------------------------------------------------------------
val EmeraldDarkColorScheme = darkColorScheme(
    primary = Color(0xFF34D399),
    onPrimary = Color(0xFF064E3B),
    primaryContainer = Color(0xFF065F46),
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Color(0xFF2DD4BF),
    onTertiary = Color(0xFF042F2E),
    tertiaryContainer = Color(0xFF115E59),
    onTertiaryContainer = Color(0xFFCCFBF1),
    background = Color(0xFF061A14),
    onBackground = Color(0xFFF0FDF4),
    surface = Color(0xFF0D2E24),
    onSurface = Color(0xFFF0FDF4),
    surfaceVariant = Color(0xFF134134),
    onSurfaceVariant = Color(0xFFA7F3D0),
    outline = Color(0xFF1F5142),
    outlineVariant = Color(0xFF2C6D5A),
    error = SophisticatedError,
    onError = Color(0xFF450A0A),
    errorContainer = SophisticatedErrorContainer,
    onErrorContainer = Color(0xFFFEE2E2)
)

val EmeraldLightColorScheme = lightColorScheme(
    primary = Color(0xFF059669),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Color(0xFF334155),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Color(0xFF0D9488),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF115E59),
    background = Color(0xFFF6FBF8),
    onBackground = Color(0xFF062319),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF062319),
    surfaceVariant = Color(0xFFECFDF5),
    onSurfaceVariant = Color(0xFF065F46),
    outline = Color(0xFFA7F3D0),
    outlineVariant = Color(0xFFD1FAE5),
    error = SophisticatedLightError,
    onError = Color(0xFFFFFFFF),
    errorContainer = SophisticatedLightErrorContainer,
    onErrorContainer = Color(0xFF7F1D1D)
)

// -------------------------------------------------------------
// Safety Amber / Industrial
// -------------------------------------------------------------
val AmberDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFBBF24),
    onPrimary = Color(0xFF451A03),
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Color(0xFFFB923C),
    onTertiary = Color(0xFF431407),
    tertiaryContainer = Color(0xFF9A3412),
    onTertiaryContainer = Color(0xFFFFEDD5),
    background = Color(0xFF18130B),
    onBackground = Color(0xFFFFFBEB),
    surface = Color(0xFF261E14),
    onSurface = Color(0xFFFFFBEB),
    surfaceVariant = Color(0xFF382C1E),
    onSurfaceVariant = Color(0xFFFDE68A),
    outline = Color(0xFF4D3D2B),
    outlineVariant = Color(0xFF6B553D),
    error = SophisticatedError,
    onError = Color(0xFF450A0A),
    errorContainer = SophisticatedErrorContainer,
    onErrorContainer = Color(0xFFFEE2E2)
)

val AmberLightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF475569),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Color(0xFFEA580C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFEDD5),
    onTertiaryContainer = Color(0xFF9A3412),
    background = Color(0xFFFFFDF7),
    onBackground = Color(0xFF1F1300),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F1300),
    surfaceVariant = Color(0xFFFEF3C7),
    onSurfaceVariant = Color(0xFF78350F),
    outline = Color(0xFFFDE68A),
    outlineVariant = Color(0xFFE2E8F0),
    error = SophisticatedLightError,
    onError = Color(0xFFFFFFFF),
    errorContainer = SophisticatedLightErrorContainer,
    onErrorContainer = Color(0xFF7F1D1D)
)

// -------------------------------------------------------------
// Royal Purple / Obsidian
// -------------------------------------------------------------
val PurpleDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF2E1065),
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Color(0xFFF472B6),
    onTertiary = Color(0xFF500724),
    tertiaryContainer = Color(0xFF9D174D),
    onTertiaryContainer = Color(0xFFFCE7F3),
    background = Color(0xFF120E1E),
    onBackground = Color(0xFFF5F3FF),
    surface = Color(0xFF1E1730),
    onSurface = Color(0xFFF5F3FF),
    surfaceVariant = Color(0xFF2D2348),
    onSurfaceVariant = Color(0xFFDDD6FE),
    outline = Color(0xFF43356C),
    outlineVariant = Color(0xFF5B4991),
    error = SophisticatedError,
    onError = Color(0xFF450A0A),
    errorContainer = SophisticatedErrorContainer,
    onErrorContainer = Color(0xFFFEE2E2)
)

val PurpleLightColorScheme = lightColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF2E1065),
    secondary = Color(0xFF475569),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Color(0xFFDB2777),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFCE7F3),
    onTertiaryContainer = Color(0xFF9D174D),
    background = Color(0xFFFAF8FF),
    onBackground = Color(0xFF170E2C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF170E2C),
    surfaceVariant = Color(0xFFF3E8FF),
    onSurfaceVariant = Color(0xFF581C87),
    outline = Color(0xFFDDD6FE),
    outlineVariant = Color(0xFFE2E8F0),
    error = SophisticatedLightError,
    onError = Color(0xFFFFFFFF),
    errorContainer = SophisticatedLightErrorContainer,
    onErrorContainer = Color(0xFF7F1D1D)
)

// -------------------------------------------------------------
// Titanium Steel
// -------------------------------------------------------------
val SteelDarkColorScheme = darkColorScheme(
    primary = Color(0xFF94A3B8),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF334155),
    onPrimaryContainer = Color(0xFFF1F5F9),
    secondary = Color(0xFF64748B),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF0F172A),
    tertiaryContainer = Color(0xFF075985),
    onTertiaryContainer = Color(0xFFE0F2FE),
    background = Color(0xFF0F141C),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1A222E),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF283445),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF38495E),
    outlineVariant = Color(0xFF4C617B),
    error = SophisticatedError,
    onError = Color(0xFF450A0A),
    errorContainer = SophisticatedErrorContainer,
    onErrorContainer = Color(0xFFFEE2E2)
)

val SteelLightColorScheme = lightColorScheme(
    primary = Color(0xFF334155),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF64748B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Color(0xFF0284C7),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF075985),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = SophisticatedLightError,
    onError = Color(0xFFFFFFFF),
    errorContainer = SophisticatedLightErrorContainer,
    onErrorContainer = Color(0xFF7F1D1D)
)

val SophisticatedDarkColorScheme = CobaltDarkColorScheme
val SophisticatedLightColorScheme = CobaltLightColorScheme

val LocalIsDarkTheme = staticCompositionLocalOf { true }
val LocalThemePreferences = staticCompositionLocalOf<ThemePreferences?> { null }

@Composable
fun ArtifyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    accentPalette: AccentPalette = AccentPalette.COBALT,
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
        else -> when (accentPalette) {
            AccentPalette.COBALT -> if (darkTheme) CobaltDarkColorScheme else CobaltLightColorScheme
            AccentPalette.EMERALD -> if (darkTheme) EmeraldDarkColorScheme else EmeraldLightColorScheme
            AccentPalette.AMBER -> if (darkTheme) AmberDarkColorScheme else AmberLightColorScheme
            AccentPalette.PURPLE -> if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
            AccentPalette.STEEL -> if (darkTheme) SteelDarkColorScheme else SteelLightColorScheme
        }
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



