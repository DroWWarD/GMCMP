package ru.acs.grandmap.core

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandlerCompat(
    enabled: Boolean = true,
    onBack: () -> Unit
)
