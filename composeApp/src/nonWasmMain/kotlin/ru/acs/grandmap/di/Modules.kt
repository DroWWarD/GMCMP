package ru.acs.grandmap.di

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.*
import org.koin.dsl.module
import ru.acs.grandmap.config.BASE_URL
import ru.acs.grandmap.data.auth.AuthApi
import ru.acs.grandmap.data.auth.AuthRepository
import ru.acs.grandmap.data.auth.AuthRepositoryImpl
import ru.acs.grandmap.network.makeHttpClient

val networkModule = module {
    single<HttpClient> { makeHttpClient(isDebug = true) }
}

val repoModule = module {
    single { AuthApi(get(), BASE_URL) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}

fun initLoggingOnce() {
    // безопасно вызывать многократно
    runCatching { Napier.base(DebugAntilog()) }
}
