package ru.acs.grandmap.core


import androidx.compose.runtime.Composable
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual class KmpContext

@Composable
actual fun rememberKmpContext(): KmpContext = KmpContext()

actual fun openDialer(ctx: KmpContext, phone: String) {
    val digits = phone.filter { it.isDigit() || it == '+' }
    val url = NSURL(string = "tel://$digits") ?: return
    val app = UIApplication.sharedApplication

    // На всякий случай — на главном потоке
    dispatch_async(dispatch_get_main_queue()) {
        if (app.canOpenURL(url)) {
            // НОВЫЙ API: open(_:options:completionHandler:)
            app.openURL(
                url,
                options = emptyMap<Any?, Any>(),
                completionHandler = null
            )
        }
    }
}

actual fun isDialerSupported(): Boolean = true