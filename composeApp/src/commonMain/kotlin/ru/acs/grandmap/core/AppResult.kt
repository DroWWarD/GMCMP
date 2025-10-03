package ru.acs.grandmap.core

sealed class AppResult<out T> {
    data class Ok<T>(val value: T): AppResult<T>()
    data class Err(val message: String, val cause: Throwable? = null): AppResult<Nothing>()
}
