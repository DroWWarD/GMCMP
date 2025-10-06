package ru.acs.grandmap.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.acs.grandmap.config.BASE_URL
import io.ktor.http.takeFrom
import ru.acs.grandmap.core.auth.TokenManager
import ru.acs.grandmap.core.auth.TokenState

expect fun platformEngine(): HttpClientEngineFactory<*>

fun makeHttpClient(isDebug: Boolean = true, tokenManager: TokenManager? = null): HttpClient = HttpClient(platformEngine()) {

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            }
        )
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 30_000
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = if (isDebug) LogLevel.INFO else LogLevel.NONE
    }
    defaultRequest {
        if (BASE_URL.isNotBlank()) {
            url { takeFrom(BASE_URL) }
        }
        accept(ContentType.Application.Json)
        header("Accept", "application/json")
        header("User-Agent", "GrandmApp-KMP/1.0")
    }

    if (tokenManager != null) {
        install(Auth) {
            bearer {
                // подставляем текущую пару
                loadTokens {
                    val access = tokenManager.currentAccessOrNull() ?: return@loadTokens null
                    val st = tokenManager.state.value
                    val refresh = (st as? TokenState.Authorized)
                        ?.pair?.refreshToken ?: return@loadTokens null
                    BearerTokens(access, refresh)
                }
                // при 401 — пробуем обновиться
                refreshTokens {
                    val ok = tokenManager.refreshAfterUnauthorized()
                    if (!ok) null
                    else {
                        val st =
                            tokenManager.state.value as? TokenState.Authorized
                        st?.let { BearerTokens(it.pair.accessToken, it.pair.refreshToken) }
                    }
                }
                sendWithoutRequest { request ->
                    val base = io.ktor.http.Url(BASE_URL)
                            request.url.host == base.host &&
                            request.url.protocol == base.protocol &&
                            request.url.encodedPath !in listOf("/auth/login", "/auth/refresh")
                }
            }
        }
    }
}
