package ru.acs.grandmap.feature.auth

actual fun defaultUseCookies() = false
actual fun platformCode() = 2
// Можно брать UIDevice.currentDevice.identifierForVendor?.UUIDString,
// но сохранённый UUID стабильнее и одинаков на всех платформах.
actual fun deviceId(): String? = "IOSSimulator"
actual fun deviceTitle(): String? = "IOS"