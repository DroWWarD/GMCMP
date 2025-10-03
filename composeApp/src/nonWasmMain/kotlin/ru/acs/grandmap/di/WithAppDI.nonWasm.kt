package ru.acs.grandmap.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

@Composable
actual fun WithAppDI(content: @Composable () -> Unit) {
    remember(Unit) {
        initLoggingOnce()
        // startKoin бросает KoinAppAlreadyStartedException при повторном старте.
        // runCatching проглатывает её — запускаем ровно один раз.
        runCatching {
            startKoin { modules(networkModule, repoModule) }
        }
    }
    KoinContext { content() }
}

@Composable
actual fun rememberHttpClientDI(): HttpClient = koinInject()
