package ru.acs.grandmap.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import ru.acs.grandmap.core.auth.TokenManager

@Composable
actual fun WithAppDI(content: @Composable () -> Unit) {
    remember(Unit) {
        initLoggingOnce()
        runCatching {
            startKoin {
                modules(
                    storageModule,   // Settings + TokenStorage
                    httpModule,      // plain + authed HttpClient
                    authModule,      // AuthApi(plain) + TokenManager
                    repoModule       // репозитории
                )
            }
        }
    }
    KoinContext { content() }
}

@Composable
actual fun rememberHttpClientDI(): HttpClient = koinInject()
@Composable
actual fun rememberTokenManagerDI(): TokenManager = koinInject()
