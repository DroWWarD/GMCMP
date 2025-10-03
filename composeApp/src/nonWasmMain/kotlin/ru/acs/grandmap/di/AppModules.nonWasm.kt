package ru.acs.grandmap.di

import io.ktor.client.HttpClient
import org.koin.dsl.module
import ru.acs.grandmap.network.makeHttpClient

val appModule = module {
    single<HttpClient> { makeHttpClient(isDebug = true) }
}
