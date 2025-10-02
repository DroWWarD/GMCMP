package ru.acs.grandmap.network
import io.ktor.client.engine.*
import io.ktor.client.engine.js.*
actual fun platformEngine(): HttpClientEngineFactory<*> = Js
