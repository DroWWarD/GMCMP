package ru.acs.grandmap.navigation

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import io.ktor.client.HttpClient
import ru.acs.grandmap.ui.Tab
import kotlinx.serialization.Serializable
import ru.acs.grandmap.core.auth.TokenManager
import ru.acs.grandmap.core.auth.TokenState
import ru.acs.grandmap.feature.auth.defaultUseCookies
import ru.acs.grandmap.feature.work.DefaultWorkComponent
import ru.acs.grandmap.feature.work.WorkComponent
import ru.acs.grandmap.feature.auth.dto.*
import androidx.compose.runtime.State as ComposeState


interface RootComponent {
    val childStack: Value<ChildStack<Config, Child>>
    val profile: ComposeState<EmployeeDto?>
    fun select(tab: Tab)
    fun reselect(tab: ru.acs.grandmap.ui.Tab)

    // что хранится в back stack
    @Serializable
    sealed class Config {
        @Serializable
        data object Auth : Config()
        @Serializable
        data object Work : Config()
        @Serializable
        data object Chat : Config()
        @Serializable
        data object News : Config()
        @Serializable
        data object Game : Config()
        @Serializable
        data object Me : Config()
    }

    @Serializable
    sealed class Child {
        data class Auth(val component: AuthComponent) : Child()
        data class Work(val component: WorkComponent) : Child()
        data object Chat : Child()
        data object News : Child()
        data object Game : Child()
        data object Me : Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val tokenManager: TokenManager,
    private val httpClient: HttpClient

) : RootComponent, ComponentContext by componentContext {

    private val nav = StackNavigation<RootComponent.Config>()
    private val _profile = mutableStateOf<EmployeeDto?>(null)
    override val profile: ComposeState<EmployeeDto?> = _profile
    override val childStack: Value<ChildStack<RootComponent.Config, RootComponent.Child>> =
        childStack(
            source = nav,
            serializer = RootComponent.Config.serializer(),
            initialConfiguration = if (tokenManager.state.value is TokenState.Authorized)
                RootComponent.Config.Me else RootComponent.Config.Auth,
            handleBackButton = true,
            childFactory = ::createChild
        )


    private fun createChild(
        cfg: RootComponent.Config,
        ctx: ComponentContext
    ): RootComponent.Child = when (cfg) {
        RootComponent.Config.Auth -> RootComponent.Child.Auth(
            DefaultAuthComponent(
                componentContext = ctx,
                tokenManager = tokenManager,
                httpClient = httpClient,
                useCookiesDefault = defaultUseCookies(),
                onAuthorized = { employee ->
                    _profile.value = employee
                    nav.bringToFront(RootComponent.Config.Me)
                }
            )
        )
        RootComponent.Config.Work -> RootComponent.Child.Work(DefaultWorkComponent(ctx))
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
            Tab.Me -> RootComponent.Config.Me
        }
        nav.bringToFront(cfg) // одна инстанс-вкладка, состояние сохраняется
    }

    override fun reselect(tab: Tab) {
        when (tab) {
            Tab.Work ->
                (childStack.value.active.instance as? RootComponent.Child.Work)
                    ?.component?.resetToRoot()
            // Для других вкладок позже можно сделать свои реакции
            else -> Unit
        }
    }
}
