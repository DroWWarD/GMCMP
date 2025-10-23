package ru.acs.grandmap.feature.game.snake

import TopBarController
import TopBarSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.zIndex
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnStop
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlin.math.abs

@Composable
fun SnakeScreen(
    component: SnakeComponent,
    topBar: TopBarController
) {
    // Топбар
    LaunchedEffect(Unit) {
        topBar.update(
            TopBarSpec(
                title = "Игра «Змейка»",
                onBack = component::back,
                visible = true,
                overflowOpen = false
            )
        )
    }

    val engine = remember { SnakeEngine(w = 22, h = 22) }

    // --- Saver: только примитивы и List — кроссплатформенно
    val engineStateSaver = remember {
        listSaver<EngineState, Any>(
            save = { st ->
                listOf(
                    st.snake.flatMap { listOf(it.x, it.y) }, // List<Int>
                    st.food.x, st.food.y,
                    st.dir.ordinal,
                    st.score,
                    st.gameOver
                )
            },
            restore = { l ->
                val ints = (l[0] as List<*>).map { it as Int }
                val snake = ints.chunked(2).map { (x, y) -> Cell(x, y) }
                val food  = Cell(l[1] as Int, l[2] as Int)
                val dir   = Dir.values()[l[3] as Int]
                val score = l[4] as Int
                val over  = l[5] as Boolean
                EngineState(snake, food, dir, score, over)
            }
        )
    }

    var st by rememberSaveable(stateSaver = engineStateSaver) { mutableStateOf(engine.state()) }
    var running by rememberSaveable { mutableStateOf(true) }

    // Автопауза при открытии бургера (пропускаем 1-й эмит)
    val bar by topBar.spec.collectAsState()
    var barSeenOnce by remember { mutableStateOf(false) }
    LaunchedEffect(bar.overflowOpen) {
        if (!barSeenOnce) { barSeenOnce = true; return@LaunchedEffect }
        if (bar.overflowOpen) { running = false; st = engine.state() }
    }

    // Восстановление движка
    LaunchedEffect(engine) { engine.restore(st) }

    // Пауза при уходе экрана + очистка топбара
    DisposableEffect(Unit) {
        onDispose {
            running = false
            st = engine.state()
            topBar.clear()
        }
    }

    val isTouch = isTouchDevice()

    // Фокусы
    val gameFocus     = remember { FocusRequester() }
    val resumeBtnFocus  = remember { FocusRequester() }
    val restartBtnFocus = remember { FocusRequester() }
    var layoutReady by remember { mutableStateOf(false) }

    val foodColor = MaterialTheme.colorScheme.tertiary
    val headColor = MaterialTheme.colorScheme.primary
    val bodyColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    // Луп тиков
    LaunchedEffect(running, st.gameOver) {
        if (!running || st.gameOver) return@LaunchedEffect
        while (running && !st.gameOver) {
            val tickMs = (120L - (st.score * 2L)).coerceAtLeast(50L)
            delay(tickMs)
            engine.tick(); st = engine.state()
        }
    }

    // Клавиатура — только для не-тач
    val keyHandler: (KeyEvent) -> Boolean = handler@ { e ->
        if (e.type != KeyEventType.KeyDown) return@handler false
        when (e.key) {
            Key.DirectionUp    -> engine.changeDir(Dir.Up)
            Key.DirectionDown  -> engine.changeDir(Dir.Down)
            Key.DirectionLeft  -> engine.changeDir(Dir.Left)
            Key.DirectionRight -> engine.changeDir(Dir.Right)
            Key.Spacebar       -> running = !running
            else -> return@handler false
        }
        st = engine.state()
        true
    }

    // Пауза при сворачивании/уходе
    LaunchedEffect(component.lifecycle) {
        component.lifecycle.doOnPause { running = false; st = engine.state() }
        component.lifecycle.doOnStop  { running = false; st = engine.state() }
    }

    // Верх: счёт
    Column(
        Modifier
            .fillMaxSize()
            .then(
                if (isTouch)
                    Modifier.pointerInput(Unit) {
                        detectDragGestures { change, drag ->
                            change.consume()
                            val (dx, dy) = drag
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
        horizontalAlignment = Alignment.Start
    ) {
        Text("Счёт: ${st.score}")
        Spacer(Modifier.height(12.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopStart
        ) {
            val boardSize = min(maxWidth, maxHeight)
            val overlaysVisible = st.gameOver || !running

            Box(
                modifier = Modifier
                    .size(boardSize)
                    .onGloballyPositioned { layoutReady = true }
                    // Клавиатура — только Desktop/Web без тача
                    .then(
                        if (!isTouch)
                            Modifier
                                .focusRequester(gameFocus)
                                .focusTarget()
                                .onPreviewKeyEvent(keyHandler)
                                .clickable(enabled = !overlaysVisible) { gameFocus.requestFocus() }
                        else Modifier
                    )
                    // Жесты — только тач и только поверх поля

            ) {
                Canvas(Modifier.fillMaxSize()) {
                    // !!! фиксированный грид: считаем от engine, не от st
                    val cell = size.width / engine.w

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

                // Game Over
                if (st.gameOver) {
                    Surface(
                        modifier = Modifier
                            .zIndex(1f)
                            .align(Alignment.Center),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        tonalElevation = 2.dp,
                        shadowElevation = 2.dp
                    ) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Игра окончена", style = MaterialTheme.typography.titleLarge)
                            Text("Счёт: ${st.score}", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            FilledTonalButton(
                                modifier = Modifier,
                                onClick = {
                                    engine.restart()
                                    st = engine.state()
                                    running = true
                                    gameFocus.requestFocus()
                                }
                            ) { Text("Сыграть снова") }
                        }
                    }
                }

                // Пауза
                if (!st.gameOver && !running) {
                    Surface(
                        modifier = Modifier
                            .zIndex(1f)
                            .align(Alignment.Center),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        tonalElevation = 2.dp,
                        shadowElevation = 2.dp
                    ) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Пауза", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("Нажмите «Пробел» или кнопку ниже, чтобы продолжить")
                            Spacer(Modifier.height(12.dp))
                            FilledTonalButton(
                                onClick = {
                                    running = true
                                    gameFocus.requestFocus()
                                }
                            ) { Text("Продолжить") }
                        }
                    }
                }
            }
        }
    }

    // Автофокус: поле / кнопка паузы / кнопка рестарта
    LaunchedEffect(isTouch, layoutReady, running, st.gameOver) {
        if (isTouch || !layoutReady) return@LaunchedEffect
        yield()
        when {
            st.gameOver -> restartBtnFocus.requestFocus()
            !running    -> resumeBtnFocus.requestFocus()
            else        -> gameFocus.requestFocus()
        }
    }
}

// Удобные пропсы для Canvas — без изменений
private val EngineState.w
    get() = snake.maxOfOrNull { it.x }?.let { maxX -> maxOf(maxX + 1, 22) } ?: 22
private val EngineState.h
    get() = snake.maxOfOrNull { it.y }?.let { maxY -> maxOf(maxY + 1, 22) } ?: 22
