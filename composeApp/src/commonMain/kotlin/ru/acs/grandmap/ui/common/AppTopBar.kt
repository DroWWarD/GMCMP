// ru.acs.grandmap.ui.common/AppTopBar.kt
package ru.acs.grandmap.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    loading: Boolean = false,

    onBack: (() -> Unit)? = null,
    onToggleTheme: (() -> Unit)? = null,   // ← оставляем, чтобы уметь добавлять в меню
    dark: Boolean? = null,

    primaryActions: List<AppBarIconAction> = emptyList(),
    overflowItems: List<AppBarOverflowItem> = emptyList(),

    // можно скрыть пункт темы на конкретном экране, если нужно
    showThemeToggleInOverflow: Boolean = true,

    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        titleContentColor = MaterialTheme.colorScheme.onSecondary,
        navigationIconContentColor = MaterialTheme.colorScheme.onSecondary,
        actionIconContentColor = MaterialTheme.colorScheme.onSecondary
    ),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    progressHeight: Dp = 4.dp
) {
    var menuOpen by remember { mutableStateOf(false) }

    // ↓ автоматически дополняем меню пунктом «Тёмная/Светлая тема», если есть обработчик
    val mergedOverflow = remember(overflowItems, onToggleTheme, dark, showThemeToggleInOverflow) {
        if (showThemeToggleInOverflow && onToggleTheme != null && dark != null) {
            val label = if (dark) "Светлая тема" else "Тёмная тема"
            val icon  = if (dark) Icons.Filled.LightMode else Icons.Filled.DarkMode
            overflowItems + AppBarOverflowItem(
                title = label,
                onClick = onToggleTheme,
                leadingIcon = icon
            )
        } else {
            overflowItems
        }
    }

    Box(modifier = modifier) {
        CenterAlignedTopAppBar(
            colors = colors,
            scrollBehavior = scrollBehavior,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (subtitle != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            subtitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f)
                        )
                    }
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            },
            actions = {
                // ВАЖНО: НЕ показываем иконку темы в «видимых» действиях — только в ⋮
                primaryActions.forEach { a ->
                    if (a.visible) {
                        IconButton(onClick = a.onClick, enabled = a.enabled) {
                            Icon(a.icon, a.contentDescription)
                        }
                    }
                }

                if (mergedOverflow.isNotEmpty()) {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Меню")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        mergedOverflow.forEach { item ->
                            DropdownMenuItem(
                                onClick = { menuOpen = false; item.onClick() },
                                text = {
                                    val color = if (item.danger) MaterialTheme.colorScheme.error
                                    else LocalContentColor.current
                                    ProvideTextStyle(MaterialTheme.typography.bodyLarge.copy(color = color)) {
                                        Text(item.title)
                                    }
                                },
                                leadingIcon = item.leadingIcon?.let { ic -> { Icon(ic, null) } },
                                enabled = item.enabled
                            )
                        }
                    }
                }
            }
        )

        // узкая полоса прогресса по нижней кромке бара
        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(progressHeight)
            )
        }
    }
}
