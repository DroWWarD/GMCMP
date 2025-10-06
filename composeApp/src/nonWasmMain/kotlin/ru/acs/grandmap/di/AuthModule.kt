package ru.acs.grandmap.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.acs.grandmap.config.BASE_URL
import ru.acs.grandmap.core.auth.TokenManager
import ru.acs.grandmap.core.auth.TokenStorage
import ru.acs.grandmap.data.auth.AuthApi

val authModule = module {
    single { AuthApi(get(qualifier = named("plain")), BASE_URL) }
    single { TokenManager(storage = get<TokenStorage>(), api = get<AuthApi>()) }
}
