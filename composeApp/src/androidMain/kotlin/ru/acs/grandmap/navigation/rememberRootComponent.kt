package ru.acs.grandmap.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.retainedComponent
import ru.acs.grandmap.di.rememberHttpClientDI
import ru.acs.grandmap.di.rememberTokenManagerDI

@Composable
actual fun rememberRootComponent(): RootComponent {
    // Правильный способ получить Activity в Compose
    val activity: ComponentActivity = checkNotNull(LocalActivity.current) {
        "LocalActivity is null (are you in a preview?)"
    } as ComponentActivity

    val tokenManager = rememberTokenManagerDI()
    val httpClient  = rememberHttpClientDI()

    return remember(activity, tokenManager, httpClient) {
        activity.retainedComponent { cc ->
            DefaultRootComponent(
                componentContext = cc,
                tokenManager     = tokenManager,
                httpClient       = httpClient
            )
        }
    }
}