package ru.acs.grandmap.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ru.acs.grandmap.config.BASE_URL
import ru.acs.grandmap.data.auth.AuthApi
import ru.acs.grandmap.data.auth.AuthRepository
import ru.acs.grandmap.data.auth.AuthRepositoryImpl
import ru.acs.grandmap.network.makeHttpClient

@Composable
actual fun rememberAuthRepository(): AuthRepository {
    val client = remember { makeHttpClient(isDebug = true) }
    return remember { AuthRepositoryImpl(AuthApi(client, BASE_URL)) }
}
