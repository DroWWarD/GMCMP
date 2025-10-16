package ru.acs.grandmap.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import ru.acs.grandmap.di.rememberHttpClientDI
import ru.acs.grandmap.di.rememberTokenManagerDI

@Composable
actual fun rememberRootComponent(): RootComponent {
    val lifecycle = remember { LifecycleRegistry() }
    val tokenManager = rememberTokenManagerDI()
    val httpClient = rememberHttpClientDI()

    DisposableEffect(Unit) {
        lifecycle.resume()
        onDispose { lifecycle.destroy() }
    }

    return remember(tokenManager) {
        DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            tokenManager = tokenManager,
            httpClient = httpClient
        )
    }
}