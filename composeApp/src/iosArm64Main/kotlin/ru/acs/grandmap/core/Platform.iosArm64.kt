package ru.acs.grandmap.core


import androidx.compose.runtime.Composable
import platform.Foundation.NSURL
import platform.Foundation.NSURLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIApplication

actual class KmpContext // заглушка, контекст не нужен

@Composable
actual fun rememberKmpContext(): KmpContext = KmpContext()

actual fun openDialer(ctx: KmpContext, phone: String) {
    // iOS ожидает tel://<digits>; эскейпнем на всякий случай
    val encoded = phone.stringByAddingPercentEncodingWithAllowedCharacters(
        NSURLQueryAllowedCharacterSet
    ) ?: phone
    val url = NSURL(string = "tel://$encoded") ?: return

    val app = UIApplication.sharedApplication
    if (app.canOpenURL(url)) {
        // Достаточно и этого вызова; на девайсе откроет dialer
        app.openURL(url)
        // Для iOS 10+ можно так:
        // app.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}