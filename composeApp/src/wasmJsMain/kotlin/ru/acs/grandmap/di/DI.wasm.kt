package ru.acs.grandmap.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import ru.acs.grandmap.network.makeHttpClient

@Composable
actual fun WithAppDI(content: @Composable () -> Unit) {
    // На wasm DI не нужен — рендерим сразу
    content()
}

@Composable
actual fun rememberHttpClientDI(): HttpClient =
    remember { makeHttpClient(isDebug = true) }
