package ru.acs.grandmap.feature.sessions

import androidx.compose.runtime.*
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.*
import ru.acs.grandmap.core.toUserMessage
import ru.acs.grandmap.core.auth.TokenManager
interface SessionsComponent {
    val state: State<UiState>
    fun refresh()
    fun revoke(id: String)
    fun revokeOthers()
    fun revokeAll()
    fun back()
}

data class UiState(
    val loading: Boolean = true,
    val items: List<SessionDto> = emptyList(),
    val error: String? = null,
    val revokingId: String? = null
)

class DefaultSessionsComponent(
    componentContext: ComponentContext,
    private val api: SessionsApi,
    private val onBack: () -> Unit
) : SessionsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = mutableStateOf(UiState())
    override val state: State<UiState> = _state

    init { refresh() }

    override fun refresh() {
        scope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { api.list() }
                .onSuccess { _state.value = _state.value.copy(loading = false, items = it) }
                .onFailure { _state.value = _state.value.copy(loading = false, error = it.toUserMessage()) }
        }
    }

    override fun revoke(id: String) {
        scope.launch {
            _state.value = _state.value.copy(revokingId = id, error = null)
            runCatching { api.revoke(id) }
                .onSuccess { refresh() }
                .onFailure { _state.value = _state.value.copy(revokingId = null, error = it.toUserMessage()) }
        }
    }

    override fun revokeOthers() {
        scope.launch {
            runCatching { api.revokeOthers() }
                .onSuccess { refresh() }
                .onFailure { _state.value = _state.value.copy(error = it.toUserMessage()) }
        }
    }

    override fun revokeAll() {
        scope.launch {
            runCatching { api.revokeAll() }
                .onSuccess { refresh() } // текущая сессия отвалится позже — пользователь нажмёт «Выйти» сам
                .onFailure { _state.value = _state.value.copy(error = it.toUserMessage()) }
        }
    }

    override fun back() = onBack()
}