package ru.acs.grandmap.feature.game.snake

import androidx.compose.runtime.Composable
import kotlinx.browser.window

@Composable
actual fun isTouchDevice(): Boolean = (window.navigator.maxTouchPoints ?: 0) > 0