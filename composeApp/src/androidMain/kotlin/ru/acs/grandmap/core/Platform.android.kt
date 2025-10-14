package ru.acs.grandmap.core

import android.content.Intent
import android.net.Uri
import android.content.Context
import androidx.compose.runtime.Composable
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