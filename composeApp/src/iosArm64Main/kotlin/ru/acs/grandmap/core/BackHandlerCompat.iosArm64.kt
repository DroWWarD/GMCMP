package ru.acs.grandmap.core

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandlerCompat(
    enabled: Boolean,
    onBack: () -> Unit
) {
    //системной «назад» нет — ничего не делаем.
}