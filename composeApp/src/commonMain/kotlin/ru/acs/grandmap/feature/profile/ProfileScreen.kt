// ru.acs.grandmap.feature.profile.ProfileScreen.kt
package ru.acs.grandmap.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.acs.grandmap.feature.auth.dto.EmployeeDto
import ru.acs.grandmap.ui.common.*

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
            left = { MenuItem(Icons.Filled.EmojiEvents, "Награды",
                onClick = { onAction(ProfileAction.Awards) }, shape = cardShape) },
            right = { MenuItem(Icons.Filled.Star, "Отзывы",
                onClick = { onAction(ProfileAction.Reviews) }, shape = cardShape) }
        )
        TwoTilesRow(
            left = { MenuItem(Icons.Filled.PhotoCamera, "Фото", onClick = {
                onAction(ProfileAction.Photos)
            }, shape = cardShape) },
            right = { MenuItem(Icons.Filled.CalendarMonth, "Календарь",
                onClick = { onAction(ProfileAction.Calendar) }, shape = cardShape) }
        )
        TwoTilesRow(
            left = { MenuItem(Icons.Filled.Badge, "HR и док-ты", onClick = {
                onAction(ProfileAction.HRDocs)
            }, shape = cardShape,) },
            right = { MenuItem(Icons.Filled.Notifications, "Уведомления",
                onClick = { onAction(ProfileAction.Notifications) }, shape = cardShape) }
        )
        TwoTilesRow(
            left = { MenuItem(Icons.Filled.School, "Обучение", onClick = {
                onAction(ProfileAction.Learning)
            }, shape = cardShape) },
            right = { MenuItem(Icons.Filled.Search, "WIKI", onClick = {
                onAction(ProfileAction.Wiki)
            }, shape = cardShape) }
        )

        // --- Широкие пункты меню ---
        MenuItem(
            icon = Icons.Filled.Settings,
            title = "Настройки",
            onClick = { onAction(ProfileAction.Settings) },
        )
        MenuItem(
            icon = Icons.Filled.HelpOutline,
            title = "Помощь",
            onClick = { onAction(ProfileAction.Help) },
        )
        MenuItem(
            icon = Icons.Filled.PhoneInTalk,
            title = "Обратная связь",
            onClick = { onAction(ProfileAction.Feedback) },
        )

        // --- Выход (error-стиль) ---
        MenuItem(
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
