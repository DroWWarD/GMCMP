package ru.acs.grandmap.feature.work

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

interface WorkComponent {
    val childStack: Value<ChildStack<Config, Child>>

    fun openDetails(id: Int)
    fun onBack()
    fun resetToRoot()

    // навигационные конфиги для стека внутри вкладки
    @Serializable
    sealed class Config {
        @Serializable data object Dashboard : Config()
        @Serializable data class Details(val id: Int) : Config()
    }

    // реальные дети, которые будем рендерить
    sealed class Child {
        data class Dashboard(val component: WorkComponent) : Child()
        data class Details(val id: Int, val component: WorkComponent) : Child()
    }

}

class DefaultWorkComponent(
    componentContext: ComponentContext
) : WorkComponent, ComponentContext by componentContext {

    private val nav = StackNavigation<WorkComponent.Config>()

    override val childStack: Value<ChildStack<WorkComponent.Config, WorkComponent.Child>> =
        childStack(
            source = nav,
            serializer = WorkComponent.Config.serializer(),
            initialConfiguration = WorkComponent.Config.Dashboard,
            handleBackButton = true,
            childFactory = ::createChild
        )

    private fun createChild(
        config: WorkComponent.Config,
        ctx: ComponentContext
    ): WorkComponent.Child =
        when (config) {
            WorkComponent.Config.Dashboard -> WorkComponent.Child.Dashboard(this)
            is WorkComponent.Config.Details -> WorkComponent.Child.Details(config.id, this)
        }

    override fun openDetails(id: Int) {
        nav.bringToFront(WorkComponent.Config.Details(id))
    }

    override fun onBack() {
        // просто вернёмся к Dashboard
        nav.bringToFront(WorkComponent.Config.Dashboard)
    }

    override fun resetToRoot() {
        nav.replaceAll(WorkComponent.Config.Dashboard)
    }
}
