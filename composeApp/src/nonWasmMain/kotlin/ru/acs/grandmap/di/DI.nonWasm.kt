package ru.acs.grandmap.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import org.koin.compose.KoinContext
import org.koin.core.context.startKoin
import org.koin.compose.koinInject
import ru.acs.grandmap.di.appModule

@Composable
actual fun WithAppDI(content: @Composable () -> Unit) {
    // стартуем Koin ровно один раз
    remember { startKoin { modules(appModule) } }
    KoinContext { content() }
}

@Composable
actual fun rememberHttpClientDI(): HttpClient = koinInject()
