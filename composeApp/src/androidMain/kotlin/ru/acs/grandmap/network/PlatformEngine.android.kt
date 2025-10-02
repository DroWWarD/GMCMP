package ru.acs.grandmap.network
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
actual fun platformEngine(): HttpClientEngineFactory<*> = OkHttp
