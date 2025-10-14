package ru.acs.grandmap.feature.auth

actual fun defaultUseCookies() = true
actual fun platformCode() = 4
actual fun deviceId(): String? = ensureDeviceId()  // сохраняется в localStorage
actual fun deviceTitle(): String? = "Web/Wasm"