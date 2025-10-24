package ru.acs.grandmap.feature.profile.settings.sessions

import TopBarController
import TopBarSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.acs.grandmap.core.formatIsoHumanOrDash
import ru.acs.grandmap.ui.common.AppBarIconAction
import ru.acs.grandmap.ui.common.AppBarOverflowItem
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    component: SessionsComponent,
    topBar: TopBarController
) {
    val s by component.state

    LaunchedEffect(s.loading, s.error, s.items) {
        val active = s.items.count { it.revokedAtUtc == null }
        topBar.update(
            TopBarSpec(
                title = "Сеансы входа",
                subtitle = when {
                    s.loading -> "Обновляем список…"
                    s.error != null -> "Ошибка: ${s.error}"
                    else -> "Активных: $active · Всего: ${s.items.size}"
                },
                loading = s.loading,
                onBack = component::back,
                primary = listOf(
                    AppBarIconAction(
                        Icons.Default.Refresh,
                        "Обновить",
                        component::refresh,
                        enabled = !s.loading
                    )
                ),
                overflow = listOf(
                    AppBarOverflowItem(
                        "Завершить другие",
                        onClick = component::revokeOthers,
                        leadingIcon = Icons.Default.Devices
                    ),
                    AppBarOverflowItem(
                        "Завершить все",
                        onClick = component::revokeAll,
                        leadingIcon = Icons.Default.Delete,
                        danger = true
                    )
                ),
                visible = true
            )
        )
    }
    DisposableEffect(Unit) { onDispose { topBar.clear() } }

    Scaffold { paddings ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddings)
                .padding(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(s.items) { item ->
                    SessionCard(
                        dto = item,
                        revoking = s.revokingId == item.id,
                        onRevoke = { component.revoke(item.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun SessionCard(
    dto: SessionDto,
    revoking: Boolean,
    onRevoke: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val ty = MaterialTheme.typography

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
        dto.ipAddress?.takeIf { it.isNotBlank() }?.let { append(it).append(" · ") }
        dto.userAgent?.takeIf { it.isNotBlank() }?.let { append(it) }
    }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(modifier = Modifier
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                    //Аватар платформы
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(cs.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = cs.onPrimaryContainer)
                    }
//                    Spacer(Modifier.weight(1f)) // расталкивает в стороны
//                    StatusDot(color = when {
//                        dto.isCurrent -> cs.tertiary
//                        active        -> cs.primary
//                        else          -> cs.outline
//                    })

                    Spacer(Modifier.weight(1f)) // расталкивает в стороны
                    if (dto.isCurrent) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Это вы") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = cs.tertiaryContainer,
                                labelColor = cs.onTertiaryContainer
                            )
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    if (!active) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Завершена") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = cs.surfaceVariant,
                                labelColor = cs.onSurfaceVariant
                            )
                        )
                    } else if (!dto.isCurrent) {
                        val enabled =  !revoking
                        FilledTonalButton(
                            onClick = onRevoke,
                            enabled = enabled,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (enabled) cs.errorContainer else cs.surfaceVariant,
                                contentColor = if (enabled) cs.onErrorContainer else cs.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (revoking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Завершить")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                //Платформа
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = ty.bodySmall,
                        color = cs.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(6.dp))

                ProvideTextStyle(ty.labelSmall) {
                    Text(
                        "Создано: ${dto.createdAtUtc.formatIsoHumanOrDash()}",
                        color = cs.onSurfaceVariant
                    )
                    Text(
                        "Последняя активность: ${dto.lastUsedAtUtc.formatIsoHumanOrDash()}",
                        color = cs.onSurfaceVariant
                    )
                    Text(
                        "Истекает: ${dto.refreshTokenExpiresAtUtc.formatIsoHumanOrDash()}",
                        color = cs.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
        }
    }
}

@Composable
private fun StatusDot(color: Color, size: Dp = 8.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun SessionsContent(
    component: SessionsComponent,
    modifier: Modifier = Modifier
) {
    val s by component.state
    val active = s.items.count { it.revokedAtUtc == null }

    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Сеансы входа", style = MaterialTheme.typography.titleLarge)
                Text(
                    when {
                        s.loading -> "Обновляем список…"
                        s.error != null -> "Ошибка: ${s.error}"
                        else -> "Активных сеансов: $active"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = component::refresh, enabled = !s.loading) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        }

        if (s.loading && s.items.isEmpty()) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            items(s.items) { item ->
                SessionCard(
                    dto = item,
                    revoking = s.revokingId == item.id,
                    onRevoke = { component.revoke(item.id) }
                )
            }
        }
    }
}