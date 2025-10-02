package ru.acs.grandmap.network

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

private const val PING_PATH = "/api/Auth/ping"

/** Возвращает "200 OK: <тело>" или текст ошибки */
suspend fun ping(client: HttpClient): String {
    val resp = client.get(PING_PATH)
    val text = runCatching { resp.body<String>() }.getOrElse { resp.bodyAsText() }
    return "${resp.status.value} ${resp.status.description}: $text"
}
