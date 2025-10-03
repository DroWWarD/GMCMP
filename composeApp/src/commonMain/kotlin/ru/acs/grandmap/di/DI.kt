package ru.acs.grandmap.di

import androidx.compose.runtime.Composable
import io.ktor.client.HttpClient

@Composable expect fun WithAppDI(content: @Composable () -> Unit)
@Composable expect fun rememberHttpClientDI(): HttpClient
