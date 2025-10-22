package ru.acs.grandmap.feature.work

import TopBarController
import TopBarSpec
import androidx.compose.runtime.*

@Composable
fun WorkScreen(
    component: WorkComponent,
    topBar: TopBarController
) {
    LaunchedEffect(Unit) {
        topBar.update(
            TopBarSpec(
                title = "Рабочие функции",
            )
        )
    }
    DisposableEffect(Unit) { onDispose { topBar.clear() } }
}
