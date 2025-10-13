package ru.acs.grandmap.navigation

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.acs.grandmap.ui.Tab
import kotlinx.serialization.Serializable
import ru.acs.grandmap.config.BASE_URL
import ru.acs.grandmap.core.auth.TokenManager
import ru.acs.grandmap.core.auth.TokenState
import ru.acs.grandmap.data.auth.AuthApi
import ru.acs.grandmap.feature.auth.defaultUseCookies
import ru.acs.grandmap.feature.work.DefaultWorkComponent
import ru.acs.grandmap.feature.work.WorkComponent
import ru.acs.grandmap.feature.auth.dto.*
import ru.acs.grandmap.network.ApiException
import androidx.compose.runtime.State as ComposeState
import io.ktor.utils.io.errors.IOException as KtorIOException


interface RootComponent {
    val childStack: Value<ChildStack<Config, Child>>
    val profile: ComposeState<EmployeeDto?>
    val events: SharedFlow<UiEvent>
    fun select(tab: Tab)
    fun reselect(tab: Tab)

    fun onProfileShown()
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

sealed interface UiEvent {
    data class Snack(val text: String) : UiEvent
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val tokenManager: TokenManager,
    private val httpClient: HttpClient

) : RootComponent, ComponentContext by componentContext {

    private val nav = StackNavigation<RootComponent.Config>()
    private val _profile = mutableStateOf<EmployeeDto?>(null)
    override val profile: ComposeState<EmployeeDto?> = _profile

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        scope.launch {
            tokenManager.state.collect { st ->
                if (st is TokenState.Unauthenticated) {
                    _profile.value = null
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

    private val authApi = AuthApi(httpClient, BASE_URL)

    override fun onProfileShown() {
        scope.launch {
            runCatching { authApi.getProfile() }
                .onSuccess { emp ->
                    _profile.value = emp
                }
                .onFailure { err ->
                    when (val e = err.toAppError()) {
                        AppError.Unauthorized -> { /* logout уже сделан, экран сам сменится */
                        }

                        is AppError.Http -> showSnack(e.message ?: "Ошибка ${e.code}")
                        is AppError.Network -> showSnack("Нет сети")
                        is AppError.Unknown -> showSnack("Что-то пошло не так")
                    }
                }
        }
    }

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
        RootComponent.Config.Me -> RootComponent.Child.Me
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

    override fun logout() {
        scope.launch {
            // TODO : дернуть эндпоинт на бэке! runCatching { authApi.logout(/*csrf?*/) }
            // локально чистим всё и уводим на Auth
            tokenManager.logout()
            _profile.value = null
            nav.bringToFront(RootComponent.Config.Auth)
            _events.tryEmit(UiEvent.Snack("Вы вышли из аккаунта"))
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
    is ApiException ->
        if (status == HttpStatusCode.Unauthorized)
            AppError.Unauthorized
        else
            AppError.Http(code = status.value, message = rawBody ?: message)

    // Кроссплатформенная I/O ошибка Ktor
    is KtorIOException -> AppError.Network(message ?: "Сетевая ошибка")

    // Полезные частные случаи (не обязательно, но удобно)
    is HttpRequestTimeoutException -> AppError.Network("Таймаут запроса")
    is ConnectTimeoutException -> AppError.Network("Таймаут соединения")
    is UnresolvedAddressException -> AppError.Network("Сервер недоступен")

    else -> AppError.Unknown(message ?: "Неизвестная ошибка")
}