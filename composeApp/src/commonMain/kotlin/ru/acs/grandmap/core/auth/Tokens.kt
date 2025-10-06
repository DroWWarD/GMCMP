package ru.acs.grandmap.core.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.acs.grandmap.data.auth.AuthApi
import kotlin.time.ExperimentalTime

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    /** epoch seconds; можно не задавать, тогда рефреш будет только по 401 */
    val accessExpiresAt: Long? = null,
)

sealed class TokenState {
    data object Unauthenticated : TokenState()
    data class Authorized(val pair: TokenPair) : TokenState()
}

interface TokenStorage {
    fun read(): TokenPair?
    fun write(value: TokenPair?)
}

class TokenManager(
    private val storage: TokenStorage,
    private val api: AuthApi,
    private val skewSeconds: Long = 30,     // анти-дрифт часов при проактивном рефреше
) {
    private val _state = MutableStateFlow<TokenState>(
        storage.read()?.let { TokenState.Authorized(it) } ?: TokenState.Unauthenticated
    )
    val state: StateFlow<TokenState> = _state

    private val mutex = Mutex()

    fun logout() = set(null)
    fun currentAccessOrNull(): String? =
        (state.value as? TokenState.Authorized)?.pair?.accessToken

    fun set(pair: TokenPair?) {
        storage.write(pair)
        _state.value = pair?.let { TokenState.Authorized(it) } ?: TokenState.Unauthenticated
    }

    /** true если нужно обновлять заранее по expiresAt */
    private fun isExpiringSoon(nowSec: Long, expiresAt: Long?): Boolean =
        expiresAt?.let { nowSec >= it - skewSeconds } ?: false

    /** проактивная проверка по expiresAt (опционально, не обязательно вызывать) */
    @OptIn(ExperimentalTime::class)
    suspend fun ensureFresh() {
        val st = state.value as? TokenState.Authorized ?: return
        val now = kotlin.time.Clock.System.now().epochSeconds
        if (!isExpiringSoon(now, st.pair.accessExpiresAt)) return
        refreshInternal()
    }

    /** дергаем при 401 — вернёт true, если смог обновить */
    suspend fun refreshAfterUnauthorized(): Boolean = mutex.withLock {
        val ok = refreshInternal()
        ok
    }

    private suspend fun refreshInternal(): Boolean {
        val st = state.value as? TokenState.Authorized ?: return false
        val newPair = runCatching { api.refresh(st.pair.refreshToken) }.getOrNull() ?: return false
        set(newPair)
        return true
    }
}
