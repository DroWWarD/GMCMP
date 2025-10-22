package ru.acs.grandmap.navigation

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.acs.grandmap.core.auth.TokenManager
import ru.acs.grandmap.core.auth.TokenState
import ru.acs.grandmap.feature.auth.AuthComponent
import ru.acs.grandmap.feature.auth.DefaultAuthComponent
import ru.acs.grandmap.feature.auth.defaultUseCookies
import ru.acs.grandmap.feature.work.DefaultWorkComponent
import ru.acs.grandmap.feature.work.WorkComponent
import ru.acs.grandmap.feature.chat.ChatComponent
import ru.acs.grandmap.feature.auth.dto.*
import ru.acs.grandmap.feature.chat.DefaultChatComponent
import ru.acs.grandmap.feature.news.NewsComponent
import ru.acs.grandmap.feature.game.DefaultGameComponent
import ru.acs.grandmap.feature.profile.DefaultProfileComponent
import ru.acs.grandmap.feature.profile.ProfileApi
import ru.acs.grandmap.feature.profile.ProfileComponent
import ru.acs.grandmap.feature.game.GameComponent
import ru.acs.grandmap.feature.game.snake.DefaultSnakeComponent
import ru.acs.grandmap.feature.game.snake.SnakeComponent
import ru.acs.grandmap.feature.news.DefaultNewsComponent
import ru.acs.grandmap.feature.profile.ProfileRepository
import ru.acs.grandmap.feature.profile.settings.DefaultSettingsComponent
import ru.acs.grandmap.feature.profile.settings.sessions.SessionsComponent
import ru.acs.grandmap.feature.profile.settings.SettingsComponent
import ru.acs.grandmap.feature.profile.settings.sessions.DefaultSessionsComponent
import ru.acs.grandmap.feature.profile.settings.sessions.SessionsApi
import ru.acs.grandmap.network.ApiException
import io.ktor.utils.io.errors.IOException as KtorIOException


interface RootComponent {
    val childStack: Value<ChildStack<Config, Child>>
    val events: SharedFlow<UiEvent>
    fun select(tab: Tab)
    fun reselect(tab: Tab)

    fun openSettings()
    fun openSessions()
    fun logout()

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
        data object GameSnake : Config()
        @Serializable
        data object Profile : Config()
        @Serializable
        data object Settings : Config()
        @Serializable
        data object Sessions : Config()
    }

    sealed class Child {
        data class Auth(val component: AuthComponent) : Child()
        data class Work(val component: WorkComponent) : Child()
        data class Chat(val component: ChatComponent) : Child()
        data class News(val component: NewsComponent) : Child()
        data class Game(val component: GameComponent) : Child()
        data class GameSnake(val component: SnakeComponent) : Child()
        data class Profile(val component: ProfileComponent) : Child()
        data class Settings(val component: SettingsComponent) : Child()
        data class Sessions(val component: SessionsComponent) : Child()
    }
}

sealed interface UiEvent {
    data class Snack(val text: String) : UiEvent
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val tokenManager: TokenManager,
    private val httpClient: HttpClient

) : RootComponent, ComponentContext by componentContext {

    private val tabStacks =
        mutableMapOf<Tab, List<RootComponent.Config>>() // стек для каждой вкладки

    private fun currentRootTabOrNull(): Tab? = when (childStack.value.active.configuration) {
        RootComponent.Config.Profile, RootComponent.Config.Settings, RootComponent.Config.Sessions -> Tab.Profile
        RootComponent.Config.Work -> Tab.Work
        RootComponent.Config.Chat -> Tab.Chat
        RootComponent.Config.News -> Tab.News
        RootComponent.Config.Game, RootComponent.Config.GameSnake -> Tab.Game
        RootComponent.Config.Auth -> null
    }

    private fun defaultStackFor(tab: Tab): List<RootComponent.Config> = listOf(
        when (tab) {
            Tab.Profile -> RootComponent.Config.Profile
            Tab.Work -> RootComponent.Config.Work
            Tab.Chat -> RootComponent.Config.Chat
            Tab.News -> RootComponent.Config.News
            Tab.Game -> RootComponent.Config.Game
        }
    )

    private fun snapshotCurrentStack(): List<RootComponent.Config> =
        childStack.value.items.map { it.configuration }

    private fun restoreStack(stack: List<RootComponent.Config>) {
        nav.navigate { stack } // вернём новый список как есть
    }

    private val nav = StackNavigation<RootComponent.Config>()

    private fun tabOf(cfg: RootComponent.Config): Tab? = when (cfg) {
        RootComponent.Config.Profile,
        RootComponent.Config.Settings,
        RootComponent.Config.Sessions
            -> Tab.Profile

        RootComponent.Config.Work -> Tab.Work
        RootComponent.Config.Chat -> Tab.Chat
        RootComponent.Config.News -> Tab.News

        RootComponent.Config.Game,
        RootComponent.Config.GameSnake
            -> Tab.Game

        RootComponent.Config.Auth -> null
    }

    override val childStack: Value<ChildStack<RootComponent.Config, RootComponent.Child>> =
        childStack(
            source = nav,
            serializer = RootComponent.Config.serializer(),
            initialConfiguration = if (tokenManager.state.value is TokenState.Authorized) RootComponent.Config.Profile else RootComponent.Config.Auth,
            handleBackButton = true,
            childFactory = ::createChild
        )

    private var currentTab: Tab =
        tabOf(childStack.value.active.configuration) ?: Tab.Profile

    private val stacksByTab: MutableMap<Tab, List<RootComponent.Config>> = mutableMapOf(
        Tab.Profile to listOf(RootComponent.Config.Profile),
        Tab.Work to listOf(RootComponent.Config.Work),
        Tab.Chat to listOf(RootComponent.Config.Chat),
        Tab.News to listOf(RootComponent.Config.News),
        Tab.Game to listOf(RootComponent.Config.Game),
    )

    /** Сохраняем текущий стек (только конфиги) как стек текущей вкладки */
    private fun snapshotCurrentStackIntoCurrentTab() {
        val list = childStack.value.items.map { it.configuration }
        stacksByTab[currentTab] = list
    }

    /** Применяем стек выбранной вкладки */

    private var lastByTab: MutableMap<Tab, RootComponent.Config> = mutableMapOf(
        Tab.Profile to RootComponent.Config.Profile,
        Tab.Work to RootComponent.Config.Work,
        Tab.Chat to RootComponent.Config.Chat,
        Tab.News to RootComponent.Config.News,
        Tab.Game to RootComponent.Config.Game,
    )

    private fun rememberTab(cfg: RootComponent.Config) {
        when (cfg) {
            RootComponent.Config.Profile,
            RootComponent.Config.Settings,
            RootComponent.Config.Sessions -> lastByTab[Tab.Profile] = cfg

            RootComponent.Config.Work -> lastByTab[Tab.Work] = cfg
            RootComponent.Config.Chat -> lastByTab[Tab.Chat] = cfg
            RootComponent.Config.News -> lastByTab[Tab.News] = cfg
            RootComponent.Config.Game,
            RootComponent.Config.GameSnake -> lastByTab[Tab.Game] = cfg

            RootComponent.Config.Auth -> Unit
        }
    }

    private val _profile = mutableStateOf<EmployeeDto?>(null)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        scope.launch {
            tokenManager.state.collect { st ->
                if (st is TokenState.Unauthenticated) {
                    nav.bringToFront(RootComponent.Config.Auth)
                }
            }
        }
    }

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    override val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private fun showSnack(text: String) {
        _events.tryEmit(UiEvent.Snack(text))
    }

    fun openSessionsFromSettings() {
        rememberTab(RootComponent.Config.Sessions)
        nav.bringToFront(RootComponent.Config.Sessions)
    }

    /** Можно ли сделать back ВНУТРИ текущей вкладки? */
    fun canGoBackInTab(): Boolean = childStack.value.backStack.isNotEmpty()

    /** Back внутри вкладки. Возвращает true, если что-то попнули. */
    fun backInTab(): Boolean {
        // Только если мы на рутовой вкладке и в стеке > 1 экрана — поп
        if (currentRootTabOrNull() != null && childStack.value.items.size > 1) {
            nav.pop()
            return true
        }
        return false
    }


    @OptIn(DelicateDecomposeApi::class)
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
                onAuthorized = { _ ->
                    // после логина — на вкладку Профиль:
                    nav.replaceAll(RootComponent.Config.Profile)
                }
            )
        )

        RootComponent.Config.Work -> RootComponent.Child.Work(
            DefaultWorkComponent(ctx)
        )

        RootComponent.Config.Chat -> RootComponent.Child.Chat(
            DefaultChatComponent(ctx)
        )

        RootComponent.Config.News -> RootComponent.Child.News(
            DefaultNewsComponent(ctx)
        )
        RootComponent.Config.Game -> RootComponent.Child.Game(
            DefaultGameComponent(
                componentContext = ctx,
                onOpenSnake = { nav.push(RootComponent.Config.GameSnake) }
            )
        )

        RootComponent.Config.GameSnake -> RootComponent.Child.GameSnake(
            DefaultSnakeComponent(
                componentContext = ctx,
                onBack = { nav.pop() }
            )
        )

        RootComponent.Config.Profile -> {
            val repo = ProfileRepository(api = ProfileApi(httpClient))
            RootComponent.Child.Profile(
                DefaultProfileComponent(
                    componentContext = ctx,
                    repo = repo,
                    onOpenSettings = { nav.push(RootComponent.Config.Settings) },
                    onOpenSessions = { nav.push(RootComponent.Config.Sessions) },
                    onLogOut = { logout() }
                )
            )
        }

        RootComponent.Config.Settings -> RootComponent.Child.Settings(
            DefaultSettingsComponent(
                componentContext = ctx,
                onBack = { nav.pop() },
                onOpenSessions = { nav.bringToFront(RootComponent.Config.Sessions) }
            )
        )

        RootComponent.Config.Sessions -> RootComponent.Child.Sessions(
            DefaultSessionsComponent(
                componentContext = ctx,
                api = SessionsApi(httpClient, tokenManager),
                onBack = { nav.pop() }
            )
        )
    }

    @OptIn(DelicateDecomposeApi::class)
    override fun openSettings() {
        nav.push(RootComponent.Config.Settings)
    }

    @OptIn(DelicateDecomposeApi::class)
    override fun openSessions() {
        nav.push(RootComponent.Config.Sessions)
    }

    override fun select(tab: Tab) {
        val currentTab = currentRootTabOrNull()
        // 1) Сохраняем ТЕКУЩУЮ вкладку (только если мы вообще на рутовой вкладке)
        if (currentTab != null) {
            tabStacks[currentTab] = snapshotCurrentStack()
        }
        // 2) Берём нужный стек для целевой вкладки (или дефолтный, если ещё не сохраняли)
        val target = tabStacks[tab] ?: defaultStackFor(tab)
        // 3) Восстанавливаем его
        restoreStack(target)
    }


    override fun reselect(tab: Tab) {
        val currentTab = currentRootTabOrNull()
        if (currentTab != tab) {
            // если это не активная вкладка — просто переключимcя на неё
            select(tab)
            return
        }
        // мы уже на нужной вкладке → сбрасываем её стек к корню
        val root = defaultStackFor(tab)
        tabStacks[tab] = root
        restoreStack(root)
    }

    override fun logout() {
        scope.launch {
            // (по желанию: дернуть API /logout)
            tokenManager.logout()
            // обнулим кэши стеков вкладок
            tabStacks.clear()

            // самый чистый способ — заменить стек одним Auth
            nav.replaceAll(RootComponent.Config.Auth)
        }
    }

}

sealed interface AppError {
    data class Http(val code: Int, val message: String?) : AppError
    data class Network(val message: String?) : AppError
    data class Unknown(val message: String?) : AppError
    object Unauthorized : AppError
}

fun Throwable.toAppError(): AppError = when (this) {
    is ApiException -> if (status == HttpStatusCode.Unauthorized) AppError.Unauthorized
    else AppError.Http(code = status.value, message = rawBody ?: message)

    // Кроссплатформенная I/O ошибка Ktor
    is KtorIOException -> AppError.Network(message ?: "Сетевая ошибка")

    // Полезные частные случаи (не обязательно, но удобно)
    is HttpRequestTimeoutException -> AppError.Network("Таймаут запроса")
    is ConnectTimeoutException -> AppError.Network("Таймаут соединения")
    is UnresolvedAddressException -> AppError.Network("Сервер недоступен")

    else -> AppError.Unknown(message ?: "Неизвестная ошибка")
}