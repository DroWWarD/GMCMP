package ru.acs.grandmap.feature.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.acs.grandmap.feature.game.snake.SnakeScreen

@Composable
fun GameScreen(component: GameComponent) {
    when (component.mode.value) {
        is Mode.Menu -> GameMenu(onOpenSnake = component::openSnake)
        is Mode.Snake -> SnakeScreen(onBack = component::back)
    }
}

@Composable
private fun GameMenu(onOpenSnake: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Игры", style = MaterialTheme.typography.headlineSmall)
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onOpenSnake() }
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Змейка", style = MaterialTheme.typography.titleLarge)
                    Text("Классика. Собирай яблоки, не врежься в стены и себя.")
                }
                FilledTonalButton(onClick = onOpenSnake) { Text("Играть") }
            }
        }
    }
}
