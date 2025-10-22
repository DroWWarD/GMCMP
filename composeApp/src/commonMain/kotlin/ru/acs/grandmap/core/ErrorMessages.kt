package ru.acs.grandmap.core

import io.ktor.client.plugins.*
import io.ktor.client.network.sockets.*
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import ru.acs.grandmap.network.ApiException

/** Возвращает безопасный для пользователя текст ошибки. */
fun Throwable.toUserMessage(): String = when (this) {
    is CancellationException -> "Отменено"
    // Бизнес-ошибки от сервера: ApiException уже содержит detail/title — показываем как есть
    is ApiException -> message ?: "Ошибка на сервере"
    // Сетевые и таймауты
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException -> "Ожидание ответа сервера"
    is IOException -> "Ожидание сети"
    else -> "Непредвиденная ошибка"
}
