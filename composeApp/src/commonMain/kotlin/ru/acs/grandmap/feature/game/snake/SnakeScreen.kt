package ru.acs.grandmap.feature.game.snake

import TopBarController
import TopBarSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.min
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnStop
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.min

@Composable
fun SnakeScreen(
    component: SnakeComponent,
    topBar: TopBarController
) {
    LaunchedEffect(Unit) {
        topBar.update(
            TopBarSpec(
                title = "Игра \"Змейка\"",
                onBack = component::back
            )
        )
    }
    val engine = remember { SnakeEngine(w = 22, h = 22) }

    val engineStateSaver = remember {
        listSaver<EngineState, Any>(
            save = { st ->
                listOf(
                    st.snake.flatMap { listOf(it.x, it.y) }.toIntArray(),
                    st.food.x, st.food.y,
                    st.dir.ordinal,
                    st.score,
                    st.gameOver
                )
            },
            restore = { l ->
                val pairs = l[0] as IntArray
                val snake = pairs.asList().chunked(2).map { Cell(it[0], it[1]) }
                val food = Cell(l[1] as Int, l[2] as Int)
                val dir = Dir.values()[l[3] as Int]
                val score = l[4] as Int
                val over = l[5] as Boolean
                EngineState(snake = snake, food = food, dir = dir, score = score, gameOver = over)
            }
        )
    }
    var st by rememberSaveable(stateSaver = engineStateSaver) { mutableStateOf(engine.state()) }
    var running by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(engine) { engine.restore(st) }

    DisposableEffect(Unit) {
        onDispose {
            running = false
            st = engine.state()
            topBar.clear()
        }
    }

    val isTouch = isTouchDevice()
    val focusRequester = remember { FocusRequester() }

    val foodColor = MaterialTheme.colorScheme.tertiary
    val headColor = MaterialTheme.colorScheme.primary
    val bodyColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

    // Игровой цикл
    LaunchedEffect(running, st.gameOver) {
        if (!running || st.gameOver) return@LaunchedEffect
        while (running && !st.gameOver) {
            val tickMs = (120L - (st.score * 2L)).coerceAtLeast(50L)
            delay(tickMs)
            engine.tick()
            st = engine.state()
        }
    }

    // Клавиатура — только для не-тач (Desktop/Web без тача)
    val keyHandler: (KeyEvent) -> Boolean = handler@{ e ->
        if (e.type != KeyEventType.KeyDown) return@handler false
        when (e.key) {
            Key.DirectionUp -> engine.changeDir(Dir.Up)
            Key.DirectionDown -> engine.changeDir(Dir.Down)
            Key.DirectionLeft -> engine.changeDir(Dir.Left)
            Key.DirectionRight -> engine.changeDir(Dir.Right)
            Key.Spacebar -> running = !running
            else -> return@handler false
        }
        st = engine.state()
        true
    }

    Column(
        Modifier
            .fillMaxSize()
            // клавиатура и фокус — только на Desktop/Web без тача
            .then(
                if (!isTouch)
                    Modifier
                        .onPreviewKeyEvent(keyHandler)
                        .focusRequester(focusRequester)
                        .onFocusChanged { if (!it.isFocused) running = false }
                else Modifier
            )
            // жесты — только на тач-устройствах
            .then(
                if (isTouch)
                    Modifier.pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val (dx, dy) = dragAmount
                            if (abs(dx) > abs(dy)) {
                                if (dx > 0) engine.changeDir(Dir.Right) else engine.changeDir(Dir.Left)
                            } else {
                                if (dy > 0) engine.changeDir(Dir.Down) else engine.changeDir(Dir.Up)
                            }
                            st = engine.state()
                        }
                    }
                else Modifier
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // запрос фокуса — только если есть клавы
        if (!isTouch) {
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
            LaunchedEffect(running) { if (running && !st.gameOver) focusRequester.requestFocus() }
        }

        // Пауза при сворачивании/уходе из приложения
        LaunchedEffect(component.lifecycle) {
            component.lifecycle.doOnPause { running = false; st = engine.state() }
            component.lifecycle.doOnStop { running = false; st = engine.state() }
        }

        // Верхняя панель

        Text("Счёт: ${st.score}")
        Spacer(Modifier.height(12.dp))

        // Игровое поле: ВСЕГДА целиком помещается (квадрат по min ширины/высоты оставшегося места)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // берём всё оставшееся по высоте
            contentAlignment = Alignment.TopStart
        ) {
            val boardSize = min(maxWidth, maxHeight)
            Box(
                modifier = Modifier
                    .size(boardSize) // строгий квадрат
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val cell = size.minDimension / st.w.coerceAtMost(st.h)

                    // сетка
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

                    // еда
                    drawRect(
                        color = foodColor,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            st.food.x * cell,
                            st.food.y * cell
                        ),
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

                // Оверлеи
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
                            FilledTonalButton(onClick = {
                                engine.restart(); st = engine.state(); running = true
                            }) {
                                Text("Сыграть снова")
                            }
                        }
                    }
                } else if (!running) {
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
                                "Нажмите «Пробел» (Desktop/Web) или используйте кнопку ниже",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(12.dp))
                            FilledTonalButton(onClick = { running = true }) { Text("Продолжить") }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        // ВАЖНО: экранная крестовина УДАЛЕНА
    }
}

// Удобные пропсы для Canvas — без изменений
private val EngineState.w
    get() = snake.maxOfOrNull { it.x }?.let { maxX -> maxOf(maxX + 1, 22) } ?: 22
private val EngineState.h
    get() = snake.maxOfOrNull { it.y }?.let { maxY -> maxOf(maxY + 1, 22) } ?: 22
