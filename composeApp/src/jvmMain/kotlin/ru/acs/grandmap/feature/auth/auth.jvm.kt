package ru.acs.grandmap.feature.auth

actual fun defaultUseCookies() = false
actual fun platformCode() = 3
actual fun deviceId(): String? = ensureDeviceId()
actual fun deviceTitle(): String? = "Desktop"