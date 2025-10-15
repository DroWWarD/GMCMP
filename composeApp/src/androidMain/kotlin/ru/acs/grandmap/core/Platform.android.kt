package ru.acs.grandmap.core

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

actual class KmpContext internal constructor(val context: Context)

@Composable
actual fun rememberKmpContext(): KmpContext =
    KmpContext(LocalContext.current)

actual fun openDialer(ctx: KmpContext, phone: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    ctx.context.startActivity(intent)
}

actual fun isDialerSupported(): Boolean = true

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
actual fun LockPortrait() {
    val activity = LocalContext.current.findActivity() ?: return
    DisposableEffect(activity) {
        val prev = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose { activity.requestedOrientation = prev }
    }
}