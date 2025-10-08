// ru.acs.grandmap.feature.profile.ProfileScreen.kt
package ru.acs.grandmap.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.acs.grandmap.feature.auth.dto.EmployeeDto

@Composable
fun ProfileScreen(employee: EmployeeDto?) {
    if (employee == null) {
        Box(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Профиль не загружен")
        }
        return
    }
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(employee.displayName ?: "—", style = MaterialTheme.typography.titleLarge)
        Divider()
        Text("Телефон: ${employee.phoneE164 ?: "—"}")
        Text("Email: ${employee.email ?: "—"}")
        Text("Табельный №: ${employee.employeeNumber ?: "—"}")
        Text("Должность: ${employee.jobTitle ?: "—"}")
    }
}
