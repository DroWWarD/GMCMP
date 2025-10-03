package ru.acs.grandmap.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import ru.acs.grandmap.ui.Tab
import kotlinx.serialization.Serializable
import ru.acs.grandmap.feature.work.DefaultWorkComponent
import ru.acs.grandmap.feature.work.WorkComponent


interface RootComponent {
    val childStack: Value<ChildStack<Config, Child>>
    fun select(tab: Tab)
    fun reselect(tab: ru.acs.grandmap.ui.Tab)

    // что хранится в back stack
    @Serializable
    sealed class Config {
        @Serializable data object Work  : Config()
        @Serializable data object Chat  : Config()
        @Serializable data object News  : Config()
        @Serializable data object Game  : Config()
        @Serializable data object Me    : Config()
    }

    // что рисуем (пока без внутренних компонентов)
    @Serializable
    sealed class Child {
        data class Work(val component: WorkComponent) : Child()
        data object Chat  : Child()
        data object News  : Child()
        data object Game  : Child()
        data object Me    : Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {

    private val nav = StackNavigation<RootComponent.Config>()

    override val childStack: Value<ChildStack<RootComponent.Config, RootComponent.Child>> =
        childStack(
            source = nav,
            serializer = RootComponent.Config.serializer(),
            initialConfiguration = RootComponent.Config.Work,
            handleBackButton = true,
            childFactory = ::createChild
        )



    private fun createChild(
        config: RootComponent.Config,
        ctx: ComponentContext
    ): RootComponent.Child = when (config) {
        RootComponent.Config.Work -> RootComponent.Child.Work(
            component = DefaultWorkComponent(ctx)
        )
        RootComponent.Config.Chat -> RootComponent.Child.Chat
        RootComponent.Config.News -> RootComponent.Child.News
        RootComponent.Config.Game -> RootComponent.Child.Game
        RootComponent.Config.Me   -> RootComponent.Child.Me
    }

    override fun select(tab: Tab) {
        val cfg = when (tab) {
            Tab.Work -> RootComponent.Config.Work
            Tab.Chat -> RootComponent.Config.Chat
            Tab.News -> RootComponent.Config.News
            Tab.Game -> RootComponent.Config.Game
            Tab.Me   -> RootComponent.Config.Me
        }
        nav.bringToFront(cfg) // одна инстанс-вкладка, состояние сохраняется
    }

    override fun reselect(tab: ru.acs.grandmap.ui.Tab) {
        when (tab) {
            ru.acs.grandmap.ui.Tab.Work ->
                (childStack.value.active.instance as? RootComponent.Child.Work)
                    ?.component?.resetToRoot()
            // Для других вкладок позже можно сделать свои реакции
            else -> Unit
        }
    }
}
