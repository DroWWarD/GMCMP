package ru.acs.grandmap.network

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual fun platformEngine(): HttpClientEngineFactory<*> = Darwin
