@file:Suppress("unused")

package ru.acs.grandmap.feature.asbDTO

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartPhoneRequestDto(
    @SerialName("phoneE164") val phoneE164: String,
    @SerialName("useCookies") val useCookies: Boolean = true,
    @SerialName("deviceId") val deviceId: String? = null,
    @SerialName("deviceTitle") val deviceTitle: String? = null,
    // @SerialName("Platform") val platform: Int? = null // можно не слать — сервер сам ставит Web
)

@Serializable
data class EmployeeDto(
    @SerialName("id") val id: String? = null,
    @SerialName("displayName") val displayName: String? = null,
)

@Serializable
data class StartPhoneResponseDto(
    @SerialName("employee") val employee: EmployeeDto? = null,
    @SerialName("sessionId") val sessionId: String? = null,

    @SerialName("accessToken") val accessToken: String,
    @SerialName("accessTokenExpiresAtUtc") val accessTokenExpUtc: String,

    @SerialName("refreshToken") val refreshToken: String? = null,        // null при UseCookies=true
    @SerialName("refreshTokenExpiresAtUtc") val refreshTokenExpUtc: String? = null,

    @SerialName("csrfToken") val csrfToken: String? = null               // только при UseCookies=true
)

@Serializable
data class RefreshResponseDto(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("accessTokenExpiresAtUtc") val accessTokenExpUtc: String
)
