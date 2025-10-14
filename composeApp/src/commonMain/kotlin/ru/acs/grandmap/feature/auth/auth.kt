package ru.acs.grandmap.feature.auth

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlin.random.Random
expect fun defaultUseCookies(): Boolean        // wasm=true, android=false
expect fun platformCode(): Int                 // Web=4, Android=1, iOS=2, Desktop=3
expect fun deviceId(): String?
expect fun deviceTitle(): String?

private const val DEVICE_ID_KEY = "device.id"

// Генератор псевдо-UUID (без зависимостей)
private fun randomUuid(): String {
    val bytes = Random.Default.nextBytes(16)
    // Простейший hex (xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)
    val hex = "0123456789abcdef"
    fun b(i: Int) = bytes[i].toInt() and 0xFF
    fun h(v: Int) = charArrayOf(hex[v ushr 4], hex[v and 0x0F])
    val out = StringBuilder(36)
    for (i in 0 until 16) {
        if (i in intArrayOf(4, 6, 8, 10)) out.append('-')
        out.append(h(b(i)))
    }
    return out.toString()
}

/** Возвращает сохранённый deviceId, либо создаёт и сохраняет новый. */
fun ensureDeviceId(): String {
    val s = Settings()
    val cached = s.getStringOrNull(DEVICE_ID_KEY)
    if (cached != null) return cached
    val id = randomUuid()
    s[DEVICE_ID_KEY] = id
    return id
}