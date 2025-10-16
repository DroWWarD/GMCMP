package ru.acs.grandmap.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.UIKit.*
import platform.Foundation.NSSelectorFromString
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BackHandlerCompat(
    enabled: Boolean,
    onBack: () -> Unit
) {
    DisposableEffect(enabled) {
        if (!enabled) return@DisposableEffect onDispose {}

        val app = UIApplication.sharedApplication
        // keyWindow может быть null на iOS 13+, возьмём первый window как fallback
        val window: UIWindow? = app.keyWindow ?: (app.windows.firstOrNull() as? UIWindow)
        if (window == null) return@DisposableEffect onDispose {}

        val target = EdgePanTarget(onBack)
        val recognizer = UIScreenEdgePanGestureRecognizer(
            target = target,
            action = NSSelectorFromString("handle:")
        ).apply {
            edges = UIRectEdgeLeft
            cancelsTouchesInView = false // не ломаем клики/скроллы под жестом
        }

        window.addGestureRecognizer(recognizer)

        onDispose {
            window.removeGestureRecognizer(recognizer)
            // target удерживается recognizer-ом, отдельного dispose не нужно
        }
    }
}

@ExportObjCClass
private class EdgePanTarget(
    private val onBack: () -> Unit
) : NSObject() {

    @OptIn(ExperimentalForeignApi::class)
    @ObjCAction
    fun handle(recognizer: UIScreenEdgePanGestureRecognizer) {
        // Срабатываем на завершении жеста и достаточном сдвиге вправо
        if (recognizer.state == UIGestureRecognizerStateEnded) {
            val view = recognizer.view ?: return
            val dx = recognizer.translationInView(view).useContents { x }
            if (dx > 24.0) onBack() // порог в 24pt — не слишком «чувствительно»
        }
    }
}