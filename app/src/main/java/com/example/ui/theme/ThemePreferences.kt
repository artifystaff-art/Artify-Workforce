package com.example.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    val dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    val highContrast: Boolean = false
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

        val defaultDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, defaultDynamic)
        val highContrast = prefs.getBoolean(KEY_HIGH_CONTRAST, false)

        return ThemeSettings(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            highContrast = highContrast
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
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
