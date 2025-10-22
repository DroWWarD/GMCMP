package ru.acs.grandmap.feature.news

import TopBarController
import TopBarSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect


@Composable
fun NewsScreen(
    component: NewsComponent,
    topBar: TopBarController
) {
    LaunchedEffect(Unit) {
        topBar.update(
            TopBarSpec(
                title = "Новости",
            )
        )
    }
    DisposableEffect(Unit) { onDispose { topBar.clear() } }
}
