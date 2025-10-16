package ru.acs.grandmap.feature.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import ru.acs.grandmap.ui.common.AppTopBar
import ru.acs.grandmap.ui.common.MenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    component: SettingsComponent,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Настройки",
                onBack = component::back,
            )
        }
    ) { paddings ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddings)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MenuItem(
                icon = Icons.Filled.Devices,
                title = "Сеансы на устройствах",
                onClick = component::openSessions
            )
        }
    }
}
