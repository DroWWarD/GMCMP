package ru.acs.grandmap.di

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import ru.acs.grandmap.data.auth.AuthRepository

@Composable
actual fun rememberAuthRepository(): AuthRepository = koinInject()
