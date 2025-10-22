package ru.acs.grandmap.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    loading: Boolean = false,

    onBack: (() -> Unit)? = null,
    onToggleTheme: (() -> Unit)? = null,
    dark: Boolean? = null,

    primaryActions: List<AppBarIconAction> = emptyList(),
    overflowItems: List<AppBarOverflowItem> = emptyList(),

    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        titleContentColor = MaterialTheme.colorScheme.onSecondary,
        navigationIconContentColor = MaterialTheme.colorScheme.onSecondary,
        actionIconContentColor = MaterialTheme.colorScheme.onSecondary
    ),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    var menuOpen by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        modifier = modifier,
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
                if (subtitle != null || loading) {
                    Spacer(Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        if (subtitle != null) {
                            Text(
                                subtitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f)
                            )
                        }
                    }
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
            if (onToggleTheme != null && dark != null) {
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        if (!dark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = "Сменить тему"
                    )
                }
            }
            primaryActions.forEach { a ->
                if (a.visible) {
                    IconButton(onClick = a.onClick, enabled = a.enabled) {
                        Icon(a.icon, a.contentDescription)
                    }
                }
            }
            if (overflowItems.isNotEmpty()) {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Меню")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    overflowItems.forEach { item ->
                        DropdownMenuItem(
                            onClick = {
                                menuOpen = false
                                item.onClick()
                            },
                            text = {
                                val color = if (item.danger)
                                    MaterialTheme.colorScheme.error
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
}
