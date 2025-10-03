package ru.acs.grandmap.navigation

import androidx.compose.runtime.*
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume

@Composable
fun rememberRootComponent(): RootComponent {
    val lifecycle = remember { LifecycleRegistry() }
    DisposableEffect(Unit) {
        lifecycle.resume()
        onDispose { lifecycle.destroy() }
    }
    return remember { DefaultRootComponent(DefaultComponentContext(lifecycle)) }
}
