package ru.acs.grandmap.feature.auth.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class StartPhoneSmsRequestDto(
    @JsonNames("phoneE164","PhoneE164") val phoneE164: String
)

@Serializable
data class StartPhoneSmsResponseDto(
    @JsonNames("otpId") val otpId: String,
    @JsonNames("expiresAtUtc") val expiresAtUtc: String,
    @JsonNames("retryAfterSec") val retryAfterSec: Int? = null
)

@Serializable
data class ConfirmPhoneSmsRequestDto(
    @JsonNames("otpId","OtpId") val otpId: String,
    @JsonNames("code","Code") val code: String,
    @JsonNames("useCookies","UseCookies") val useCookies: Boolean,
    @JsonNames("deviceId","DeviceId") val deviceId: String? = null,
    @JsonNames("deviceTitle","DeviceTitle") val deviceTitle: String? = null,
    @JsonNames("platform","Platform") val platform: Int
)

@Serializable
data class EmployeeDto(
    @JsonNames("id","Id") val id: String,
    @JsonNames("organizationId","OrganizationId") val organizationId: String? = null,
    @JsonNames("affiliation","Affiliation") val affiliation: Int = 0,
    @JsonNames("displayName","DisplayName") val displayName: String? = null,
    @JsonNames("email","Email") val email: String? = null,
    @JsonNames("phoneE164","PhoneE164") val phoneE164: String? = null,
    @JsonNames("isActive","IsActive") val isActive: Boolean = true,
    @JsonNames("employeeNumber","EmployeeNumber") val employeeNumber: String? = null,
    @JsonNames("birthDate","BirthDate") val birthDate: String? = null,
    @JsonNames("hireDate","HireDate") val hireDate: String? = null,
    @JsonNames("terminationDate","TerminationDate") val terminationDate: String? = null,
    @JsonNames("departmentId","DepartmentId") val departmentId: String? = null,
    @JsonNames("jobTitle","JobTitle") val jobTitle: String? = null,
    @JsonNames("avatarPhotoId","AvatarPhotoId") val avatarPhotoId: String? = null,
)

@Serializable
data class ConfirmPhoneSmsResponseDto(
    @JsonNames("employee","Employee") val employee: EmployeeDto,
    @JsonNames("sessionId","SessionId") val sessionId: String,
    @JsonNames("accessToken","AccessToken") val accessToken: String,
    @JsonNames("accessTokenExpiresAtUtc","AccessTokenExpiresAtUtc") val accessTokenExpiresAtUtc: String? = null,
    @JsonNames("refreshToken","RefreshToken") val refreshToken: String? = null, // cookie-flow ⇒ обычно null
    @JsonNames("refreshTokenExpiresAtUtc","RefreshTokenExpiresAtUtc") val refreshTokenExpiresAtUtc: String? = null,
    @JsonNames("csrfToken","CsrfToken") val csrfToken: String? = null
)

@Serializable
data class GetTokensRequestDto(
    @JsonNames("refreshToken","RefreshToken")
    val refreshToken: String? = null
)

@Serializable
data class RefreshResponseDto(
    @JsonNames("accessToken","AccessToken") val accessToken: String,
    @JsonNames("accessTokenExpiresAtUtc","AccessTokenExpiresAtUtc") val accessTokenExpUtc: String? = null,
    @JsonNames("refreshToken","RefreshToken") val refreshToken: String? = null,
    @JsonNames("refreshTokenExpiresAtUtc","RefreshTokenExpiresAtUtc") val refreshTokenExpUtc: String? = null,
    @JsonNames("csrfToken","CsrfToken") val csrfToken: String? = null
)