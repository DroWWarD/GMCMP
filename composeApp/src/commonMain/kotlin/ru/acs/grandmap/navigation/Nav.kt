package ru.acs.grandmap.navigation

import TopBarController
import TopBarSpec
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
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.ExperimentalComposeUiApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.flow.collectLatest
import ru.acs.grandmap.core.BackHandlerCompat
import ru.acs.grandmap.feature.auth.AuthScreen
import ru.acs.grandmap.feature.chat.ChatScreen
import ru.acs.grandmap.feature.game.GameScreen
import ru.acs.grandmap.feature.game.snake.SnakeScreen
import ru.acs.grandmap.feature.news.NewsScreen
import ru.acs.grandmap.feature.profile.ProfileScreen
import ru.acs.grandmap.feature.profile.settings.sessions.SessionsScreen
import ru.acs.grandmap.feature.profile.settings.SettingsScreen
import ru.acs.grandmap.feature.work.WorkScreen
import ru.acs.grandmap.ui.common.AppTopBar

sealed class Tab(val route: String, val label: String, val icon: ImageVector) {
    data object Profile : Tab("me", "Профиль", Icons.Filled.Person)
    data object Work : Tab("work", "Функции", Icons.Filled.CardTravel)
    data object Chat : Tab("chat", "Чат", Icons.AutoMirrored.Filled.Chat)
    data object News : Tab("news", "Новости", Icons.Filled.Newspaper)
    data object Game : Tab("game", "Игры", Icons.Filled.VideogameAsset)
}

private fun saveKeyOf(cfg: RootComponent.Config): String = when (cfg) {
    RootComponent.Config.Profile       -> "tab:profile"
    RootComponent.Config.Work     -> "tab:work"
    RootComponent.Config.Chat     -> "tab:chat"
    RootComponent.Config.News     -> "tab:news"
    RootComponent.Config.Game     -> "tab:game"
    RootComponent.Config.GameSnake -> "screen:game-snake"
    RootComponent.Config.Settings -> "screen:settings"
    RootComponent.Config.Sessions -> "screen:sessions"
    RootComponent.Config.Auth     -> "screen:auth"
}
private fun titleFor(tab: Tab) = when (tab) {
    Tab.Work -> "Рабочие функции"
    Tab.Chat -> "Чат"
    Tab.News -> "Новости"
    Tab.Game -> "Игры"
    Tab.Profile -> "Профиль"
}

@Composable
private fun Placeholder(text: String) {
    Box(Modifier.fillMaxSize().padding(16.dp)) { Text(text) }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun RootScaffold(
    dark: Boolean,
    onToggleTheme: () -> Unit
) {
    val root = rememberRootComponent()
    val stack by root.childStack.subscribeAsState()
    val snackHost = remember { SnackbarHostState() }

    BackHandlerCompat(enabled = true) {
        val popped = (root as? DefaultRootComponent)?.backInTab() ?: false
        // На корне вкладки ничего не делаем (не закрываем приложение).
        if (!popped) {
            // Можно показать снекбар "Вы на главном экране" — по желанию.
        }
    }
    LaunchedEffect(root) {
        root.events.collectLatest { ev ->
            when (ev) {
                is UiEvent.Snack -> snackHost.showSnackbar(ev.text)
            }
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
        RootComponent.Config.GameSnake -> Tab.Game
        RootComponent.Config.Profile -> Tab.Profile
        RootComponent.Config.Auth -> Tab.Work
        RootComponent.Config.Sessions -> Tab.Profile
        RootComponent.Config.Settings -> Tab.Profile
    }

    val isRoot = when (stack.active.configuration) {
        RootComponent.Config.Profile,
        RootComponent.Config.Work,
        RootComponent.Config.Chat,
        RootComponent.Config.News,
        RootComponent.Config.Game -> true
        else -> false
    }

    val tabs = remember { listOf(Tab.Profile, Tab.Work, Tab.Chat, Tab.News, Tab.Game) }
    val tabStateHolder = rememberSaveableStateHolder()

    val topBarController = remember { TopBarController() }
    val spec by topBarController.spec.collectAsState()

    LaunchedEffect(selected, isRoot) {
        if (isRoot) {
            topBarController.update(
                TopBarSpec(
                    title = titleFor(selected),
                    subtitle = "",
                    loading = false,
                    onBack = null,
                    primary = emptyList(),
                    overflow = emptyList(),
                    visible = true
                )
            )
        } else {
            topBarController.update(TopBarSpec(visible = false))
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth < 600.dp

        if (compact) {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackHost) },
                topBar = {
                    if (spec.visible) {
                        AppTopBar(
                            title = spec.title ?: titleFor(selected),
                            subtitle = spec.subtitle,
                            loading = spec.loading,
                            onBack = spec.onBack,
                            onToggleTheme = onToggleTheme,
                            dark = dark,
                            primaryActions = spec.primary,
                            overflowItems = spec.overflow,
                            overflowOpen = spec.overflowOpen,
                            onOverflowOpenChange = { open -> topBarController.setOverflowOpen(open) }
                        )
                    }
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
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
                        val saveKey = saveKeyOf(child.configuration)
                        tabStateHolder.SaveableStateProvider(saveKey) {
                            when (val inst = child.instance) {
                                is RootComponent.Child.Profile -> ProfileScreen(component = inst.component, topBarController)
                                is RootComponent.Child.Work -> WorkScreen(inst.component, topBarController)
                                is RootComponent.Child.Chat -> ChatScreen(inst.component, topBarController)
                                is RootComponent.Child.News -> NewsScreen(inst.component, topBarController)
                                is RootComponent.Child.Game -> GameScreen(inst.component, topBarController)
                                is RootComponent.Child.GameSnake -> SnakeScreen(inst.component, topBarController)
                                is RootComponent.Child.Auth -> {}
                                is RootComponent.Child.Settings -> SettingsScreen(inst.component, topBarController)
                                is RootComponent.Child.Sessions -> SessionsScreen(inst.component, topBarController)
                            }
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
                    if (spec.visible) {
                        AppTopBar(
                            title = spec.title ?: titleFor(selected),
                            subtitle = spec.subtitle,
                            loading = spec.loading,
                            onBack = spec.onBack,
                            onToggleTheme = onToggleTheme,
                            dark = dark,
                            primaryActions = spec.primary,
                            overflowItems = spec.overflow,
                            overflowOpen = spec.overflowOpen,
                            onOverflowOpenChange = { open -> topBarController.setOverflowOpen(open) },
                            windowInsets = WindowInsets(0),
                            pinButtonsToBottom = true
                        )
                        Divider()
                    }
                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackHost) }
                    ) { paddings ->
                        Box(Modifier.fillMaxSize()) {
                            Children(stack) { child ->
                                val saveKey = saveKeyOf(child.configuration)
                                tabStateHolder.SaveableStateProvider(saveKey) {
                                    when (val inst = child.instance) {
                                        is RootComponent.Child.Work -> WorkScreen(inst.component, topBarController)
                                        is RootComponent.Child.Chat -> ChatScreen(inst.component, topBarController)
                                        is RootComponent.Child.News ->  NewsScreen(inst.component, topBarController)
                                        is RootComponent.Child.Game -> GameScreen(inst.component, topBarController)
                                        is RootComponent.Child.GameSnake -> SnakeScreen( inst.component, topBarController)
                                        is RootComponent.Child.Profile   -> ProfileScreen(inst.component, topBarController)
                                        is RootComponent.Child.Auth -> {}
                                        is RootComponent.Child.Settings -> SettingsScreen(inst.component, topBarController)
                                        is RootComponent.Child.Sessions -> SessionsScreen(inst.component, topBarController)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
