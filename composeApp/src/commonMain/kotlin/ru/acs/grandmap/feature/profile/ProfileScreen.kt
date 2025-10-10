// ru.acs.grandmap.feature.profile.ProfileScreen.kt
package ru.acs.grandmap.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.acs.grandmap.feature.auth.dto.EmployeeDto
import ru.acs.grandmap.ui.common.MenuWideItem

sealed interface ProfileAction {
    data object Awards : ProfileAction
    data object Reviews : ProfileAction
    data object Photos : ProfileAction
    data object Calendar : ProfileAction
    data object HRDocs : ProfileAction
    data object Notifications : ProfileAction
    data object Learning : ProfileAction
    data object Wiki : ProfileAction
    data object Settings : ProfileAction
    data object Help : ProfileAction
    data object Feedback : ProfileAction
}

@Composable
fun ProfileScreen(
    employee: EmployeeDto?,
    onAction: (ProfileAction) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    if (employee == null) {
        Box(Modifier.fillMaxSize().padding(16.dp)) { Text("Профиль не загружен") }
        return
    }

    val outline = MaterialTheme.colorScheme.outline
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Заголовок
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(employee.displayName ?: "—", style = MaterialTheme.typography.titleLarge)
            Divider()
            Text("Телефон: ${employee.phoneE164 ?: "—"}")
            Text("Email: ${employee.email ?: "—"}")
            Text("Табельный №: ${employee.employeeNumber ?: "—"}")
            Text("Должность: ${employee.jobTitle ?: "—"}")
        }

        Spacer(Modifier.height(8.dp))

        // --- Сетка быстрых действий (по два в ряд) ---
        TwoTilesRow(
            left = { MenuTile(Icons.Filled.EmojiEvents, "Награды", "Достижения",
                onClick = { onAction(ProfileAction.Awards) }, shape = cardShape, outline = outline) },
            right = { MenuTile(Icons.Filled.Star, "Отзывы", "Обратная связь",
                onClick = { onAction(ProfileAction.Reviews) }, shape = cardShape, outline = outline) }
        )
        TwoTilesRow(
            left = { MenuTile(Icons.Filled.PhotoCamera, "Фото", "", onClick = {
                onAction(ProfileAction.Photos)
            }, shape = cardShape, outline = outline) },
            right = { MenuTile(Icons.Filled.CalendarMonth, "Календарь", "",
                onClick = { onAction(ProfileAction.Calendar) }, shape = cardShape, outline = outline) }
        )
        TwoTilesRow(
            left = { MenuTile(Icons.Filled.Badge, "HR и док-ты", "", onClick = {
                onAction(ProfileAction.HRDocs)
            }, shape = cardShape, outline = outline) },
            right = { MenuTile(Icons.Filled.Notifications, "Уведомления", "",
                onClick = { onAction(ProfileAction.Notifications) }, shape = cardShape, outline = outline) }
        )
        TwoTilesRow(
            left = { MenuTile(Icons.Filled.School, "Обучение", "", onClick = {
                onAction(ProfileAction.Learning)
            }, shape = cardShape, outline = outline) },
            right = { MenuTile(Icons.Filled.Search, "WIKI", "", onClick = {
                onAction(ProfileAction.Wiki)
            }, shape = cardShape, outline = outline) }
        )

        // --- Широкие пункты меню ---
        MenuWideItem(
            icon = Icons.Filled.Settings,
            title = "Настройки",
            onClick = { onAction(ProfileAction.Settings) },
        )
        MenuWideItem(
            icon = Icons.Filled.HelpOutline,
            title = "Помощь",
            onClick = { onAction(ProfileAction.Help) },
        )
        MenuWideItem(
            icon = Icons.Filled.PhoneInTalk,
            title = "Обратная связь",
            onClick = { onAction(ProfileAction.Feedback) },
        )

        // --- Выход (error-стиль) ---
        MenuWideItem(
            icon = Icons.Filled.Logout,
            title = "Выйти",
            onClick = onLogout,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor   = MaterialTheme.colorScheme.onErrorContainer
            ),
            leadingTint = MaterialTheme.colorScheme.onErrorContainer,
            trailingTint = MaterialTheme.colorScheme.onErrorContainer
        )

        Spacer(Modifier.height(8.dp))
        Text(
            "Версия 2.1.4",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)

        )
        Spacer(Modifier.height(64.dp)) // чтобы не упиралось в нижний бар
    }
}

/* ---------- UI helpers ---------- */

@Composable
private fun TwoTilesRow(
    left: @Composable () -> Unit,
    right: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.weight(1f)) { left() }
        Box(Modifier.weight(1f)) { right() }
    }
}

@Composable
private fun MenuTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    shape: RoundedCornerShape,
    outline: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 92.dp)
            .clickable { onClick() },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


