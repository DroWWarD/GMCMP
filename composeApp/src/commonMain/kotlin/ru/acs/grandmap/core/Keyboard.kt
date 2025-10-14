package ru.acs.grandmap.core


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

expect fun hideKeyboardPlatform()

@Composable
fun rememberHideKeyboard(): () -> Unit {
    val fm = LocalFocusManager.current
    return remember(fm) { { fm.clearFocus(force = true); hideKeyboardPlatform() } }
}

fun Modifier.dismissKeyboardOnTap(): Modifier = composed {
    val fm: FocusManager = LocalFocusManager.current
    pointerInput(Unit) {
        detectTapGestures(onTap = {
            fm.clearFocus(force = true)
            hideKeyboardPlatform()
        })
    }
}