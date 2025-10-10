package ru.acs.grandmap.data.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ru.acs.grandmap.core.auth.TokenPair
import kotlinx.serialization.Serializable
import ru.acs.grandmap.feature.auth.dto.EmployeeDto
import ru.acs.grandmap.feature.auth.dto.GetTokensRequestDto
import ru.acs.grandmap.feature.auth.dto.RefreshResponseDto

class AuthApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val AUTH = "$baseUrl/api/Auth"
    private val REFRESH_V2 = "$AUTH/refresh-v2"
    private val PROFILE    = "$AUTH/profile"
    suspend fun ping(): String =
        client.get("$baseUrl/api/Auth/ping").body()

    /** cookie-flow: refresh берём из HttpOnly cookie, X-CSRF — из параметра */
    suspend fun refreshViaCookie(csrf: String?): RefreshResponseDto =
        client.post(REFRESH_V2) {
            contentType(ContentType.Application.Json)
            csrf?.let { header("X-CSRF", it) }
            setBody(GetTokensRequestDto(null))
        }.body()

    /** token-flow: refresh строкой в теле */
    suspend fun refreshViaToken(refresh: String): RefreshResponseDto =
        client.post(REFRESH_V2) {
            contentType(ContentType.Application.Json)
            setBody(GetTokensRequestDto(refresh))
        }.body()

    suspend fun getProfile(): EmployeeDto =
        client.get(PROFILE).body()

    @Serializable private data class RefreshReq(val refreshToken: String)
    @Serializable private data class RefreshResp(
        val accessToken: String,
        val refreshToken: String,
        val accessExpiresAt: Long? = null
    )
}
