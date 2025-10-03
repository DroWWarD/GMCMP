package ru.acs.grandmap

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import androidx.compose.ui.window.ComposeViewport
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Ждём, пока Skiko загрузит wasm (иначе — пустой экран)
    onWasmReady {
        // важно: совпадает с id в index.html
        ComposeViewport(content = {
            App()
        })
    }
}
