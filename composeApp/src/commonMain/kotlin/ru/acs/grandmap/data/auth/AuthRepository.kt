package ru.acs.grandmap.data.auth

import io.github.aakira.napier.Napier
import ru.acs.grandmap.core.AppResult

interface AuthRepository {
    suspend fun ping(): AppResult<String>
}

class AuthRepositoryImpl(
    private val api: AuthApi,
) : AuthRepository {
    override suspend fun ping(): AppResult<String> = try {
        val text = api.ping()
        Napier.d("PING ok: $text", tag = "AuthRepo")
        AppResult.Ok(text)
    } catch (t: Throwable) {
        Napier.e("PING failed", t, tag = "AuthRepo")
        AppResult.Err(t.message ?: "Unknown error", t)
    }
}
