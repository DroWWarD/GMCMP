package ru.acs.grandmap.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.*
import platform.darwin.NSObject

@Composable
actual fun BackHandlerCompat(
    enabled: Boolean,
    onBack: () -> Unit
) {
    DisposableEffect(enabled) {
        if (enabled) {
            EdgeBackBridge.shared.attach(onBack)
        } else {
            EdgeBackBridge.shared.detach()
        }
        onDispose { EdgeBackBridge.shared.detach() }
    }
}

/**
 * Синглтон-мост, устойчивый к смене окна/сцены и рекомпозициям.
 */
@ExportObjCClass
private class EdgeBackBridge : NSObject(), UIGestureRecognizerDelegateProtocol {

    private var recognizer: UIScreenEdgePanGestureRecognizer? = null
    private var window: UIWindow? = null
    private var onBack: (() -> Unit)? = null

    companion object {
        val shared = EdgeBackBridge()
    }

    @OptIn(ExperimentalForeignApi::class)
    fun attach(callback: () -> Unit) {
        onBack = callback
        // Подписываемся на смену keyWindow — на iOS 13+ это частая история
        NSNotificationCenter.defaultCenter.addObserver(
            this,
            selector = NSSelectorFromString("onWindowChange"),
            name = UIWindowDidBecomeKeyNotification,
            `object` = null
        )
        installOnCurrentWindow()
    }

    fun detach() {
        removeFromWindow()
        onBack = null
        NSNotificationCenter.defaultCenter.removeObserver(this)
    }

    @ObjCAction
    fun onWindowChange() {
        // keyWindow сменилось — перевесим распознаватель
        installOnCurrentWindow()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun installOnCurrentWindow() {
        val app = UIApplication.sharedApplication
        val newWindow: UIWindow? = app.keyWindow ?: (app.windows.firstOrNull() as? UIWindow)
        if (newWindow == window && recognizer != null) return

        removeFromWindow()
        window = newWindow ?: return

        val r = UIScreenEdgePanGestureRecognizer(
            target = this,
            action = NSSelectorFromString("handle:")
        ).apply {
            edges = UIRectEdgeLeft
            cancelsTouchesInView = false
            delegate = this@EdgeBackBridge
        }
        window!!.addGestureRecognizer(r)
        recognizer = r
    }

    private fun removeFromWindow() {
        recognizer?.let { rec ->
            window?.removeGestureRecognizer(rec)
        }
        recognizer = null
        window = null
    }

    // Основная логика: реагируем только на законченный свайп справа-налево с достаточным dx
    @OptIn(ExperimentalForeignApi::class)
    @ObjCAction
    fun handle(recognizer: UIScreenEdgePanGestureRecognizer) {
        val view = recognizer.view ?: return
        val translation = recognizer.translationInView(view)
        val dx = translation.useContents { x }
        when (recognizer.state) {
            UIGestureRecognizerStateEnded -> {
                // Порог смещения 24pt, можно подстроить
                if (dx > 24.0) onBack?.invoke()
            }
            else -> Unit
        }
    }

    // Разрешаем одновременно распознавать с жестами Compose (скролл и т.п.)
    override fun gestureRecognizer(
        gestureRecognizer: UIGestureRecognizer,
        shouldRecognizeSimultaneouslyWithGestureRecognizer: UIGestureRecognizer
    ): Boolean = true

    // Не начинаем, если движение влево или старт не у левого края (подстраховка)
    @OptIn(ExperimentalForeignApi::class)
    override fun gestureRecognizerShouldBegin(gestureRecognizer: UIGestureRecognizer): Boolean {
        val rec = gestureRecognizer as? UIScreenEdgePanGestureRecognizer ?: return true
        val v = rec.view ?: return true
        val tr = rec.translationInView(v)
        val dx = tr.useContents { x }
        return dx >= 0.0
    }
}
