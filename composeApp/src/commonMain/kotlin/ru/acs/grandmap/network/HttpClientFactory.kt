package ru.acs.grandmap.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
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
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.encodedPath
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.acs.grandmap.config.BASE_URL
import io.ktor.http.takeFrom
import ru.acs.grandmap.core.auth.TokenManager
import ru.acs.grandmap.core.auth.TokenState
import kotlinx.serialization.Serializable

expect fun platformEngine(): HttpClientEngineFactory<*>
expect fun HttpClientConfig<*>.enablePlatformCookies()

class ApiException(
    val status: HttpStatusCode,
    val rawBody: String? = null,
    message: String? = null
) : Exception(message ?: "${status.value} ${status.description}")

@Serializable
private data class ProblemDto(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val error: String? = null
)

fun makeHttpClient(isDebug: Boolean = true, tokenManager: TokenManager? = null): HttpClient =
    HttpClient(platformEngine()) {
        enablePlatformCookies()
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
                    sendWithoutRequest { req ->
                        val base = Url(BASE_URL)

                        // тот же origin (учитываем порт!)
                        val sameOrigin =
                            req.url.protocol == base.protocol &&
                                    req.url.host == base.host &&
                                    (req.url.port == base.port || base.port == -1 && req.url.port == base.protocol.defaultPort)

                        if (!sameOrigin) return@sendWithoutRequest false

                        // путь в нижнем регистре для надёжности
                        val p = req.url.encodedPath.lowercase()

                        // не посылать Bearer на ручки аутентификации (исключения)
                        p !in setOf(
                            "/api/auth/start-phone-sms",
                            "/api/auth/confirm-phone-sms",
                            "/api/auth/refresh-v2"
                        )
                    }

                }
            }
        }

        install(HttpCallValidator) {
            // Любой non-2xx -> ApiException с вытянутым сообщением
            validateResponse { response ->
                if (!response.status.isSuccess()) {
                    val msg = runCatching { response.body<ProblemDto>() }
                        .mapCatching { it.detail ?: it.title ?: it.error }
                        .getOrElse { runCatching { response.bodyAsText() }.getOrNull() }
                    throw ApiException(response.status, msg, msg)
                }
            }

            // Финальная 401 (после попытки рефреша) -> logout()
            handleResponseExceptionWithRequest { cause, _ ->
                val resp = (cause as? ResponseException)?.response
                if (resp?.status == HttpStatusCode.Unauthorized) {
                    tokenManager?.logout()
                }
                // Приводим к ApiException, если это другой ResponseException
                if (cause !is ApiException && resp != null && !resp.status.isSuccess()) {
                    val msg = runCatching { resp.body<ProblemDto>() }
                        .mapCatching { it.detail ?: it.title ?: it.error }
                        .getOrElse { runCatching { resp.bodyAsText() }.getOrNull() }
                    throw ApiException(resp.status, msg, msg)
                }
            }
        }

    }
