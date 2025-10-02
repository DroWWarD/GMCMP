package ru.acs.grandmap.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideogameAsset

// Модель вкладки
sealed class Tab(val route: String, val label: String, val icon: ImageVector) {
    data object Me    : Tab("me",    "Профиль",      Icons.Filled.Person)
    data object Work  : Tab("work",  "Функции",      Icons.Filled.CardTravel)
    data object Chat : Tab("chat", "Чат",  Icons.AutoMirrored.Filled.Chat)
    data object News  : Tab("news",  "Новости",      Icons.Filled.Newspaper)
    data object Game : Tab("game", "Игры",  Icons.Filled.VideogameAsset)

}

@Composable
fun RootScaffold() {
    // список вкладок
    val tabs = remember { listOf(Tab.Me, Tab.Work, Tab.Chat, Tab.News, Tab.Game) }
    var selected by remember { mutableStateOf<Tab>(Tab.Work) }

    // адаптивность: bottom bar < 600dp, иначе rail
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth < 600.dp

        if (compact) {
            Scaffold(
                topBar = { AppTopBar(title = when (selected) {
                    Tab.Work -> "Главная"
                    Tab.Chat -> "Чат"
                    Tab.News -> "Новости"
                    Tab.Game -> "Уведомления"
                    Tab.Me -> "Профиль"
                }) },
                bottomBar = {
                    NavigationBar {
                        tabs.forEach { t ->
                            NavigationBarItem(
                                selected = (t == selected),
                                onClick = { selected = t },
                                icon = { Icon(t.icon, contentDescription = t.label) },
                                label = { Text(t.label) }
                            )
                        }
                    }
                }
            ) { paddings ->
                Box(Modifier.fillMaxSize().padding(paddings)) {
                    TabContent(selected)
                }
            }
        } else {
            Row(Modifier.fillMaxSize()) {
                NavigationRail {
                    tabs.forEach { t ->
                        NavigationRailItem(
                            selected = (t == selected),
                            onClick = { selected = t },
                            icon = { Icon(t.icon, contentDescription = t.label) },
                            label = { Text(t.label) }
                        )
                    }
                }
                Column(Modifier.fillMaxSize()) {
                    AppTopBar(title = when (selected) {
                        Tab.Work -> "Главная"
                        Tab.Chat -> "Чат"
                        Tab.News -> "Новости"
                        Tab.Game -> "Уведомления"
                        Tab.Me -> "Профиль"
                    })
                    Divider()
                    Box(Modifier.fillMaxSize()) {
                        TabContent(selected)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabContent(tab: Tab) {
    when (tab) {
        Tab.Work  -> PingScreen()                  // используем уже готовый экран
        Tab.News  -> Placeholder("Здесь будут новости")
        Tab.Game -> Placeholder("Здесь будут уведомления")
        Tab.Chat -> Placeholder("Здесь будут чаты")
        Tab.Me    -> Placeholder("Здесь профиль")
    }
}

@Composable
private fun Placeholder(text: String) {
    Box(Modifier.fillMaxSize().padding(16.dp)) { Text(text) }
}
