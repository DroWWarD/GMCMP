package ru.acs.grandmap.core

import androidx.compose.runtime.Composable

actual class KmpContext
@Composable actual fun rememberKmpContext(): KmpContext = KmpContext()

actual fun openDialer(ctx: KmpContext, phone: String) { /* no-op */ }
actual fun isDialerSupported(): Boolean = false