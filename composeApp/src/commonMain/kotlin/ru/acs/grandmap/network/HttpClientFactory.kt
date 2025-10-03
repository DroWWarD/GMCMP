package ru.acs.grandmap.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.acs.grandmap.config.BASE_URL
import io.ktor.http.takeFrom

// Платформенный движок (OkHttp/Darwin/JS)
expect fun platformEngine(): HttpClientEngineFactory<*>

fun makeHttpClient(isDebug: Boolean = true): HttpClient = HttpClient(platformEngine()) {

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
}
