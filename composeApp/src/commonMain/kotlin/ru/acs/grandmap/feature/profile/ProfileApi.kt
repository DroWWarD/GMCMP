package ru.acs.grandmap.feature.profile

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.get
import ru.acs.grandmap.feature.auth.dto.EmployeeDto

private const val PROFILE = "/api/Auth/profile"

class ProfileApi(
    private val client: HttpClient,
) {
    suspend fun getProfile(): EmployeeDto =
        client.get(PROFILE).body()
}