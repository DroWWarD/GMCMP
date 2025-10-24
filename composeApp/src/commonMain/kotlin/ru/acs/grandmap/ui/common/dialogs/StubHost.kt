package ru.acs.grandmap.ui.common.dialogs

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Icon

data class StubState(
    val visible: Boolean = false,
    val title: String? = null,
    val message: String = "",
    val confirmLabel: String = "Ок",
    val dismissLabel: String? = null,
    val icon: ImageVector? = null
)

@Stable
class StubController internal constructor(
    internal val state: MutableState<StubState>
) {
    fun show(
        message: String,
        title: String? = null,
        confirmLabel: String = "Ок",
        dismissLabel: String? = null,
        icon: ImageVector? = null
    ) {
        state.value = StubState(
            visible = true,
            title = title,
            message = message,
            confirmLabel = confirmLabel,
            dismissLabel = dismissLabel,
            icon = icon
        )
    }

    fun hide() {
        state.value = state.value.copy(visible = false)
    }
}

/** Храним контроллер на экране. */
@Composable
fun rememberStubController(): StubController {
    val s = remember { mutableStateOf(StubState()) }
    return remember { StubController(s) }
}

/** Хост диалога: повесь рядом с `Scaffold`/корнем экрана. */
@Composable
fun StubHost(
    controller: StubController,
    modifier: Modifier = Modifier,
    onConfirm: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    val st = controller.state.value
    if (!st.visible) return

    AlertDialog(
        onDismissRequest = {
            controller.hide()
            onDismiss?.invoke()
        },
        icon = {
            st.icon?.let { Icon(it, contentDescription = null) }
        },
        title = {
            st.title?.let {
                Text(
                    text = it,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Text(
                text = st.message,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(onClick = {
                controller.hide()
                onConfirm?.invoke()
            }) { Text(st.confirmLabel) }
        },
        dismissButton = {
            st.dismissLabel?.let {
                TextButton(onClick = { controller.hide(); onDismiss?.invoke() }) { Text(it) }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true)
    )
}

/** Удобный хелпер: получаем лямбду для прямой передачи в onClick. */
fun StubController.click(
    message: String,
    title: String? = null,
    confirmLabel: String = "Ок",
    dismissLabel: String? = null,
    icon: ImageVector? = null
): () -> Unit = {
    show(
        message = message,
        title = title,
        confirmLabel = confirmLabel,
        dismissLabel = dismissLabel,
        icon = icon
    )
}
