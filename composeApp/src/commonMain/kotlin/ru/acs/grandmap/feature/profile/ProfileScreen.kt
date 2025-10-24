package ru.acs.grandmap.feature.profile

import TopBarController
import TopBarSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.acs.grandmap.feature.profile.settings.sessions.SessionsContent
import ru.acs.grandmap.ui.common.AppBarIconAction
import ru.acs.grandmap.ui.common.AppBarOverflowItem
import ru.acs.grandmap.ui.common.dialogs.AppConfirmDialog
import ru.acs.grandmap.ui.common.InfoMetricsCard
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import ru.acs.grandmap.ui.common.MenuItem
import ru.acs.grandmap.ui.common.MetricSpec
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    component: ProfileComponent,
    topBar: TopBarController
) {
    val s by component.uiState
    var showLogout by remember { mutableStateOf(false) }
    var showSessions by remember { mutableStateOf(false) }
    val sessions = remember(component) { component.sessionsComponent() }

    LaunchedEffect( s.loading, s.error, s.lastSync) {
        topBar.update(
            TopBarSpec(
                title = "Профиль",
                subtitle = when {
                    s.loading -> "Обновляем профиль…"
                    s.error != null -> "Ошибка: ${s.error}"
                    s.lastSync != null -> "Обновлено ${formatAgoNonComposable(s.lastSync!!)}"
                    else -> "Нет данных"
                },
                loading = s.loading,
                primary = listOf(AppBarIconAction(Icons.Default.Refresh, "Обновить", component::refresh, enabled = !s.loading)),
                overflow = listOf(
                    AppBarOverflowItem("Подсказка", onClick = {/*TODO*/}, leadingIcon = Icons.AutoMirrored.Filled.HelpOutline),
                    AppBarOverflowItem("Обратная связь", onClick = {/*TODO*/}, leadingIcon = Icons.Default.Call),
//                    AppBarOverflowItem("Инспектор БД", onClick = component::showDbInspector, leadingIcon = Icons.Filled.TableChart),
//                    AppBarOverflowItem("Настройки", onClick = component::openSettings, leadingIcon = Icons.Filled.Settings),
                    AppBarOverflowItem("Активные сеансы", onClick = { showSessions = true }, leadingIcon = Icons.Filled.Devices),
                    AppBarOverflowItem("Выйти", onClick = { showLogout = true }, leadingIcon = Icons.AutoMirrored.Filled.Logout, danger = true),
                ),
                visible = true
            )
        )
    }
    DisposableEffect(Unit) { onDispose { topBar.clear() } }

    Box(Modifier.fillMaxSize()) {
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
            InfoMetricsCard(
                title = s.employee?.displayName ?: "",
                subtitles = listOf("ООО \"АКС\" • IT-департамент",s.employee?.jobTitle?:"", "т/н:${s.employee?.employeeNumber} • В команде с ${s.employee?.hireDate?.substring(0, 4)}"),
                onAvatarClick = {/*TODO*/},
                avatarEnabled = true,
                metrics = listOf(
                    MetricSpec(Icons.Default.EmojiEvents, "--", "награды", onClick = {/*TODO*/}),
                    MetricSpec(Icons.Default.Star,        "--", "отзывы", onClick = {/*TODO*/}),
                    MetricSpec(Icons.Default.PhotoCamera, "--",  "фото", onClick = {/*TODO*/})
                ),
                // можно менять стили как в MenuItem:
                elevation = CardDefaults.cardElevation(2.dp),

            )

            MenuItem(Icons.Filled.Notifications, "Уведомления", onClick = { /* TODO */ }, shape = shape)
            MenuItem(Icons.Filled.CalendarMonth,"Календарь",onClick = { /* TODO */ }, shape = shape)
            MenuItem(Icons.Filled.Badge, "HR и док-ты", onClick = { /* TODO */ }, shape = shape)
            MenuItem(Icons.Filled.School, "Обучение", onClick = { /* TODO */ }, shape = shape)
            MenuItem(Icons.Default.TravelExplore, "WIKI",     onClick = { /* TODO */ }, shape = shape)

            if (s.dbVisible) {
                DbInspectorDialog(
                    rows = s.dbRows,
                    onRefresh = component::refreshDbInspector,
                    onClear = component::clearDbInspector,
                    onClose = component::hideDbInspector,
                    onSync = component::syncAndRefreshDbInspector
                )
            }
            Spacer(Modifier.height(20.dp))
            //------------ Версия ---------------------------------------
            Text(
                text = "Версия 0.0.1 Pre-Alpha",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
        }

        // ======= МОДАЛКИ =======

        if (showSessions) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
//                containerColor = MaterialTheme.colorScheme.secondary,
                onDismissRequest = { showSessions = false },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
            ) {
                SessionsContent(component = sessions, modifier = Modifier.padding(16.dp))
            }
        }

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

@OptIn(ExperimentalTime::class)
fun formatAgoNonComposable(instant: Instant): String {
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
                        .fillMaxHeight(0.5f)
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