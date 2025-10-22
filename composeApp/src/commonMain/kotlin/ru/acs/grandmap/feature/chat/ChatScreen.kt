package ru.acs.grandmap.feature.chat

import TopBarController
import TopBarSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect


@Composable
fun ChatScreen(
    component: ChatComponent,
    topBar: TopBarController
) {
    LaunchedEffect(Unit) {
        topBar.update(
            TopBarSpec(
                title = "Чаты",
            )
        )
    }
    DisposableEffect(Unit) { onDispose { topBar.clear() } }
}
