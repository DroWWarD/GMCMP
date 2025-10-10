package ru.acs.grandmap

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import org.jetbrains.compose.ui.tooling.preview.Preview

import ru.acs.grandmap.di.WithAppDI
import ru.acs.grandmap.ui.RootScaffold
import ru.acs.grandmap.ui.theme.GrandmAppTheme
import ru.acs.grandmap.ui.theme.ThemeMode
import ru.acs.grandmap.ui.theme.ThemePrefs
import ru.acs.grandmap.ui.theme.isDark

@Composable
@Preview
fun App() {
    WithAppDI {
        // Тема:
        var themeMode by remember { mutableStateOf(ThemePrefs.read()) }
        val systemDark = isDark(ThemeMode.System)
        val actualDark = isDark(themeMode)
        LaunchedEffect(themeMode) { ThemePrefs.write(themeMode) }


        val onToggleTheme = remember(systemDark, themeMode) {
            {
                themeMode = when (themeMode) {
                    ThemeMode.System -> if (systemDark) ThemeMode.Light else ThemeMode.Dark
                    ThemeMode.Light  -> ThemeMode.Dark
                    ThemeMode.Dark   -> ThemeMode.Light
                }
            }
        }

        GrandmAppTheme(mode = themeMode) {
            RootScaffold(
                onToggleTheme = onToggleTheme,
                dark = actualDark
            )
        }
    }
}
