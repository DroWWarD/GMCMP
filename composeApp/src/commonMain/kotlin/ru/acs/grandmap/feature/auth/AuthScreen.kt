package ru.acs.grandmap.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ru.acs.grandmap.navigation.AuthComponent
import ru.acs.grandmap.navigation.UiState
import ru.acs.grandmap.composeResources.*
import ru.acs.grandmap.core.dismissKeyboardOnTap
import ru.acs.grandmap.core.rememberHideKeyboard
import ru.acs.grandmap.ui.AppTopBar
import ru.acs.grandmap.ui.common.MenuItem
import ru.acs.grandmap.ui.common.SupportPhone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    component: AuthComponent,
    dark: Boolean,
    onToggleTheme: () -> Unit,
) {
    val s = component.uiState.value

    // ВАЖНО: убрали .imePadding() отсюда.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .dismissKeyboardOnTap() // тап по фону скрывает клавиатуру (iOS в т.ч.)
    ) {
        val scroll = rememberScrollState()
        val logoAcs = if (dark) Res.drawable.logo_acs_on_dark else Res.drawable.logo_acs
        val logoGM  = if (dark) Res.drawable.logo_grandmapp_on_dark else Res.drawable.logo_grandmapp

        Scaffold(
            topBar = {
                AppTopBar(title = "", onToggleTheme = onToggleTheme, dark = dark)
            },
            contentWindowInsets = WindowInsets(0.dp), // сами управляем инсетами
            bottomBar = { AuthFooter() }             // футер без imePadding — уедет под IME
        ) { paddings ->
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddings)
                    .verticalScroll(scroll)
                    .padding(horizontal = 30.dp)
                    .dismissKeyboardOnTap(),        // можно оставить и тут
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(50.dp))
                Image(
                    painter = painterResource(logoAcs),
                    contentDescription = null,
                    modifier = Modifier.height(110.dp)
                )
                Spacer(Modifier.height(50.dp))
                Image(
                    painter = painterResource(logoGM),
                    contentDescription = null,
                    modifier = Modifier.height(50.dp)
                )
                Spacer(Modifier.height(25.dp))

                // imePadding применяем ТОЛЬКО к секции формы,
                // чтобы она вместе с кнопкой поднималась над клавиатурой.
                when (s.step) {
                    UiState.Step.Phone -> PhoneStep(
                        phone = s.phone,
                        loading = s.loading,
                        error = s.error,
                        onChange = component::onPhoneChange,
                        onContinue = component::sendSms,
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                    )
                    UiState.Step.Code -> CodeStep(
                        code = s.code,
                        loading = s.loading,
                        error = s.error,
                        onChange = component::onCodeChange,
                        onConfirm = component::confirmCode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                    )
                }

                Spacer(Modifier.height(15.dp))
                MenuItem(
                    icon = Icons.Filled.HowToReg,
                    isLoading = s.loading,
                    title = "Присоединиться к команде",
                    onClick = { /* TODO */ },
                )
                Spacer(Modifier.height(15.dp))
                MenuItem(
                    icon = Icons.Filled.Warehouse,
                    isLoading = s.loading,
                    title = "Регистрация поставщика",
                    onClick = { /* TODO */ },
                )
                Spacer(Modifier.height(25.dp))
            }
        }
    }
}

@Composable
private fun PhoneStep(
    phone: String,
    loading: Boolean,
    error: String?,
    onChange: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier
) {
    val hideKeyboard = rememberHideKeyboard()
    val isValid = phone.length == 10 && phone.all { it.isDigit() }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) { // ← применяем модификатор сюда
        OutlinedTextField(
            value = phone,
            onValueChange = { new ->
                // фильтруем ввод: оставляем цифры и ограничиваем 10-ю
                val digits = new.filter { it.isDigit() }.take(10)
                if (digits != phone) onChange(digits)
            },
            shape = RoundedCornerShape(16.dp),
            label = { Text("Введите свой номер (+7...)") },
            leadingIcon = { Icon(imageVector = Icons.Filled.Phone, contentDescription = "PhoneIcon") },
            prefix = { Text("+7") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { hideKeyboard() }
            ),
            isError = phone.isNotEmpty() && !isValid,
            suffix = {
                Text("${phone.length}/10")
            },
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                hideKeyboard()
                onContinue()
            },
            enabled = !loading
        ) {
            Text(if (loading) "Отправка..." else "Продолжить")
        }
    }
}

@Composable
private fun CodeStep(
    code: String,
    loading: Boolean,
    error: String?,
    onChange: (String) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier
) {
    val hideKeyboard = rememberHideKeyboard()
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = code,
            onValueChange = onChange,
            label = { Text("Код из SMS") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    hideKeyboard()
                    onConfirm()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                hideKeyboard()
                onConfirm() // ← вызывали без скобок — не работало
            },
            enabled = !loading
        ) {
            Text(if (loading) "Проверяем..." else "Подтвердить")
        }
    }
}

@Composable
private fun AuthFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D496F))
            .windowInsetsPadding(WindowInsets.navigationBars), // только системные бары, НЕ IME
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Время работы технической поддержки",
                color = Color.White,
                lineHeight = 14.sp,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Пн–Пт 8:00–18:00 по МСК",
                lineHeight = 10.sp,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            SupportPhone()
            Text(
                "© ACS CIS Russia",
                color = Color.White,
                lineHeight = 10.sp,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
