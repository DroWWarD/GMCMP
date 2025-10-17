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
actual fun BackHandlerCompat(enabled: Boolean, onBack: () -> Unit) {
    DisposableEffect(enabled) {
        if (enabled) {
            EdgeBackManager.attach(onBack)
        } else {
            EdgeBackManager.detach()
        }
        onDispose { EdgeBackManager.detach() }
    }
}

/** Топ-левел менеджер (НЕ ObjC-класс) — можно хранить поля, всё ок */
private object EdgeBackManager {
    private var bridge: EdgeBackBridge? = null

    fun attach(onBack: () -> Unit) {
        if (bridge == null) bridge = EdgeBackBridge()
        bridge!!.attach(onBack)
    }

    fun detach() {
        bridge?.detach()
    }
}

/**
 * Сам ObjC-класс без companion.
 * Держит recognizer/окно/колбэк, перевешивает жест при смене key window.
 */
@ExportObjCClass
private class EdgeBackBridge : NSObject(), UIGestureRecognizerDelegateProtocol {

    private var recognizer: UIScreenEdgePanGestureRecognizer? = null
    private var window: UIWindow? = null
    private var onBack: (() -> Unit)? = null

    @OptIn(ExperimentalForeignApi::class)
    fun attach(callback: () -> Unit) {
        onBack = callback
        // следим за сменой key window
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
        installOnCurrentWindow()
    }

    private fun currentWindow(): UIWindow? {
        val app = UIApplication.sharedApplication
        return app.keyWindow ?: (app.windows.firstOrNull() as? UIWindow)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun installOnCurrentWindow() {
        val newWindow = currentWindow() ?: return
        if (newWindow == window && recognizer != null) return

        removeFromWindow()
        window = newWindow

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
        recognizer?.let { window?.removeGestureRecognizer(it) }
        recognizer = null
        window = null
    }

    // Основной хэндлер свайпа
    @OptIn(ExperimentalForeignApi::class)
    @ObjCAction
    fun handle(recognizer: UIScreenEdgePanGestureRecognizer) {
        val v = recognizer.view ?: return
        val dx = recognizer.translationInView(v).useContents { x }
        if (recognizer.state == UIGestureRecognizerStateEnded && dx > 24.0) {
            onBack?.invoke()
        }
    }

    // Разрешаем одновременное распознавание (чтобы не конфликтовало со скроллами/кликами)
    override fun gestureRecognizer(
        gestureRecognizer: UIGestureRecognizer,
        shouldRecognizeSimultaneouslyWithGestureRecognizer: UIGestureRecognizer
    ): Boolean = true

    // Подстраховка: не начинаем, если жест влево
    @OptIn(ExperimentalForeignApi::class)
    override fun gestureRecognizerShouldBegin(gestureRecognizer: UIGestureRecognizer): Boolean {
        val r = gestureRecognizer as? UIScreenEdgePanGestureRecognizer ?: return true
        val v = r.view ?: return true
        val dx = r.translationInView(v).useContents { x }
        return dx >= 0.0
    }
}