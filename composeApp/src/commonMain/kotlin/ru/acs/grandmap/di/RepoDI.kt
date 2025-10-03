package ru.acs.grandmap.di

import androidx.compose.runtime.Composable
import ru.acs.grandmap.data.auth.AuthRepository

@Composable expect fun rememberAuthRepository(): AuthRepository
