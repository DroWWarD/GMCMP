package ru.acs.grandmap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

// -------------------------------------------------------------
// Мэппинг CSS-переменных Figma → токены Compose
// -------------------------------------------------------------
// LIGHT (из :root)
private val L_Background            = Color(0xFFFFFFFF)          // --background
private val L_Foreground            = Color(0xFF1B1B1B)          // oklch(0.145 0 0) ~ тёмный нейтральный
private val L_Card                  = Color(0xFFFFFFFF)          // --card
private val L_Primary               = Color(0xFF2C3B90)          // --corporate-primary
private val L_Secondary             = Color(0xFF0D496F)          // --corporate-secondary
private val L_Tertiary              = Color(0xFF4A5BA3)          // --corporate-accent
private val L_AccentBg              = Color(0xFFE9EBEF)          // --accent
private val L_Muted                 = Color(0xFFECECF0)          // --muted
private val L_MutedForeground       = Color(0xFF717182)          // --muted-foreground
private val L_Error                 = Color(0xFFEF4444)          // --corporate-error
private val L_Border                = Color(0xFFE0E0E0)          // --border rgba(0,0,0,0.1) → ближний непрозр. серый
private val L_TextPrimary           = Color(0xFF2C3B90)          // --text-primary
private val L_TextSecondary         = Color(0xBF2C3B90)          // --text-secondary (rgba 0.75)
private val L_TextMuted             = Color(0x802C3B90)          // --text-muted (rgba 0.5)

// Доп. корпоративные (для Extended)
private val L_Success               = Color(0xFF22C55E)          // --corporate-success
private val L_Warning               = Color(0xFFF59E0B)          // --corporate-warning
private val L_Destructive           = Color(0xFFD4183D)          // --destructive
private val L_GlassBg               = Color(0x1AFFFFFF)          // --glass-background (10% белый)
private val L_GlassBorder           = Color(0x33FFFFFF)          // --glass-border (20% белый)
private val L_HeaderGradStart       = Color(0xFF2C3B90)
private val L_HeaderGradMid         = Color(0xFF0D496F)
private val L_HeaderGradEnd         = Color(0xFF4A5BA3)

// DARK (из .dark)
private val D_Background            = Color(0xFF0F1419)          // из градиента начала
private val D_Surface               = Color(0xFF1A1F2E)          // из градиента конца / --card
private val D_OnBackground          = Color(0xFFE1E5FF)          // --text-primary
private val D_Primary               = Color(0xFF4A5BA3)          // --corporate-primary (dark)
private val D_Secondary             = Color(0xFF1A5A85)          // --corporate-secondary (dark)
private val D_Tertiary              = Color(0xFF6B7EC8)          // --corporate-accent (dark)
private val D_Error                 = Color(0xFFDC2626)          // --corporate-error (dark)
private val D_Muted                 = Color(0xFF353A44)          // oklch(0.269...) близкий нейтральный
private val D_OnMuted               = Color(0xFFB9C0D4)          // oklch(0.708...) светло-серый текст
private val D_Border                = Color(0xFF2A3140)          // --sidebar-border / --border (тёмный)
private val D_HeaderGradStart       = Color(0xFF1A5A85)
private val D_HeaderGradMid         = Color(0xFF2C3B90)
private val D_HeaderGradEnd         = Color(0xFF6B7EC8)

// Доп. корпоративные (для Extended)
private val D_Success               = Color(0xFF16A34A)
private val D_Warning               = Color(0xFFD97706)
private val D_Destructive           = Color(0xFFDC2626)
private val D_GlassBg               = Color(0x0DFFFFFF)          // 5% белый
private val D_GlassBorder           = Color(0x1AFFFFFF)          // 10% белый

// -------------------------------------------------------------
// Material3 ColorScheme
// -------------------------------------------------------------
private val LightColors = lightColorScheme(
    primary              = L_Primary,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFDDE2FF),    // пастель к primary
    onPrimaryContainer   = Color(0xFF101B4B),

    secondary            = L_Secondary,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFCDE8F5),    // пастель к secondary
    onSecondaryContainer = Color(0xFF062C43),

    tertiary             = L_Tertiary,
    onTertiary           = Color(0xFF241600),
    tertiaryContainer    = Color(0xFFFFF2C3),
    onTertiaryContainer  = Color(0xFF2A2300),

    error                = L_Error,
    onError              = Color.White,
    errorContainer       = Color(0xFFFCD8DF),
    onErrorContainer     = Color(0xFF410002),

    background           = L_Background,
    onBackground         = L_Foreground,

    surface              = L_Card,
    onSurface            = L_Foreground,

    surfaceVariant       = L_Muted,
    onSurfaceVariant     = L_MutedForeground,

    outline              = L_Border,
    inverseSurface       = Color(0xFF2A2F3E),
    inverseOnSurface     = Color(0xFFF2F3F7),
    inversePrimary       = Color(0xFFB9C3FF)
)

private val DarkColors = darkColorScheme(
    primary              = D_Primary,
    onPrimary            = Color(0xFF0D1220),
    primaryContainer     = Color(0xFF2A3566),
    onPrimaryContainer   = Color(0xFFDDE2FF),

    secondary            = D_Secondary,
    onSecondary          = Color(0xFFE8F4FF),
    secondaryContainer   = Color(0xFF0E3B5C),
    onSecondaryContainer = Color(0xFFCFEAFF),

    tertiary             = D_Tertiary,
    onTertiary           = Color(0xFF0E0A00),
    tertiaryContainer    = Color(0xFF3B3763),
    onTertiaryContainer  = Color(0xFFFFE08D),

    error                = D_Error,
    onError              = Color.White,
    errorContainer       = Color(0xFF8C1221),
    onErrorContainer     = Color(0xFFFFDAD6),

    background           = D_Background,
    onBackground         = D_OnBackground,

    surface              = D_Surface,          // непрозрачная!
    onSurface            = D_OnBackground,     // читабельный контраст

    surfaceVariant       = D_Muted,
    onSurfaceVariant     = D_OnMuted,

    outline              = D_Border,
    inverseSurface       = Color(0xFFF0F2FA),
    inverseOnSurface     = Color(0xFF1B2233),
    inversePrimary       = Color(0xFFB9C3FF)
)

// -------------------------------------------------------------
// Extended (то, чего нет напрямую в ColorScheme, но есть в твоей палитре)
// -------------------------------------------------------------
data class ExtendedColors(
    val success: Color,
    val warning: Color,
    val destructive: Color,
    val textPrimaryBrand: Color,
    val textSecondaryBrand: Color,
    val textMutedBrand: Color,
    val glassBackground: Color,
    val glassBorder: Color,
    val headerBrush: Brush,
    val fabBrush: Brush
)

private val LocalExtendedColors = staticCompositionLocalOf<ExtendedColors> {
    error("No ExtendedColors provided")
}

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

private fun headerBrushLight() = Brush.linearGradient(
    colors = listOf(L_HeaderGradStart, L_HeaderGradMid, L_HeaderGradEnd),
    tileMode = TileMode.Clamp
)

private fun headerBrushDark() = Brush.linearGradient(
    colors = listOf(D_HeaderGradStart, D_HeaderGradMid, D_HeaderGradEnd),
    tileMode = TileMode.Clamp
)

// Можно использовать тот же градиент для FAB
private fun fabBrushLight() = headerBrushLight()
private fun fabBrushDark() = headerBrushDark()

// -------------------------------------------------------------
// Тема
// -------------------------------------------------------------
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
    val extended = if (dark) {
        ExtendedColors(
            success            = D_Success,
            warning            = D_Warning,
            destructive        = D_Destructive,
            textPrimaryBrand   = D_OnBackground, // #e1e5ff
            textSecondaryBrand = Color(0xD9E1E5FF), // 85%
            textMutedBrand     = Color(0xA6E1E5FF), // 65%
            glassBackground    = D_GlassBg,
            glassBorder        = D_GlassBorder,
            headerBrush        = headerBrushDark(),
            fabBrush           = fabBrushDark()
        )
    } else {
        ExtendedColors(
            success            = L_Success,
            warning            = L_Warning,
            destructive        = L_Destructive,
            textPrimaryBrand   = L_TextPrimary,
            textSecondaryBrand = L_TextSecondary,
            textMutedBrand     = L_TextMuted,
            glassBackground    = L_GlassBg,
            glassBorder        = L_GlassBorder,
            headerBrush        = headerBrushLight(),
            fabBrush           = fabBrushLight()
        )
    }

    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}

@Composable
@ReadOnlyComposable
fun isDark(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light  -> false
    ThemeMode.Dark   -> true
}

// -------------------------------------------------------------
// Хранилище пользовательского выбора
// -------------------------------------------------------------
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
