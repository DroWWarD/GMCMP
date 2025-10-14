package ru.acs.grandmap.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideogameAsset
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.flow.collectLatest
import ru.acs.grandmap.feature.auth.AuthScreen
import ru.acs.grandmap.feature.profile.ProfileScreen
import ru.acs.grandmap.feature.work.WorkContent
import ru.acs.grandmap.navigation.RootComponent
import ru.acs.grandmap.navigation.UiEvent
import ru.acs.grandmap.navigation.rememberRootComponent

sealed class Tab(val route: String, val label: String, val icon: ImageVector) {
    data object Me : Tab("me", "Профиль", Icons.Filled.Person)
    data object Work : Tab("work", "Функции", Icons.Filled.CardTravel)
    data object Chat : Tab("chat", "Чат", Icons.AutoMirrored.Filled.Chat)
    data object News : Tab("news", "Новости", Icons.Filled.Newspaper)
    data object Game : Tab("game", "Игры", Icons.Filled.VideogameAsset)
}

private fun titleFor(tab: Tab) = when (tab) {
    Tab.Work -> "Главная"
    Tab.Chat -> "Чат"
    Tab.News -> "Новости"
    Tab.Game -> "Уведомления"
    Tab.Me -> "Профиль"
}

@Composable
private fun Placeholder(text: String) {
    Box(Modifier.fillMaxSize().padding(16.dp)) { Text(text) }
}

@Composable
fun RootScaffold(
    dark: Boolean,
    onToggleTheme: () -> Unit
) {
    val root = rememberRootComponent()
    val stack by root.childStack.subscribeAsState()

    val snackHost = remember { SnackbarHostState() }

    LaunchedEffect(root) {
        root.events.collectLatest { ev ->
            when (ev) {
                is UiEvent.Snack -> snackHost.showSnackbar(ev.text)
            }
        }
    }

    LaunchedEffect(stack.active.configuration) {
        if (stack.active.configuration == RootComponent.Config.Me) {
            root.onProfileShown()
        }
    }

    // Если активен экран авторизации — рисуем его полноэкранно и не показываем бары
    val isAuth = stack.active.instance is RootComponent.Child.Auth
    if (isAuth) {
        Children(stack) { child ->
            when (val inst = child.instance) {
                is RootComponent.Child.Auth -> AuthScreen(inst.component, dark, onToggleTheme)

                else -> {}
            }
        }
        return
    }

    // иначе обычная навигация по вкладкам
    val selected: Tab = when (stack.active.configuration) {
        RootComponent.Config.Work -> Tab.Work
        RootComponent.Config.Chat -> Tab.Chat
        RootComponent.Config.News -> Tab.News
        RootComponent.Config.Game -> Tab.Game
        RootComponent.Config.Me -> Tab.Me
        RootComponent.Config.Auth -> Tab.Work
    }

    val tabs = remember { listOf(Tab.Me, Tab.Work, Tab.Chat, Tab.News, Tab.Game) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth < 600.dp

        if (compact) {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackHost) },
                topBar = {
                    AppTopBar(
                        title = titleFor(selected),
                        onToggleTheme = onToggleTheme,
                        dark = dark
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.height(70.dp)

                    )
                    {
                        tabs.forEach { t ->
                            NavigationBarItem(
                                selected = (t == selected),
                                onClick = {
                                    if (t == selected) root.reselect(t) else root.select(t)
                                },
                                icon = { Icon(t.icon, contentDescription = t.label) },
                                label = { Text(t.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(
                                        alpha = .55f
                                    ),
                                    unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(
                                        alpha = .55f
                                    ),
                                    indicatorColor = MaterialTheme.colorScheme.onPrimary.copy(
                                        alpha = .25f
                                    )
                                ),
                                modifier = Modifier.height(50.dp)
                            )
                        }
                    }
                }
            ) { paddings ->
                Box(Modifier.fillMaxSize().padding(paddings)) {
                    Children(stack) { child ->
                        when (val inst = child.instance) {
                            is RootComponent.Child.Me -> {
                                LaunchedEffect(Unit) { root.onProfileShown() }
                                ProfileScreen(
                                    employee = root.profile.value,
                                    onLogout = { root.logout() })
                            }
                            is RootComponent.Child.Work -> WorkContent(inst.component)
                            is RootComponent.Child.Chat -> Placeholder("Здесь будут чаты")
                            is RootComponent.Child.News -> Placeholder("Здесь будут новости")
                            is RootComponent.Child.Game -> Placeholder("Здесь будут игры")
                            is RootComponent.Child.Auth -> TODO()
                        }
                    }
                }
            }
        } else {
            Row(Modifier.fillMaxSize()) {

                NavigationRail(containerColor = MaterialTheme.colorScheme.secondary) {
                    tabs.forEach { t ->
                        NavigationRailItem(
                            selected = (t == selected),
                            onClick = { root.select(t) },
                            icon = { Icon(t.icon, contentDescription = t.label) },
                            label = { Text(t.label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = .55f),
                                unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = .55f),
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = .25f
                                )
                            )
                        )
                    }
                }
                Column(Modifier.fillMaxSize()) {
                    AppTopBar(
                        title = titleFor(selected),
                        onToggleTheme = onToggleTheme,
                        dark = dark
                    )
                    Divider()
                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackHost) } // тот же state
                    ) { paddings ->
                        Box(Modifier.fillMaxSize()) {
                            Children(stack) { child ->
                                when (val inst = child.instance) {
                                    is RootComponent.Child.Work -> WorkContent(inst.component)
                                    is RootComponent.Child.Chat -> Placeholder("Здесь будут чаты")
                                    is RootComponent.Child.News -> Placeholder("Здесь будут новости")
                                    is RootComponent.Child.Game -> Placeholder("Здесь будут уведомления")
                                    is RootComponent.Child.Me -> {
                                        LaunchedEffect(Unit) { root.onProfileShown() }
                                        ProfileScreen(
                                            employee = root.profile.value,
                                            onLogout = { root.logout() })
                                    }

                                    is RootComponent.Child.Auth -> TODO()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
