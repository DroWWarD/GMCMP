package ru.acs.grandmap.feature.sessions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.acs.grandmap.ui.common.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    component: SessionsComponent
) {
    val s by component.state

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Сеансы входа",
                onBack = component::back,
                // тумблер темы не нужен → onToggleTheme = null
                actions = {
                    if (!s.loading) {
                        IconButton(onClick = component::refresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Обновить"
                            )
                        }
                    }
                }
            )

        }
    ) { paddings ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddings)
                .padding(16.dp)
        ) {
            if (s.error != null) {
                Text(s.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            if (s.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(s.items) { item ->
                    SessionCard(
                        dto = item,
                        revoking = s.revokingId == item.id,
                        onRevoke = { component.revoke(item.id) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = component::revokeOthers,
                    modifier = Modifier.weight(1f)
                ) { Text("Завершить другие") }

                Button(
                    onClick = component::revokeAll,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Завершить все") }
            }
        }
    }
}

@Composable
private fun SessionCard(
    dto: SessionDto,
    revoking: Boolean,
    onRevoke: () -> Unit
) {
    val icon = when (dto.platform?.lowercase()) {
        "android" -> Icons.Filled.Android
        "ios"     -> Icons.Filled.Smartphone
        "desktop" -> Icons.Filled.Computer
        "web"     -> Icons.Filled.Language
        else      -> Icons.Filled.Computer
    }
    val active = dto.revokedAtUtc == null
    val subtitle = buildString {
        if (!dto.deviceTitle.isNullOrBlank()) append(dto.deviceTitle).append(" · ")
        dto.ipAddress?.let { append(it).append(" · ") }
        dto.userAgent?.takeIf { it.isNotBlank() }?.let { append(it) }
    }

    ElevatedCard {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        when {
                            dto.isCurrent -> "Текущее устройство"
                            active       -> "Активная сессия"
                            else         -> "Завершена"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    if (dto.isCurrent) AssistChip(onClick = {}, label = { Text("это вы") })
                    if (!active) AssistChip(onClick = {}, label = { Text("завершена") })
                }
                Spacer(Modifier.height(4.dp))
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Создано: ${dto.createdAtUtc} · Последняя активность: ${dto.lastUsedAtUtc ?: "—"} · Истекает: ${dto.refreshTokenExpiresAtUtc}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.width(12.dp))
            if (active) {
                val enabled = !dto.isCurrent && !revoking
                OutlinedButton(onClick = onRevoke, enabled = enabled) {
                    Text(if (revoking) "…" else "Завершить")
                }
            }
        }
    }
}
