package ru.acs.grandmap.core
import androidx.compose.runtime.Composable

expect class KmpContext
@Composable expect fun rememberKmpContext(): KmpContext

// открыть звонилку (где поддерживается)
expect fun openDialer(ctx: KmpContext, phone: String)

// платформа поддерживает вызов звонилки?
expect fun isDialerSupported(): Boolean

fun normalizePhone(phone: String): String =
    phone.filter { it.isDigit() || it == '+' }

@Composable expect fun LockPortrait()

