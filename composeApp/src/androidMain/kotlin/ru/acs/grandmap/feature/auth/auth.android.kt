package ru.acs.grandmap.feature.auth

actual fun defaultUseCookies() = false
actual fun platformCode() = 1
// Можно было бы брать ANDROID_ID, но для простоты и без контекста — используем сохранённый UUID
actual fun deviceId(): String? = ensureDeviceId()
actual fun deviceTitle(): String? = "Android"