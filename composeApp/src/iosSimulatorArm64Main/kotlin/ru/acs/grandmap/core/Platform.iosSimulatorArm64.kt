package ru.acs.grandmap.core

import androidx.compose.runtime.Composable
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class KmpContext

@Composable
actual fun rememberKmpContext(): KmpContext = KmpContext()

actual fun openDialer(ctx: KmpContext, phone: String) {
    val url = NSURL(string = "tel://$phone") ?: return
    val app = UIApplication.sharedApplication
    if (app.canOpenURL(url)) app.openURL(url)
}

actual fun isDialerSupported(): Boolean = true