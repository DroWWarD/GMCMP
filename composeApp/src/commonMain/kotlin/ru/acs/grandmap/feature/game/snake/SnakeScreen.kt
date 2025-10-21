package ru.acs.grandmap.feature.game.snake

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SnakeScreen(onBack: () -> Unit) {
    val engine = remember { SnakeEngine(w = 22, h = 22) }
    var st by remember { mutableStateOf(engine.state()) }
    var running by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }

    val foodColor = MaterialTheme.colorScheme.tertiary
    val headColor = MaterialTheme.colorScheme.primary
    val bodyColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    // Игровой цикл
    LaunchedEffect(running, st.gameOver) {
        if (!running || st.gameOver) return@LaunchedEffect
        while (running && !st.gameOver) {
            // ускоряемся по мере роста
            val tickMs = (120L - (st.score * 2L)).coerceAtLeast(50L)
            delay(tickMs)
            engine.tick()
            st = engine.state()
        }
    }

    // Клавиатура (Desktop/Web/симулятор iOS)
    val keyHandler: (KeyEvent) -> Boolean = handler@ { e ->
        if (e.type != KeyEventType.KeyDown) return@handler false
        when (e.key) {
            Key.DirectionUp    -> engine.changeDir(Dir.Up)
            Key.DirectionDown  -> engine.changeDir(Dir.Down)
            Key.DirectionLeft  -> engine.changeDir(Dir.Left)
            Key.DirectionRight -> engine.changeDir(Dir.Right)
            Key.Spacebar       -> running = !running
            else -> {}
        }
        st = engine.state()
        true
    }

    Column(
        Modifier
            .fillMaxSize()
            .onPreviewKeyEvent(keyHandler)
            .focusRequester(focusRequester)
            .pointerInput(Unit) {
                // свайпы/перетаскивания — мобилки
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val (dx, dy) = dragAmount
                    if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                        if (dx > 0) engine.changeDir(Dir.Right) else engine.changeDir(Dir.Left)
                    } else {
                        if (dy > 0) engine.changeDir(Dir.Down) else engine.changeDir(Dir.Up)
                    }
                    st = engine.state()
                }
            }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        // Верхняя панель управления
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = { running = !running }, label = { Text(if (running) "Пауза" else "Старт") })
                AssistChip(onClick = { engine.restart(); running = true; st = engine.state() }, label = { Text("Заново") })
                Text("Счёт: ${st.score}")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Игровое поле
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val cell = size.minDimension / st.w.coerceAtMost(st.h) // равные клетки
                // фон — сетка (легкая)
                for (x in 0..st.w) {
                    drawRect(
                        color = Color.LightGray.copy(alpha = 0.05f),
                        topLeft = androidx.compose.ui.geometry.Offset(x * cell, 0f),
                        size = androidx.compose.ui.geometry.Size(1f, size.height)
                    )
                }
                for (y in 0..st.h) {
                    drawRect(
                        color = Color.LightGray.copy(alpha = 0.05f),
                        topLeft = androidx.compose.ui.geometry.Offset(0f, y * cell),
                        size = androidx.compose.ui.geometry.Size(size.width, 1f)
                    )
                }
                //еда
                drawRect(
                    color = foodColor,
                    topLeft = androidx.compose.ui.geometry.Offset(st.food.x * cell, st.food.y * cell),
                    size = androidx.compose.ui.geometry.Size(cell, cell)
                )

                // змейка
                st.snake.forEachIndexed { i, c ->
                    val col = if (i == st.snake.lastIndex) headColor else bodyColor
                    drawRect(
                        color = col,
                        topLeft = androidx.compose.ui.geometry.Offset(c.x * cell, c.y * cell),
                        size = androidx.compose.ui.geometry.Size(cell, cell)
                    )
                }
            }

            if (st.gameOver) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Игра окончена", style = MaterialTheme.typography.titleLarge)
                        Text("Счёт: ${st.score}", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        FilledTonalButton(onClick = { engine.restart(); st = engine.state(); running = true }) {
                            Text("Сыграть снова")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Простая экранная «крестовина» на случай отсутствия клавиш
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(onClick = { engine.changeDir(Dir.Up); st = engine.state() }) {
                    Icon(Icons.Filled.KeyboardArrowUp, null)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    FilledTonalIconButton(onClick = { engine.changeDir(Dir.Left); st = engine.state() }) {
                        Icon(Icons.Filled.KeyboardArrowLeft, null)
                    }
                    FilledTonalIconButton(onClick = { engine.changeDir(Dir.Right); st = engine.state() }) {
                        Icon(Icons.Filled.KeyboardArrowRight, null)
                    }
                }
                FilledTonalIconButton(onClick = { engine.changeDir(Dir.Down); st = engine.state() }) {
                    Icon(Icons.Filled.KeyboardArrowDown, null)
                }
            }
        }
    }
}

// Удобные пропсы для Canvas
private val EngineState.w get() = snake.maxOfOrNull { it.x }?.let { maxX ->
    maxOf(maxX + 1, 22)
} ?: 22
private val EngineState.h get() = snake.maxOfOrNull { it.y }?.let { maxY ->
    maxOf(maxY + 1, 22)
} ?: 22
