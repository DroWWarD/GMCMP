package ru.acs.grandmap.feature.sessions

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import ru.acs.grandmap.core.auth.TokenManager

private const val SESSIONS = "/api/profile/sessions"

@Serializable
data class SessionDto(
    val id: String,
    val deviceId: String? = null,
    val deviceTitle: String? = null,
    val platform: String? = null,
    val userAgent: String? = null,
    val ipAddress: String? = null,
    val createdAtUtc: String,
    val lastUsedAtUtc: String? = null,
    val refreshTokenExpiresAtUtc: String,
    val revokedAtUtc: String? = null,
    val revocationReason: String? = null,
    val isCurrent: Boolean = false,
)

class SessionsApi(
    private val client: HttpClient,
    private val tokenManager: TokenManager
) {
    private fun HttpRequestBuilder.csrfIfAny() {
        tokenManager.currentCsrfOrNull()?.let { header("X-CSRF", it) }
    }

    suspend fun list(): List<SessionDto> =
        client.get(SESSIONS) { csrfIfAny() }.body()

    suspend fun revoke(id: String, reason: String? = null) {
        client.post("$SESSIONS/$id/revoke") {
            csrfIfAny()
            contentType(ContentType.Application.Json)
            setBody(reason?.let { mapOf("reason" to it) } ?: emptyMap<String, String>())
        }
    }

    suspend fun revokeOthers() {
        client.post("$SESSIONS/revoke-others") { csrfIfAny() }
    }

    suspend fun revokeAll() {
        client.post("$SESSIONS/revoke-all") { csrfIfAny() }
    }
}
