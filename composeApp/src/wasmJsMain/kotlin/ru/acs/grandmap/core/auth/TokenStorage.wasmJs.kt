package ru.acs.grandmap.core.auth
import kotlinx.browser.document
import kotlinx.browser.window

class TokenStorageImpl : TokenStorage {
    private var pair: TokenPair? = null
    override fun read(): TokenPair? = pair
    override fun write(value: TokenPair?) { pair = value }
}
//val wasmCsrfProvider = CsrfProvider {
//    val raw = document.cookie ?: return@CsrfProvider null
//    raw.split(';')
//        .map { it.trim() }
//        .firstOrNull { it.startsWith("gma_csrf=") }
//        ?.substringAfter('=')
//}

class WasmCsrfStorage : CsrfStorage {
    private val K = "auth.csrf"
    override fun read(): String? = window.localStorage.getItem(K)
    override fun write(value: String?) {
        if (value == null) window.localStorage.removeItem(K)
        else window.localStorage.setItem(K, value)
    }
}