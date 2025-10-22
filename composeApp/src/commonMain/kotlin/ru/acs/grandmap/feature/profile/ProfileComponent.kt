package ru.acs.grandmap.feature.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import kotlinx.coroutines.*
import kotlinx.datetime.Instant
import ru.acs.grandmap.feature.auth.dto.EmployeeDto
import kotlin.time.ExperimentalTime

interface ProfileComponent {
    val uiState: State<UiState>
    fun refresh(force: Boolean = true)
    fun openSettings()
    fun openSessions()

    fun showDbInspector()
    fun hideDbInspector()
    fun refreshDbInspector()
    fun clearDbInspector()
    fun syncAndRefreshDbInspector()
    fun logOut()
}

data class UiState @OptIn(ExperimentalTime::class) constructor(
    val loading: Boolean = false,
    val employee: EmployeeDto? = null,
    val error: String? = null,
    val lastSync: Instant? = null,   // когда были синхронизированы данные
    val fromCache: Boolean = false,   // текущее employee из кэша?

    val dbVisible: Boolean = false,
    val dbRows: List<ProfileRepository.DebugRow> = emptyList()
)

class DefaultProfileComponent(
    componentContext: ComponentContext,
    private val repo: ProfileRepository,
    private val onOpenSettings: () -> Unit,
    private val onOpenSessions: () -> Unit,
    private  val onLogOut: () -> Unit
) : ProfileComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = mutableStateOf(UiState())
    override val uiState: State<UiState> = _state

    init {
        // 1) Быстро показываем кэш (если есть), полностью рисуем экран.
        // 2) Параллельно запрашиваем свежие данные.
        lifecycle.doOnCreate {
            scope.launch { loadCachedThenRefresh() }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun loadCachedThenRefresh() {
        // Кэш (моментально)
        repo.readCached()?.let { cached ->
            _state.value = _state.value.copy(
                employee = cached.employee,
                lastSync = cached.lastSync,
                fromCache = true,
                loading = true,  // показываем, что сверху ещё идёт обновление
                error = null
            )
        } ?: run {
            // кэша нет — просто показываем каркас + индикатор
            _state.value = _state.value.copy(loading = true, error = null)
        }

        // Сеть (актуализация)
        runCatching { repo.fetchRemoteAndCache() }
            .onSuccess { fresh ->
                _state.value = UiState(
                    loading = false,
                    employee = fresh.employee,
                    error = null,
                    lastSync = fresh.lastSync,
                    fromCache = false
                )
            }
            .onFailure { e ->
                // если были кэш-данные — просто оставим их и снимем loading
                // если кэша не было — покажем ошибку
                val hadCache = _state.value.employee != null
                _state.value = _state.value.copy(
                    loading = false,
                    error = if (hadCache) null else (e.message ?: "Не удалось загрузить профиль")
                )
            }
    }

    @OptIn(ExperimentalTime::class)
    override fun refresh(force: Boolean) {
        scope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val result = runCatching { repo.fetchRemoteAndCache() }
            _state.value = result.fold(
                onSuccess = { fresh ->
                    UiState(
                        loading = false,
                        employee = fresh.employee,
                        error = null,
                        lastSync = fresh.lastSync,
                        fromCache = false
                    )
                },
                onFailure = { e ->
                    _state.value.copy(
                        loading = false,
                        error = e.message ?: "Не удалось обновить"
                    )
                }
            )
        }
    }

    override fun openSettings() = onOpenSettings()
    override fun openSessions() = onOpenSessions()

    fun onDestroy() { scope.cancel() }

    override fun showDbInspector() {
        scope.launch {
            val rows = runCatching { repo.debugDump() }.getOrElse { emptyList() }
            _state.value = _state.value.copy(dbVisible = true, dbRows = rows)
        }
    }

    override fun hideDbInspector() {
        _state.value = _state.value.copy(dbVisible = false)
    }

    override fun refreshDbInspector() {
        scope.launch {
            val rows = runCatching { repo.debugDump() }.getOrElse { emptyList() }
            _state.value = _state.value.copy(dbRows = rows)
        }
    }

    override fun clearDbInspector() {
        scope.launch {
            runCatching { repo.clear() }
            val rows = runCatching { repo.debugDump() }.getOrElse { emptyList() }
            _state.value = _state.value.copy(dbRows = rows)
        }
    }

    override fun syncAndRefreshDbInspector() {
        scope.launch {
            runCatching { repo.fetchRemoteAndCache() }
            val rows = runCatching { repo.debugDump() }.getOrElse { emptyList() }
            _state.value = _state.value.copy(dbRows = rows)
        }
    }

    override fun logOut() = onLogOut()
}
