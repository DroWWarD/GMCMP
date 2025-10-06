package ru.acs.grandmap.feature.authsandbox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.acs.grandmap.core.auth.TokenManager
import ru.acs.grandmap.core.auth.TokenPair
import ru.acs.grandmap.di.rememberHttpClientDI
import ru.acs.grandmap.di.rememberTokenManagerDI

private const val AUTH_BASE = "/api/Auth"     // проверь свой Route/Controller
private const val LOGIN_PATH = "$AUTH_BASE/login"
private const val PING_PATH  = "$AUTH_BASE/ping"

@Serializable
private data class AuthRequestDto(
    @SerialName("PhoneNumber") val phone: String,
    @SerialName("Password")    val password: String
)

@Serializable
private data class AuthResponseDto(
    @SerialName("success")        val success: Boolean,
    @SerialName("errorMessage")   val errorMessage: String? = null,
    @SerialName("accessToken")    val accessToken: String? = null,
    @SerialName("refreshToken")   val refreshToken: String? = null,
    @SerialName("personnelNumber")val personnelNumber: String? = null,
    @SerialName("canteens")       val canteens: String? = null,
    @SerialName("fio")            val fio: String? = null
    // при необходимости добавьте остальные поля
)

@Composable
fun AuthSandbox(
    modifier: Modifier = Modifier
) {
    val client: HttpClient = rememberHttpClientDI()
    val tokenManager: TokenManager = rememberTokenManagerDI()
    val scope = rememberCoroutineScope()

    var phone by remember { mutableStateOf("+79990000000") }
    var password by remember { mutableStateOf("pass") }
    var output by remember { mutableStateOf("") }

    fun log(line: String) {
        output = buildString {
            append(line)
            append('\n')
            append(output)
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text("Auth Sandbox")

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone (+7/7/8...)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        runCatching {
                            val resp: AuthResponseDto = client.post(LOGIN_PATH) {
                                contentType(ContentType.Application.Json)
                                setBody(AuthRequestDto(phone = phone, password = password))
                            }.body()
                            resp
                        }.onSuccess { resp ->
                            if (resp.success && resp.accessToken != null && resp.refreshToken != null) {
                                // сохраняем в TokenManager; expiresAt нет — оставляем null
                                tokenManager.set(
                                    TokenPair(
                                        accessToken = resp.accessToken,
                                        refreshToken = resp.refreshToken,
                                        accessExpiresAt = null
                                    )
                                )
                                log("Login OK. FIO=${resp.fio ?: "—"} PN=${resp.personnelNumber ?: "—"}")
                                log("Access(short)=${resp.accessToken.take(20)}…")
                            } else {
                                log("Login FAIL: ${resp.errorMessage ?: "unknown error"}")
                            }
                        }.onFailure { e ->
                            log("Login EXCEPTION: ${e.message}")
                        }
                    }
                }
            ) { Text("Login") }

            Button(
                onClick = {
                    scope.launch {
                        // ручной рефреш через TokenManager (внутри вызовет AuthApi.refresh)
                        val ok = runCatching { tokenManager.refreshAfterUnauthorized() }.getOrElse { false }
                        log("Refresh: $ok")
                    }
                }
            ) { Text("Refresh token") }

            Button(
                onClick = {
                    scope.launch {
                        runCatching {
                            // произвольный GET; если эндпоинт защищён — Ktor подставит Authorization автоматически
                            client.get(PING_PATH).body<String>()
                        }.onSuccess { pong ->
                            log("Ping: $pong")
                        }.onFailure { e ->
                            log("Ping EXCEPTION: ${e.message}")
                        }
                    }
                }
            ) { Text("Ping") }
        }

        Spacer(Modifier.height(16.dp))

        Text("Output:")

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            Text(
                text = output.ifBlank { "—" },
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
