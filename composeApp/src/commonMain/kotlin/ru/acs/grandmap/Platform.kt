package ru.acs.grandmap

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform