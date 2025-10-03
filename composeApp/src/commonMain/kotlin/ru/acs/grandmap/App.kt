package ru.acs.grandmap

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview

import ru.acs.grandmap.di.WithAppDI
import ru.acs.grandmap.ui.RootScaffold

@Composable
@Preview
fun App() {
    WithAppDI {
        MaterialTheme { RootScaffold() }
    }
}