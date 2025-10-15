package ru.acs.grandmap

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import java.awt.Dimension
import java.util.prefs.Preferences

fun main() = application {
    // 1) читаем сохранённые параметры
    val (pw, ph, pmax) = WinPrefs.load()

    // 2) создаём WindowState с сохранёнными значениями
    val winState = rememberWindowState(
        placement = if (pmax) WindowPlacement.Maximized else WindowPlacement.Floating,
        position  = WindowPosition.Aligned(Alignment.Center),
        size      = DpSize(pw.dp, ph.dp)
    )

    Window(
        state = winState,
        title = "GrandmApp",
        resizable = true,
        onCloseRequest = {
            // 3) сохраняем текущее состояние перед выходом
            val isMax = winState.placement == WindowPlacement.Maximized ||
                    winState.placement == WindowPlacement.Fullscreen

            val sz: DpSize = winState.size
            WinPrefs.save(
                w = sz.width.value.toInt(),
                h = sz.height.value.toInt(),
                maximized = isMax
            )
            exitApplication()
        }
    ) {
        // 4) минимальный размер окна (AWT ComposeWindow доступен ТОЛЬКО внутри контента)
        LaunchedEffect(Unit) {
            window.minimumSize = Dimension(400, 800)
        }

        App()
    }
}

object WinPrefs {
    private val p: Preferences = Preferences.userRoot().node("grandmapp.window")

    fun save(w: Int, h: Int, maximized: Boolean) {
        p.putInt("w", w)
        p.putInt("h", h)
        p.putBoolean("max", maximized)
    }

    fun load(): Triple<Int, Int, Boolean> =
        Triple(
            p.getInt("w", 400),       // дефолтная ширина при первом запуске
            p.getInt("h", 800),        // дефолтная высота
            p.getBoolean("max", false)  // по умолчанию стартуем развёрнутыми
        )
}
