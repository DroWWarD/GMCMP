package ru.acs.grandmap.feature.work

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.extensions.compose.stack.Children
import ru.acs.grandmap.ui.PingScreen

@Composable
fun WorkContent(component: WorkComponent) {
    val stack by component.childStack.subscribeAsState()

    Children(stack) { child ->
        when (val c = child.instance) {
            is WorkComponent.Child.Dashboard ->
                WorkDashboard(onOpenDetails = { component.openDetails(it) })
            is WorkComponent.Child.Details ->
                WorkDetails(id = c.id, onBack = component::onBack)
        }
    }
}

@Composable
private fun WorkDashboard(onOpenDetails: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Dashboard (вкладка Work)", style = MaterialTheme.typography.titleLarge)

        // 1) Кнопка ПРОВАЛА – теперь над PingScreen и точно видна
        Button(onClick = { onOpenDetails(42) }) {
            Text("Открыть Details(id=42)")
        }

        // 2) PingScreen не растягиваем на всю высоту
        Card(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 320.dp)
                    .padding(16.dp)
            ) {
                PingScreen()
            }
        }
    }
}

@Composable
private fun WorkDetails(id: Int, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Details экрана Work", style = MaterialTheme.typography.titleLarge)
        Text("Параметр id = $id")
        Button(onClick = onBack) { Text("Назад") }
    }
}