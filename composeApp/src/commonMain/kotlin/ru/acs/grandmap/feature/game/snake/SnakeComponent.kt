package ru.acs.grandmap.feature.game.snake

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle

interface SnakeComponent {
    fun back()
    val lifecycle: Lifecycle
}

class DefaultSnakeComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit
) : SnakeComponent, ComponentContext by componentContext {

    override val lifecycle: Lifecycle = componentContext.lifecycle
    override fun back() = onBack()
}