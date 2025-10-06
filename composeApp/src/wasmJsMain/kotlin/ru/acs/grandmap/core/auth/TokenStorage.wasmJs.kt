package ru.acs.grandmap.core.auth

class TokenStorageImpl : TokenStorage {
    private var pair: TokenPair? = null
    override fun read(): TokenPair? = pair
    override fun write(value: TokenPair?) { pair = value }
}
