package ru.acs.grandmap

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Ждём, пока Skiko загрузит wasm (иначе — пустой экран)
    onWasmReady {
        enableFetchCredentialsInclude()
        ComposeViewport(content = {
            App()
        })
    }
}
