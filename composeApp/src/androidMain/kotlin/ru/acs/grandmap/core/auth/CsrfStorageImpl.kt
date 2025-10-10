package ru.acs.grandmap.core.auth

import com.russhwolf.settings.Settings

class CsrfStorageImpl(private val settings: Settings = Settings()) : CsrfStorage {
    private val K = "auth.csrf"
    override fun read(): String? = settings.getStringOrNull(K)
    override fun write(value: String?) {
        if (value == null) settings.remove(K) else settings.putString(K, value)
    }
}