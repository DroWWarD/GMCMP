// di/HttpModule.kt
package ru.acs.grandmap.di

import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.acs.grandmap.network.makeHttpClient

val httpModule = module {
    // "Голый" клиент для login/refresh
    single<HttpClient>(named("plain")) { makeHttpClient(isDebug = true, tokenManager = null) }

    // Клиент с bearer-логикой
    single<HttpClient>(named("authed")) { makeHttpClient(isDebug = true, tokenManager = get()) }

    // Удобный алиас по умолчанию — автhed
    single<HttpClient> { get(named("authed")) }
}
