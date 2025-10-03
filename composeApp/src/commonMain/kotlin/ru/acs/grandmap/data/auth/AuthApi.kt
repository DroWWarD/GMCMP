package ru.acs.grandmap.data.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class AuthApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun ping(): String =
        client.get("$baseUrl/api/Auth/ping").body()
}
