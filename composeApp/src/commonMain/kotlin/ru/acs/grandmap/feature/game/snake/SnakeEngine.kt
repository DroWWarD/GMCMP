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
        // запрет разворота на 180°
        val bad = (dir == Dir.Left && new == Dir.Right) ||
                (dir == Dir.Right && new == Dir.Left) ||
                (dir == Dir.Up && new == Dir.Down) ||
                (dir == Dir.Down && new == Dir.Up)
        if (!bad) dir = new
    }

    fun tick() {
        if (over) return
        val head = snake.last()
        val next = when (dir) {
            Dir.Left  -> Cell(head.x - 1, head.y)
            Dir.Right -> Cell(head.x + 1, head.y)
            Dir.Up    -> Cell(head.x, head.y - 1)
            Dir.Down  -> Cell(head.x, head.y + 1)
        }
        // стены
        if (next.x !in 0 until w || next.y !in 0 until h) { over = true; return }
        // самоукус
        if (snake.contains(next)) { over = true; return }

        snake.addLast(next)
        if (next == food) {
            score += 1
            food = randomFreeCell()
        } else {
            snake.removeFirst()
        }
    }

    fun restart() {
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
}
