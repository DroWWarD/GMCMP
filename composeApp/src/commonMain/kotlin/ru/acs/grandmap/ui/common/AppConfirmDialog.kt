package ru.acs.grandmap.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.acs.grandmap.core.BackHandlerCompat // у тебя уже есть

@Composable
fun AppConfirmDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String? = null,
    confirmText: String = "Подтвердить",
    cancelText: String = "Отмена",
    icon: ImageVector? = null,
    danger: Boolean = false,                // делает кнопку «подтвердить» красной
) {
    if (!visible) return

    // Закрываем по back/escape
    BackHandlerCompat(enabled = true) { onDismissRequest() }

    // Фон-скрим
    val scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)
    val noIndication = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "confirm-dialog" }
    ) {
        // затемнение
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(scrimColor)
                    .clickable(
                        interactionSource = noIndication,
                        indication = null
                    ) { onDismissRequest() }
            )
        }

        // карточка
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 10 }, animationSpec = tween(180)) + fadeIn(tween(180)),
            exit = slideOutVertically(targetOffsetY = { it / 10 }, animationSpec = tween(180)) + fadeOut(tween(120))
        ) {
            Surface(
                tonalElevation = 6.dp,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp)
                    .widthIn(max = 520.dp)
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (icon != null) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (danger) MaterialTheme.colorScheme.error else LocalContentColor.current
                        )
                    }
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!message.isNullOrBlank()) {
                        Text(
                            message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f)
                        ) { Text(cancelText) }

                        val confirmColors = if (danger) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            colors = confirmColors
                        ) { Text(confirmText) }
                    }
                }
            }
        }
    }
}
