package ru.acs.grandmap.di

import androidx.compose.runtime.Composable
import io.ktor.client.HttpClient
import ru.acs.grandmap.core.auth.TokenManager

@Composable expect fun WithAppDI(content: @Composable () -> Unit)
@Composable expect fun rememberHttpClientDI(): HttpClient
@Composable
expect fun rememberTokenManagerDI(): TokenManager