package ru.acs.grandmap.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.acs.grandmap.core.AppResult
import ru.acs.grandmap.di.rememberAuthRepository


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PingScreen() {
    val repo = rememberAuthRepository()
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {
                scope.launch {
                    loading = true; result = null; error = null
                    when (val r = repo.ping()) {
                        is AppResult.Ok  -> result = r.value
                        is AppResult.Err -> error  = r.message
                    }
                    loading = false
                }
            },
            enabled = !loading
        ) { Text(if (loading) "Пинг..." else "Ping") }

        result?.let { Text("Ответ: $it") }
        error?.let { Text("Ошибка: $it", color = MaterialTheme.colorScheme.error) }
    }
}
