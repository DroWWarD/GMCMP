package ru.acs.grandmap.core.auth

import com.russhwolf.settings.Settings

private const val K_ACCESS  = "auth.access"
private const val K_REFRESH = "auth.refresh"
private const val K_EXP_AT  = "auth.access.expiresAt"

class TokenStorageImpl(
    private val settings: Settings = Settings()
) : TokenStorage {
    override fun read(): TokenPair? {
        val access  = settings.getStringOrNull(K_ACCESS) ?: return null
        val refresh = settings.getStringOrNull(K_REFRESH) ?: return null
        val exp     = settings.getLongOrNull(K_EXP_AT)
        return TokenPair(access, refresh, exp)
    }

    override fun write(value: TokenPair?) {
        if (value == null) {
            settings.remove(K_ACCESS)
            settings.remove(K_REFRESH)
            settings.remove(K_EXP_AT)
        } else {
            settings.putString(K_ACCESS, value.accessToken)
            settings.putString(K_REFRESH, value.refreshToken)
            value.accessExpiresAt?.let { settings.putLong(K_EXP_AT, it) } ?: settings.remove(K_EXP_AT)
        }
    }
}

