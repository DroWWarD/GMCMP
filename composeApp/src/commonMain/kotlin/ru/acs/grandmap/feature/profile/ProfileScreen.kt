package ru.acs.grandmap.feature.profile

import TopBarController
import TopBarSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.acs.grandmap.ui.common.AppConfirmDialog
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import ru.acs.grandmap.ui.common.MenuItem
import ru.acs.grandmap.ui.common.TwoTilesRow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
fun ProfileScreen(
    component: ProfileComponent,
    topBar: TopBarController
) {
    val s by component.uiState
    var showLogout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        topBar.update(TopBarSpec(title = "Профиль", visible = true))
    }

    Box(Modifier.fillMaxSize()) {                    // ← корневой слой экрана

        // ======= ОСНОВНОЙ КОНТЕНТ (первым — будет под модалкой) =======
        val scroll = rememberScrollState()
        val shape = RoundedCornerShape(16.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeaderCard(
                loading = s.loading,
                lastSync = s.lastSync,
                onRefresh = component::refresh
            )
            InfoSection(
                loading = s.loading && s.employee == null,
                name = s.employee?.displayName,
                phone = s.employee?.phoneE164,
                email = s.employee?.email,
                number = s.employee?.employeeNumber,
                title = s.employee?.jobTitle
            )
            TwoTilesRow(
                left  = { MenuItem(Icons.Filled.EmojiEvents, "Награды", onClick = { /* TODO */ }, shape = shape) },
                right = { MenuItem(Icons.Filled.Star,        "Отзывы",  onClick = { /* TODO */ }, shape = shape) },
            )
            TwoTilesRow(
                left  = { MenuItem(Icons.Filled.PhotoCamera, "Фото",     onClick = { /* TODO */ }, shape = shape) },
                right = { MenuItem(Icons.Filled.CalendarMonth,"Календарь",onClick = { /* TODO */ }, shape = shape) },
            )
            TwoTilesRow(
                left  = { MenuItem(Icons.Filled.Badge, "HR и док-ты", onClick = { /* TODO */ }, shape = shape) },
                right = { MenuItem(Icons.Filled.Notifications, "Уведомления", onClick = { /* TODO */ }, shape = shape) },
            )
            TwoTilesRow(
                left  = { MenuItem(Icons.Filled.School, "Обучение", onClick = { /* TODO */ }, shape = shape) },
                right = { MenuItem(Icons.Filled.Search, "WIKI",     onClick = { /* TODO */ }, shape = shape) },
            )

            MenuItem(Icons.Filled.TableChart, "Инспектор БД", onClick = component::showDbInspector)

            if (s.dbVisible) {
                DbInspectorDialog(
                    rows = s.dbRows,
                    onRefresh = component::refreshDbInspector,
                    onClear = component::clearDbInspector,
                    onClose = component::hideDbInspector,
                    onSync = component::syncAndRefreshDbInspector
                )
            }

            MenuItem(Icons.Filled.Settings, "Настройки", onClick = component::openSettings)
            MenuItem(Icons.Filled.Devices,  "Сеансы на устройствах", onClick = component::openSessions)

            // ВЫХОД → открываем модалку
            MenuItem(
                icon = Icons.Filled.Logout,
                title = "Выйти",
                onClick = { showLogout = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor   = MaterialTheme.colorScheme.onErrorContainer
                ),
                leadingTint = MaterialTheme.colorScheme.onErrorContainer,
                trailingTint = MaterialTheme.colorScheme.onErrorContainer
            )

            if (s.error != null && s.employee == null) {
                Text(
                    s.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedButton(onClick = { component::refresh }) { Text("Повторить") }
            }

            Spacer(Modifier.height(64.dp))
        }

        // ======= МОДАЛКА (последним слоем — окажется поверх и центрируется на весь экран) =======
        AppConfirmDialog(
            visible = showLogout,
            onDismissRequest = { showLogout = false },
            onConfirm = {
                showLogout = false
                component.logOut()
            },
            title = "Выйти из приложения?",
            message = "Текущая сессия будет завершена. Вы сможете войти снова.",
            confirmText = "Выйти",
            cancelText  = "Отмена",
            icon = Icons.Filled.Logout,
            danger = true
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun HeaderCard(
    loading: Boolean,
    lastSync: Instant?,
    onRefresh: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Профиль", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                val info = when {
                    loading && lastSync == null -> "Загружаем…"
                    loading && lastSync != null -> "Обновляем…"
                    lastSync != null -> "Обновлено ${formatAgo(lastSync)}"
                    else -> "Нет данных"
                }
                Text(
                    info,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            FilledTonalIconButton(
                onClick = onRefresh,
                enabled = !loading
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Обновить")
            }
        }
        if (loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun InfoSection(
    loading: Boolean,
    name: String?,
    phone: String?,
    email: String?,
    number: String?,
    title: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoRow(label = "ФИО", value = name, loading = loading)
        Divider()
        InfoRow(label = "Телефон", value = phone, loading = loading)
        InfoRow(label = "Email", value = email, loading = loading)
        InfoRow(label = "Табельный №", value = number, loading = loading)
        InfoRow(label = "Должность", value = title, loading = loading)
    }
}

@Composable
private fun InfoRow(label: String, value: String?, loading: Boolean) {
    val placeholderHeight = 18.dp
    val placeholder = @Composable {
        Box(
            Modifier
                .fillMaxWidth(0.6f)
                .height(placeholderHeight)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                .alpha(0.7f)
        )
    }
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.width(130.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        if (loading) {
            placeholder()
        } else {
            Text(value ?: "—", modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun formatAgo(instant: Instant): String {
    // очень простой релятив: "только что", "N мин назад", "N ч назад"
    val now = Clock.System.now()
    val diff = now - instant
    return when {
        diff < 30.seconds -> "только что"
        diff < 60.minutes -> "${(diff.inWholeMinutes).coerceAtLeast(1)} мин назад"
        else -> "${(diff.inWholeHours)} ч назад"
    }
}

@Composable
fun DbInspectorDialog(
    rows: List<ProfileRepository.DebugRow>,
    onRefresh: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    onSync: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onClear) { Text("Очистить") }
                TextButton(onClick = onSync) { Text("Синхронизировать") }
                TextButton(onClick = onRefresh) { Text("Обновить") }
                TextButton(onClick = onClose) { Text("Закрыть") }
            }
        },
        title = { Text("Таблица: profile_cache") },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                // Заголовок “таблицы”
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text("id", modifier = Modifier.width(48.dp), style = MaterialTheme.typography.labelMedium)
                    Text("json (preview)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                    Text("len", modifier = Modifier.width(56.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelMedium)
                    Text("lastSyncMs", modifier = Modifier.width(120.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelMedium)
                }
                Divider()

                val scroll = rememberScrollState()
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(scroll),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (rows.isEmpty()) {
                        Text("Пусто", modifier = Modifier.padding(top = 12.dp))
                    } else {
                        rows.forEach { r ->
                            Row(Modifier.fillMaxWidth()) {
                                Text(r.id.toString(), modifier = Modifier.width(48.dp))
                                Text(r.jsonSample, modifier = Modifier.weight(1f))
                                Text(r.jsonLen.toString(), modifier = Modifier.width(56.dp), textAlign = TextAlign.End)
                                Text(r.lastSyncMs?.toString() ?: "—", modifier = Modifier.width(120.dp), textAlign = TextAlign.End)
                            }
                        }
                    }
                }
            }
        }
    )
}