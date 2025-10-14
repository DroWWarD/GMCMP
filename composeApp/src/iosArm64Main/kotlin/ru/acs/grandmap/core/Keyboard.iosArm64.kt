package ru.acs.grandmap.core

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.objc.sel_registerName

@OptIn(ExperimentalForeignApi::class)
actual fun hideKeyboardPlatform() {
    UIApplication.sharedApplication.sendAction(
        action  = sel_registerName("resignFirstResponder"),
        to      = null,   // ← НЕ target, а to
        from    = null,
        forEvent = null
    )
}