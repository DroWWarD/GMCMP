package ru.acs.grandmap.feature.game.snake

import kotlin.random.Random

data class Cell(val x: Int, val y: Int)
enum class Dir { Up, Down, Left, Right }

data class EngineState(
    val snake: List<Cell>,
    val food: Cell,
    val dir: Dir,
    val score: Int,
    val gameOver: Boolean
)

class SnakeEngine(
    val w: Int = 20,
    val h: Int = 20,
) {
    private var dir: Dir = Dir.Right
    private var pendingDir: Dir? = null
    private val snake = ArrayDeque<Cell>()
    private var food: Cell
    private var score = 0
    private var over = false

    init {
        val cx = w / 2
        val cy = h / 2
        snake.addLast(Cell(cx - 2, cy))
        snake.addLast(Cell(cx - 1, cy))
        snake.addLast(Cell(cx, cy))
        food = randomFreeCell()
    }

    fun state(): EngineState = EngineState(
        snake = snake.toList(),
        food = food,
        dir = dir,
        score = score,
        gameOver = over
    )

    fun changeDir(new: Dir) {
        val current = pendingDir ?: dir
        if (!isOpposite(current, new) && pendingDir == null) {
            pendingDir = new
        }
    }

    private fun isOpposite(a: Dir, b: Dir): Boolean =
        (a == Dir.Left && b == Dir.Right) ||
                (a == Dir.Right && b == Dir.Left) ||
                (a == Dir.Up && b == Dir.Down) ||
                (a == Dir.Down && b == Dir.Up)

    fun tick() {
        if (over) return

        // применяем отложенный поворот ровно один раз
        pendingDir?.let { nd ->
            if (!isOpposite(dir, nd)) dir = nd
            pendingDir = null
        }

        val head = snake.last()
        val next = when (dir) {
            Dir.Left  -> Cell(head.x - 1, head.y)
            Dir.Right -> Cell(head.x + 1, head.y)
            Dir.Up    -> Cell(head.x, head.y - 1)
            Dir.Down  -> Cell(head.x, head.y + 1)
        }

        // стены
        if (next.x !in 0 until w || next.y !in 0 until h) { over = true; return }

        // Фикс 2 — хвост освобождает клетку: не считаем укус,
        // если голова идёт в текущую клетку хвоста и при этом не растём
        val willEat = (next == food)
        val bodyToCheck = if (willEat) snake else snake.drop(1) // игнорируем хвост, если не едим
        if (next in bodyToCheck) { over = true; return }

        snake.addLast(next)
        if (willEat) {
            score += 1
            food = randomFreeCell()
        } else {
            snake.removeFirst()
        }
    }

    fun restart() {
        pendingDir = null               // <— сбрасываем буфер
        snake.clear()
        dir = Dir.Right
        score = 0
        over = false
        val cx = w / 2
        val cy = h / 2
        snake.addLast(Cell(cx - 2, cy))
        snake.addLast(Cell(cx - 1, cy))
        snake.addLast(Cell(cx, cy))
        food = randomFreeCell()
    }

    private fun randomFreeCell(): Cell {
        while (true) {
            val c = Cell(Random.nextInt(w), Random.nextInt(h))
            if (!snake.contains(c)) return c
        }
    }

    fun restore(from: EngineState) {
        snake.clear()
        from.snake.forEach { snake.addLast(it) }
        dir = from.dir
        food = from.food
        score = from.score
        over  = from.gameOver
    }
}
