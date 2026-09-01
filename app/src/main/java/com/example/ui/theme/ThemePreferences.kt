package com.example.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AccentPalette(val displayName: String, val primaryColorHex: Long) {
    COBALT("Cobalt Enterprise", 0xFF2563EB),
    EMERALD("Emerald Field", 0xFF059669),
    AMBER("Safety Amber", 0xFFD97706),
    PURPLE("Royal Violet", 0xFF7C3AED),
    STEEL("Titanium Steel", 0xFF475569)
}

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT;

    val displayName: String
        get() = when (this) {
            SYSTEM -> "Follow System"
            DARK -> "Dark Mode (Night/Indoor)"
            LIGHT -> "Light Mode (Daylight/Outdoor)"
        }

    val shortName: String
        get() = when (this) {
            SYSTEM -> "System"
            DARK -> "Dark"
            LIGHT -> "Light"
        }
}

@Immutable
data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val highContrast: Boolean = false,
    val accentPalette: AccentPalette = AccentPalette.COBALT
)

class ThemePreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<ThemeSettings> = _settings.asStateFlow()

    private fun loadSettings(): ThemeSettings {
        val modeStr = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }

        val paletteStr = prefs.getString(KEY_ACCENT_PALETTE, AccentPalette.COBALT.name) ?: AccentPalette.COBALT.name
        val accentPalette = try {
            AccentPalette.valueOf(paletteStr)
        } catch (e: Exception) {
            AccentPalette.COBALT
        }

        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, false)
        val highContrast = prefs.getBoolean(KEY_HIGH_CONTRAST, false)

        return ThemeSettings(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            highContrast = highContrast,
            accentPalette = accentPalette
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun setAccentPalette(palette: AccentPalette) {
        prefs.edit().putString(KEY_ACCENT_PALETTE, palette.name).apply()
        _settings.value = _settings.value.copy(accentPalette = palette, dynamicColor = false)
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _settings.value = _settings.value.copy(dynamicColor = enabled)
    }

    fun setHighContrast(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
        _settings.value = _settings.value.copy(highContrast = enabled)
    }

    fun toggleDarkMode() {
        val newMode = when (_settings.value.themeMode) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
        setThemeMode(newMode)
    }

    fun toggleTheme(currentIsDark: Boolean) {
        val newMode = if (currentIsDark) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(newMode)
    }

    fun cycleThemeMode() {
        val newMode = when (_settings.value.themeMode) {
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
        }
        setThemeMode(newMode)
    }

    companion object {
        private const val PREFS_NAME = "artify_theme_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ACCENT_PALETTE = "accent_palette"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_HIGH_CONTRAST = "high_contrast"

        @Volatile
        private var instance: ThemePreferences? = null

        fun getInstance(context: Context): ThemePreferences {
            return instance ?: synchronized(this) {
                instance ?: ThemePreferences(context).also { instance = it }
            }
        }
    }
}
