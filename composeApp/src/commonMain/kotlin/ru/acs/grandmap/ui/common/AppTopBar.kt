package ru.acs.grandmap.ui.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,                      // ← опциональная «назад»
    onToggleTheme: (() -> Unit)? = null,               // ← опциональный тумблер темы
    dark: Boolean? = null,                             // ← обязателен только если передан onToggleTheme
    actions: @Composable RowScope.() -> Unit = {},     // ← дополнительные действия справа
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        titleContentColor = MaterialTheme.colorScheme.onSecondary,
        navigationIconContentColor = MaterialTheme.colorScheme.onSecondary,
        actionIconContentColor = MaterialTheme.colorScheme.onSecondary
    ),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = colors,
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            }
        },
        actions = {
            // сначала — глобальный тумблер темы, если он нужен на этом экране
            if (onToggleTheme != null && dark != null) {
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (!dark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = "Сменить тему"
                    )
                }
            }
            // потом — произвольные экшены экрана
            actions()
        }
    )
}
