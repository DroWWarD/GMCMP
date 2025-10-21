package ru.acs.grandmap.feature.game

import com.arkivanov.decompose.ComponentContext

interface GameComponent {
    fun openSnake()
}

class DefaultGameComponent(
    componentContext: ComponentContext,
    private val onOpenSnake: () -> Unit
) : GameComponent, ComponentContext by componentContext {

    override fun openSnake() = onOpenSnake()
}