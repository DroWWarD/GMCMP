package ru.acs.grandmap.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.acs.grandmap.navigation.AuthComponent
import ru.acs.grandmap.navigation.UiState

@Composable
fun AuthScreen(component: AuthComponent) {
    val s = component.uiState.value
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Вход по телефону", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        when (s.step) {
            UiState.Step.Phone -> PhoneStep(
                phone = s.phone,
                loading = s.loading,
                error = s.error,
                onChange = component::onPhoneChange,
                onContinue = component::sendSms
            )
            UiState.Step.Code -> CodeStep(
                code = s.code,
                loading = s.loading,
                error = s.error,
                onChange = component::onCodeChange,
                onConfirm = component::confirmCode
            )
        }
    }
}

@Composable
private fun PhoneStep(
    phone: String,
    loading: Boolean,
    error: String?,
    onChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    OutlinedTextField(
        value = phone,
        onValueChange = onChange,
        label = { Text("Телефон (+7...)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth()
    )
    if (error != null) {
        Spacer(Modifier.height(8.dp)); Text(error, color = MaterialTheme.colorScheme.error)
    }
    Spacer(Modifier.height(16.dp))
    Button(onClick = onContinue, enabled = !loading) {
        Text(if (loading) "Отправка..." else "Продолжить")
    }
}

@Composable
private fun CodeStep(
    code: String,
    loading: Boolean,
    error: String?,
    onChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    OutlinedTextField(
        value = code,
        onValueChange = onChange,
        label = { Text("Код из SMS") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth()
    )
    if (error != null) {
        Spacer(Modifier.height(8.dp)); Text(error, color = MaterialTheme.colorScheme.error)
    }
    Spacer(Modifier.height(16.dp))
    Button(onClick = onConfirm, enabled = !loading) {
        Text(if (loading) "Проверяем..." else "Подтвердить")
    }
}
