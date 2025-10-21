package ru.acs.grandmap.feature.game

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext

interface GameComponent {
    val mode: State<Mode>
    fun openSnake()
    fun back()
}

sealed class Mode {
    data object Menu : Mode()
    data object Snake : Mode()
}

class DefaultGameComponent(
    componentContext: ComponentContext,
) : GameComponent, ComponentContext by componentContext {

    private val _mode = mutableStateOf<Mode>(Mode.Menu)
    override val mode: State<Mode> = _mode

    override fun openSnake() { _mode.value = Mode.Snake }
    override fun back() { _mode.value = Mode.Menu }
}
