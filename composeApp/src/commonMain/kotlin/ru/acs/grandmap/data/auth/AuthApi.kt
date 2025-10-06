package ru.acs.grandmap.data.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ru.acs.grandmap.core.auth.TokenPair
import kotlinx.serialization.Serializable

class AuthApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun ping(): String =
        client.get("$baseUrl/api/Auth/ping").body()

    @Serializable private data class RefreshReq(val refreshToken: String)
    @Serializable private data class RefreshResp(
        val accessToken: String,
        val refreshToken: String,
        val accessExpiresAt: Long? = null
    )

    suspend fun refresh(refreshToken: String): TokenPair {

        val resp: RefreshResp = client.post("$baseUrl/api/Auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshReq(refreshToken))
        }.body()
        return TokenPair(resp.accessToken, resp.refreshToken, resp.accessExpiresAt)
    }

}
