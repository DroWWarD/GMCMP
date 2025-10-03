package ru.acs.grandmap.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import ru.acs.grandmap.network.ping
import ru.acs.grandmap.di.rememberHttpClientDI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PingScreen() {
    val client = rememberHttpClientDI()

    var run by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("Нажми PING") }

    // Без прямого использования kotlinx.coroutines.launch:
    LaunchedEffect(run) {
        if (run) {
            loading = true
            result = runCatching { ping(client) }.getOrElse { "Ошибка: ${it.message}" }
            loading = false
            run = false
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("KMP Ping") }) }) { padd ->
        Column(
            Modifier.fillMaxSize().padding(padd).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                enabled = !loading,
                onClick = { run = true }
            ) { Text(if (loading) "..." else "PING") }

            Text(result)
        }
    }
}
