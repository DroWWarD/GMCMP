// ru.acs.grandmap.di/DI.wasmJs.kt
package ru.acs.grandmap.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import ru.acs.grandmap.config.BASE_URL
import ru.acs.grandmap.core.auth.TokenManager
import ru.acs.grandmap.core.auth.TokenPair
import ru.acs.grandmap.core.auth.TokenStorage
import ru.acs.grandmap.data.auth.AuthApi
import ru.acs.grandmap.data.auth.AuthRepository
import ru.acs.grandmap.data.auth.AuthRepositoryImpl
import ru.acs.grandmap.network.makeHttpClient
import kotlinx.browser.window
import ru.acs.grandmap.core.auth.CsrfStorage
import ru.acs.grandmap.core.auth.WasmCsrfStorage

@Composable
actual fun WithAppDI(content: @Composable () -> Unit) {
    // На WASM без Koin — просто рендерим
    content()
}

/** Единственный клиент с Bearer для приложения */
@Composable
actual fun rememberHttpClientDI(): HttpClient = remember { WasmDI.authed }

/** Единственный TokenManager */
@Composable
actual fun rememberTokenManagerDI(): TokenManager = remember { WasmDI.tokenManager }

/** AuthRepository использует PLAIN клиент через AuthApi */
@Composable
actual fun rememberAuthRepository(): AuthRepository =
    remember { AuthRepositoryImpl(WasmDI.authApi) }

/* -------------------------------------------------------------------- */

private object WasmDI {
    // 1) Хранилище токенов в localStorage
    private val storage: TokenStorage = object : TokenStorage {
        private val K_ACCESS  = "auth.access"
        private val K_REFRESH = "auth.refresh"
        private val K_EXP_AT  = "auth.access.expiresAt"

        override fun read(): TokenPair? {
            val access  = window.localStorage.getItem(K_ACCESS) ?: return null
            val refresh = window.localStorage.getItem(K_REFRESH) ?: return null
            val exp     = window.localStorage.getItem(K_EXP_AT)?.toLongOrNull()
            return TokenPair(access, refresh, exp)
        }
        override fun write(value: TokenPair?) {
            if (value == null) {
                window.localStorage.removeItem(K_ACCESS)
                window.localStorage.removeItem(K_REFRESH)
                window.localStorage.removeItem(K_EXP_AT)
            } else {
                window.localStorage.setItem(K_ACCESS, value.accessToken)
                window.localStorage.setItem(K_REFRESH, value.refreshToken)
                if (value.accessExpiresAt != null)
                    window.localStorage.setItem(K_EXP_AT, value.accessExpiresAt.toString())
                else
                    window.localStorage.removeItem(K_EXP_AT)
            }
        }
    }

    // 2) PLAIN клиент (без bearer) — только для login/refresh
    val plain: HttpClient = makeHttpClient(isDebug = true, tokenManager = null)

    // 3) AuthApi, который ходит PLAIN-клиентом
    val authApi: AuthApi = AuthApi(plain, BASE_URL)

    // 4) TokenManager, который будет обновлять токены через authApi
    private val csrfStorage: CsrfStorage = WasmCsrfStorage()

    val tokenManager: TokenManager by lazy {
        TokenManager(
            storage = storage,
            api = authApi,
            csrfStorage = csrfStorage
        )
    }

    // 5) AUTHeD клиент — со встроенным bearer и привязанным tokenManager
    val authed: HttpClient = makeHttpClient(isDebug = true, tokenManager = tokenManager)

}
