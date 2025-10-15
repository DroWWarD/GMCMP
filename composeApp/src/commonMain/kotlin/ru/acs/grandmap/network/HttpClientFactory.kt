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
import io.ktor.http.HttpHeaders
import io.ktor.client.statement.*
import io.ktor.http.contentType
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
            level = if (isDebug) LogLevel.BODY else LogLevel.NONE
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
                    // если 401 на refresh — сразу разлогиниваем
                    if (response.status == HttpStatusCode.Unauthorized) {
                        val path = response.request.url.encodedPath.lowercase()
                        if (path.endsWith("/api/auth/refresh-v2")) {
                            tokenManager?.logout()
                        }
                    }

                    // безопасное сообщение
                    val uiMsg = response.problemTextOrNull()
                        ?: defaultUiMessage(response.status)

                    // НЕ прокидываем raw HTML в message
                    throw ApiException(response.status, rawBody = null, message = uiMsg)
                }
            }

            // Финальная 401 (после попытки рефреша) -> logout()
            //  401 может прийти уже как ApiException; обрабатываем оба варианта ---
            handleResponseExceptionWithRequest { cause, _ ->
                when (cause) {
                    is ApiException -> {
                        if (cause.status == HttpStatusCode.Unauthorized) {
                            tokenManager?.logout()
                        }
                        // уже user-friendly, дальше просто пробрасываем
                        return@handleResponseExceptionWithRequest
                    }
                    is ResponseException -> {
                        if (cause.response.status == HttpStatusCode.Unauthorized) {
                            tokenManager?.logout()
                        }
                        // приведём к ApiException с безопасным текстом
                        val resp = cause.response
                        val uiMsg = runCatching { resp.problemTextOrNull() }.getOrNull()
                            ?: defaultUiMessage(resp.status)
                        throw ApiException(resp.status, rawBody = null, message = uiMsg)
                    }
                }

                // Если это был ResponseException (Client/Server), но не ApiException — приведём к ApiException
                val resp = (cause as? ResponseException)?.response
                if (resp != null && !resp.status.isSuccess() && cause !is ApiException) {
                    val msg = runCatching { resp.body<ProblemDto>() }
                        .mapCatching { it.detail ?: it.title ?: it.error }
                        .getOrElse { runCatching { resp.bodyAsText() }.getOrNull() }
                    throw ApiException(resp.status, msg, msg)
                }
            }
        }

    }

// хелпер: это JSON?
private fun HttpResponse.isJson(): Boolean {
    val ct = this.contentType() ?: return false
    return ContentType.Application.Json.match(ct) ||
            (ct.contentType == "application" && ct.contentSubtype == "problem+json")
}

// если JSON — вытащим поле detail/title/error; иначе null
private suspend fun HttpResponse.problemTextOrNull(): String? =
    runCatching { if (isJson()) this.body<ProblemDto>() else null }.getOrNull()
        ?.let { it.detail ?: it.title ?: it.error }

// дефолтные user-friendly сообщения
private fun defaultUiMessage(status: HttpStatusCode): String = when (status) {
    HttpStatusCode.Unauthorized   -> "Сессия истекла. Войдите заново."
    HttpStatusCode.Forbidden      -> "Нет доступа."
    HttpStatusCode.BadRequest     -> "Некорректные данные."
    HttpStatusCode.NotFound       -> "Не найдено."
    HttpStatusCode.RequestTimeout -> "Таймаут запроса. Проверьте сеть."
    HttpStatusCode.InternalServerError,
    HttpStatusCode.ServiceUnavailable,
    HttpStatusCode.GatewayTimeout -> "Сервер недоступен. Попробуйте позже."
    else -> "Ошибка ${status.value}"
}