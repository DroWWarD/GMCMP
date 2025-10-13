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

// ==== Фирменные цвета ====
private val BrandBlue     = Color(0xFF2C3B90) // основной
private val BrandBlueDark = Color(0xFF1D2B6A) // для контейнеров/оверлеев в тёмной
private val BrandGreen    = Color(0xFF0D496F) // акцент/успех
private val BrandGold     = Color(0xFFFFC107) // терциарный/лейблы/чипы
private val BrandError    = Color(0xFFB00020)

// Нейтрали
private val SurfaceLight   = Color(0xFFF8F9FF) // светлый фон
private val SurfaceDark    = Color(0xFF141A29) // тёмный фон/поверхности
private val SurfaceDarkHi  = Color(0xFF1B2233) // чуть светлее для карточек
private val SurfaceVariantLight = Color(0xFFE7E7E7)
private val SurfaceVariantDark  = Color(0xFF2B3142)

// Пастельные контейнеры (под текст на контейнерах)
private val PrimaryContainerLight   = Color(0xFFDDE2FF) // пастель от BrandBlue
private val OnPrimaryContainerLight = Color(0xFF0A144B)

private val SecondaryContainerLight   = Color(0xFFCDE8F5) // пастель от BrandGreen
private val OnSecondaryContainerLight = Color(0xFF062C43)

private val TertiaryContainerLight   = Color(0xFFFFF2C3) // пастель от BrandGold
private val OnTertiaryContainerLight = Color(0xFF2A2300)

private val ErrorContainerLight   = Color(0xFFFCD8DF)
private val OnErrorContainerLight = Color(0xFF410002)

// Для тёмной темы контейнеры должны быть темнее фона, а текст — светлым
private val PrimaryContainerDark   = BrandBlueDark
private val OnPrimaryContainerDark = Color(0xFFD7DEFF)

private val SecondaryContainerDark   = Color(0xFF0E3B5C)
private val OnSecondaryContainerDark = Color(0xFFCFEAFF)

private val TertiaryContainerDark   = Color(0xFF4A3A00)
private val OnTertiaryContainerDark = Color(0xFFFFE08D)

private val ErrorContainerDark   = Color(0xFF8C1221)
private val OnErrorContainerDark = Color(0xFFFFDAD6)

// ---- LIGHT ----
private val LightColors = lightColorScheme(
    primary              = BrandBlue,
    onPrimary            = Color.White,
    primaryContainer     = PrimaryContainerLight,
    onPrimaryContainer   = OnPrimaryContainerLight,

    secondary            = BrandGreen,
    onSecondary          = Color.White,
    secondaryContainer   = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,

    tertiary             = BrandGold,
    onTertiary           = Color(0xFF2A2300),
    tertiaryContainer    = TertiaryContainerLight,
    onTertiaryContainer  = OnTertiaryContainerLight,

    error                = BrandError,
    onError              = Color.White,
    errorContainer       = ErrorContainerLight,
    onErrorContainer     = OnErrorContainerLight,

    background           = SurfaceLight,
    onBackground         = Color(0xFF1B1B1B),

    surface              = Color.White,             // непрозрачный!
    onSurface            = Color(0xFF1B1B1B),

    surfaceVariant       = SurfaceVariantLight,
    onSurfaceVariant     = Color(0xFF454545),

    outline              = Color(0xFF747474),
    // необязательно, но приятно иметь
    inverseSurface       = Color(0xFF2A2F3E),
    inverseOnSurface     = Color(0xFFF2F3F7),
    inversePrimary       = Color(0xFFB9C3FF)
)

// ---- DARK ----
private val DarkColors = darkColorScheme(
    primary              = BrandBlue,               // фирменный остаётся насыщенным
    onPrimary            = Color.White,
    primaryContainer     = PrimaryContainerDark,    // более тёмный контейнер
    onPrimaryContainer   = OnPrimaryContainerDark,

    secondary            = BrandGreen,
    onSecondary          = Color.White,
    secondaryContainer   = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,

    tertiary             = BrandGold,
    onTertiary           = Color(0xFF2A2300),
    tertiaryContainer    = TertiaryContainerDark,
    onTertiaryContainer  = OnTertiaryContainerDark,

    error                = BrandError,
    onError              = Color.White,
    errorContainer       = ErrorContainerDark,
    onErrorContainer     = OnErrorContainerDark,

    background           = SurfaceDark,
    onBackground         = Color(0xFFECECEC),

    surface              = SurfaceDarkHi,           // непрозрачный, чуть светлее background
    onSurface            = Color(0xFFE6E8EE),       // НЕ полупрозрачный — нормальный контраст

    surfaceVariant       = SurfaceVariantDark,
    onSurfaceVariant     = Color(0xFFBCC2D0),

    outline              = Color(0xFF4D5569),
    inverseSurface       = Color(0xFFF0F2FA),
    inverseOnSurface     = Color(0xFF1B2233),
    inversePrimary       = Color(0xFFB9C3FF)
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
