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
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnStop
import kotlinx.coroutines.delay

@Composable
fun SnakeScreen(component: SnakeComponent) {
    val engine = remember { SnakeEngine(w = 22, h = 22) }

    // 2) Saver для EngineState (только примитивы → переживёт переключение вкладок/рекреацию)
    val engineStateSaver = remember {
        listSaver<EngineState, Any>(
            save = { st ->
                listOf(
                    st.snake.flatMap { listOf(it.x, it.y) }.toIntArray(), // IntArray
                    st.food.x, st.food.y,                                 // Int, Int
                    st.dir.ordinal,                                       // Int
                    st.score,                                             // Int
                    st.gameOver                                           // Boolean
                )
            },
            restore = { l ->
                val pairs = l[0] as IntArray
                val snake = pairs.asList().chunked(2).map { Cell(it[0], it[1]) }
                val food  = Cell(l[1] as Int, l[2] as Int)
                val dir   = Dir.values()[l[3] as Int]
                val score = l[4] as Int
                val over  = l[5] as Boolean
                EngineState(snake = snake, food = food, dir = dir, score = score, gameOver = over)
            }
        )
    }
    // 3) Сохраняем/восстанавливаем состояние змейки
    var st by rememberSaveable(stateSaver = engineStateSaver) { mutableStateOf(engine.state()) }

    // 4) Флаг «идёт игра» тоже сохраним
    var running by rememberSaveable { mutableStateOf(true) }

    // 5) При повторном входе на экран — восстановим движок из сохранённого состояния
    LaunchedEffect(engine) {
        engine.restore(st)
    }

    // 6) Если экран уходит из композиции (переключили вкладку/ушли назад) — ставим паузу
    DisposableEffect(Unit) {
        onDispose {
            running = false           // при возвращении будет "на паузе"
            st = engine.state()       // фиксируем последний снимок
        }
    }

    val focusRequester = remember { FocusRequester() }

    val foodColor = MaterialTheme.colorScheme.tertiary
    val headColor = MaterialTheme.colorScheme.primary
    val bodyColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

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
            .onFocusChanged { if (!it.isFocused) running = false }
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
        LaunchedEffect(running) {
            if (running && !st.gameOver) focusRequester.requestFocus()
        }
        // Пауза при сворачивании/уходе из приложения (Android/iOS/WASM/частично Desktop)
        LaunchedEffect(component.lifecycle) {
            component.lifecycle.doOnPause {
                // приложение потеряло активность — ставим на паузу и сохраняем снимок
                running = false
                st = engine.state()
            }
            component.lifecycle.doOnStop {
                // ушли в бэкграунд — тоже на паузу и фиксируем состояние
                running = false
                st = engine.state()
            }
        }

        // Верхняя панель управления
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(onClick = component::back) { Icon(Icons.Filled.ArrowBack, null) }
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
                for (x in 0..engine.w) {
                    drawRect(
                        color = gridColor,
                        topLeft = androidx.compose.ui.geometry.Offset(x * cell, 0f),
                        size = androidx.compose.ui.geometry.Size(1f, size.height)
                    )
                }
                for (y in 0..engine.h) {
                    drawRect(
                        color = gridColor,
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
            if (!running && !st.gameOver) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Пауза", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Нажмите «Пробел» или кнопку ниже, чтобы продолжить",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                        FilledTonalButton(
                            onClick = { running = true }
                        ) { Text("Продолжить") }
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
