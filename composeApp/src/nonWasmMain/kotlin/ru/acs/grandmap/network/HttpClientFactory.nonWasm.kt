package ru.acs.grandmap.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.*

actual fun HttpClientConfig<*>.enablePlatformCookies() { /* no-op */ }