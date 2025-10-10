package ru.acs.grandmap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

// ==== корпоративные цвета (замени на свои HEX) ====
private val BrandBlue      = Color(0xFF2C3B90)
private val BrandBlueDark  = Color(0xFF0D47A1)
private val BrandGold      = Color(0xFFFFC107)
private val BrandGreen     = Color(0xFF0D496F)
private val BrandError     = Color(0xFFB00020)

private val SurfaceLight   = Color(0xFFF5F5F5)
private val SurfaceDark    = Color(0xFF121212)
private val SurfaceVariant = Color(0xFFE7E7E7)

// ---- LIGHT ----
private val LightColors = lightColorScheme(
    primary            = BrandBlue,
    onPrimary          = Color.White,
    primaryContainer   = BrandBlue.copy(alpha = .10f),
    onPrimaryContainer = BrandBlueDark,

    secondary          = BrandGreen,
    onSecondary        = Color.White,
    secondaryContainer = BrandGreen.copy(alpha = .10f),
    onSecondaryContainer = BrandGreen,

    tertiary           = BrandGold,
    onTertiary         = Color(0xFF3A2F00),

    error              = BrandError,

    background         = SurfaceLight,
    onBackground       = Color(0xFF1B1B1B),
    surface            = Color.White,
    onSurface          = Color(0xFF1B1B1B),
    surfaceVariant     = SurfaceVariant,
    onSurfaceVariant   = Color(0xFF454545),
    outline            = Color(0xFF747474)
)

// ---- DARK ----
private val DarkColors = darkColorScheme(
    primary            = BrandBlue,
    onPrimary          = Color.White,
    primaryContainer   = BrandBlueDark,
    onPrimaryContainer = Color.White,

    secondary          = BrandGreen,
    onSecondary        = Color.White,
    secondaryContainer = BrandGreen.copy(alpha = .25f),
    onSecondaryContainer = Color.White,

    tertiary           = BrandGold,
    onTertiary         = Color(0xFF2A2300),

    error              = BrandError,

    background         = SurfaceDark,
    onBackground       = Color(0xFFECECEC),
    surface            = SurfaceDark,
    onSurface          = Color(0xFFECECEC),
    surfaceVariant     = Color(0xFF2A2A2A),
    onSurfaceVariant   = Color(0xFFBDBDBD),
    outline            = Color(0xFF5F5F5F)
)

enum class ThemeMode { System, Light, Dark }

@Composable
fun GrandmAppTheme(
    mode: ThemeMode,
    content: @Composable () -> Unit
) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light  -> false
        ThemeMode.Dark   -> true
    }
    val colors = if (dark) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}

@Composable @ReadOnlyComposable
fun isDark(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light  -> false
    ThemeMode.Dark   -> true
}

object ThemePrefs {
    private val settings = Settings()
    private const val KEY = "ui.themeMode"

    fun read(): ThemeMode =
        runCatching { ThemeMode.valueOf(settings.getStringOrNull(KEY) ?: ThemeMode.System.name) }
            .getOrDefault(ThemeMode.System)

    fun write(mode: ThemeMode) {
        settings[KEY] = mode.name
    }
}