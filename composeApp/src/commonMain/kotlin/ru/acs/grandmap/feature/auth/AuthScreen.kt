package ru.acs.grandmap.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import ru.acs.grandmap.core.LockPortrait
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
    LockPortrait()
    val s = component.uiState.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dismissKeyboardOnTap()
    ) {
        val scroll = rememberScrollState()
        val logoAcs = if (dark) Res.drawable.logo_acs_on_dark else Res.drawable.logo_acs
        val logoGM = if (dark) Res.drawable.logo_grandmapp_on_dark else Res.drawable.logo_grandmapp

        Scaffold(
            topBar = {
                AppTopBar(title = "", onToggleTheme = onToggleTheme, dark = dark)
            },
            contentWindowInsets = WindowInsets(0.dp),
            bottomBar = { AuthFooter() }
        ) { paddings ->
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddings)
                    .verticalScroll(scroll)
                    .padding(horizontal = 30.dp)
                    .dismissKeyboardOnTap(),
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

                when (s.step) {
                    UiState.Step.Phone -> PhoneStep(
                        phone = s.phone,
                        loading = s.loading,
                        error = s.error,
                        onChange = component::onPhoneChange,
                        onContinue = {
                            component.sendSms()
                        },
                        state = s,
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
                        onResend = {
                            component.sendSms()
                        },
                        onEditPhone = { component.backToPhone() },
                        phone = s.phone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding(),
                    )
                }

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
    state: UiState,
    modifier: Modifier
) {
    val hideKeyboard = rememberHideKeyboard()
    val isValid = phone.length == 10 && phone.all { it.isDigit() }
    val phoneKey = "7$phone"
    val remaining by rememberSmsRemainingTimer(phoneKey)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = phone,
            onValueChange = { new ->
                val digits = new.filter(Char::isDigit).take(10)
                if (digits != phone) onChange(digits)
            },
            shape = RoundedCornerShape(16.dp),
            label = { Text("Введите свой номер (+7...)") },
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "PhoneIcon") },
            prefix = { Text("+7") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { hideKeyboard() }),
            isError = phone.isNotEmpty() && !isValid,
            suffix = { Text("${phone.length}/10") },
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
                onContinue() // без smsMarkSent — отправка решается в компоненте
            },
            enabled = !loading && isValid && remaining == 0L
        ) {
            Text(
                when {
                    loading       -> "Отправка..."
                    !isValid      -> "Продолжить"
                    remaining==0L -> "Продолжить"
                    else          -> "Повторно через ${formatMmSs(remaining)}"
                }
            )
        }

        Spacer(Modifier.height(15.dp))
        MenuItem(
            icon = Icons.Filled.HowToReg,
            isLoading = state.loading,
            title = "Присоединиться к команде",
            onClick = { /* TODO */ },
        )
        Spacer(Modifier.height(15.dp))
        MenuItem(
            icon = Icons.Filled.Warehouse,
            isLoading = state.loading,
            title = "Регистрация поставщика",
            onClick = { /* TODO */ },
        )
    }
}

@Composable
private fun CodeStep(
    code: String,
    loading: Boolean,
    error: String?,
    onChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onResend: () -> Unit,
    onEditPhone: () -> Unit,
    phone: String,
    modifier: Modifier
) {
    val hideKeyboard = rememberHideKeyboard()
    val isValid = code.length == 4 && code.all { it.isDigit() }
    val phoneKey = "7$phone"
    val remaining by rememberSmsRemainingTimer(phoneKey)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = code,
            onValueChange = onChange,
            shape = RoundedCornerShape(16.dp),
            label = { Text("Код из SMS +7${phone}") },
            leadingIcon = { Icon(imageVector = Icons.Filled.Password, contentDescription = "CodeIcon") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { hideKeyboard(); onConfirm() }),
            isError = code.isNotEmpty() && !isValid,
            suffix = { Text("${code.length}/4") },
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        // Линия действий: [✎]  [  Подтвердить  ]  [↻/таймер]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            // --- слева: изменить номер (корпоративный outlined) ---
            val editEnabled = !loading
            val editBorder = if (editEnabled)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline

            OutlinedIconButton(
                onClick = { hideKeyboard(); onEditPhone() },
                enabled = editEnabled,
                modifier = Modifier.size(56.dp), // чуть крупнее — приятней попадать
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor   = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.outline
                ),
                border = BorderStroke(1.dp, editBorder)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Изменить номер"
                    // tint не нужен — берётся из colors.contentColor
                )
            }

            Spacer(Modifier.width(12.dp))

            // --- центр: Подтвердить (как было) ---
            Button(
                onClick = { hideKeyboard(); onConfirm() },
                enabled = !loading && isValid,
//                modifier = Modifier.weight(1f)
            ) {
                Text(if (loading) "Проверяем..." else "Подтвердить")
            }

            Spacer(Modifier.width(12.dp))

            // --- справа: повторная отправка (тональный, корпоративный) ---
            val resendEnabled = !loading && remaining == 0L
            val tonalColors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (remaining == 0L) {
                FilledTonalIconButton(
                    onClick = { hideKeyboard(); onResend() },
                    enabled = resendEnabled,
                    colors = tonalColors,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Отправить повторно")
                }
            } else {
                BadgedBox(badge = { Badge { Text(formatMmSs(remaining)) } }) {
                    FilledTonalIconButton(
                        onClick = { /* disabled */ },
                        enabled = false,
                        colors = tonalColors,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D496F))
            .windowInsetsPadding(WindowInsets.navigationBars),
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
