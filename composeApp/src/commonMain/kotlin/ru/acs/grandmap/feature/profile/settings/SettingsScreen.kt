package ru.acs.grandmap.feature.profile.settings

import TopBarController
import TopBarSpec
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import ru.acs.grandmap.ui.common.MenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    component: SettingsComponent,
    topBar: TopBarController
) {

    LaunchedEffect(Unit) {
        topBar.update(
            TopBarSpec(
                title = "Настройки",
                onBack = component::back,
                visible = true
            )
        )
    }
    DisposableEffect(Unit) { onDispose { topBar.clear() } }
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        MenuItem(
            icon = Icons.Filled.Devices,
            title = "Сеансы на устройствах",
            onClick = component::openSessions
        )
    }
}
