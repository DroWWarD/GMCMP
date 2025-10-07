package ru.acs.grandmap.feature.authsandbox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import ru.acs.grandmap.core.auth.TokenManager
import ru.acs.grandmap.core.auth.TokenPair
import ru.acs.grandmap.di.rememberHttpClientDI
import ru.acs.grandmap.di.rememberTokenManagerDI
import ru.acs.grandmap.feature.asbDTO.RefreshResponseDto
import ru.acs.grandmap.feature.asbDTO.StartPhoneRequestDto
import ru.acs.grandmap.feature.asbDTO.StartPhoneResponseDto

private const val AUTH_BASE      = "/api/Auth"                 // проверь Route
private const val START_PHONE    = "$AUTH_BASE/start-phone"
private const val PING           = "$AUTH_BASE/ping"
private const val REFRESH_COOKIE = "$AUTH_BASE/refresh"        // эндпоинт, читающий refresh из cookie

@Composable
fun AuthSandbox(modifier: Modifier = Modifier) {
    val client: HttpClient = rememberHttpClientDI()
    val tokenManager: TokenManager = rememberTokenManagerDI()
    val scope = rememberCoroutineScope()

    var phone by remember { mutableStateOf("+79990000000") }
    var deviceTitle by remember { mutableStateOf("Firefox on Win") }
    var useCookies by remember { mutableStateOf(true) } // на WASM: true
    var csrf by remember { mutableStateOf<String?>(null) }

    var output by remember { mutableStateOf("") }
    fun log(line: String) {
        output = buildString { append(line).append('\n').append(output) }
    }

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        Text("Auth Sandbox (start-phone)")

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = phone, onValueChange = { phone = it },
            label = { Text("PhoneE164 (+7XXXXXXXXXX)") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = deviceTitle, onValueChange = { deviceTitle = it },
            label = { Text("Device title") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = useCookies, onCheckedChange = { useCookies = it })
            Spacer(Modifier.width(8.dp))
            Text("Use cookies (HttpOnly refresh)")
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                scope.launch {
                    runCatching {
                        client.post(START_PHONE) {
                            contentType(ContentType.Application.Json)
                            setBody(
                                StartPhoneRequestDto(
                                    phoneE164 = phone,
                                    useCookies = useCookies,
                                    deviceId = null,
                                    deviceTitle = deviceTitle
                                )
                            )
                        }.body<StartPhoneResponseDto>()
                    }.onSuccess { resp ->
                        // сохраняем ACCESS в TokenManager
                        val access = resp.accessToken
                        val refresh = resp.refreshToken ?: "COOKIE" // маркер, если кука
                        tokenManager.set(TokenPair(
                            accessToken = access,
                            refreshToken = refresh,
                            accessExpiresAt = null // можно распарсить resp.accessTokenExpUtc, если хочешь ensureFresh()
                        ))
                        csrf = resp.csrfToken
                        log("StartPhone OK. user=${resp.employee?.displayName ?: "—"} session=${resp.sessionId ?: "—"}")
                        log("Access(short)=${access.take(24)}…; cookies=${if (useCookies) "on" else "off"}")
                        if (useCookies) log("CSRF=${csrf ?: "—"}")
                    }.onFailure { e ->
                        log("StartPhone EX: ${e.message}")
                    }
                }
            }) { Text("StartPhone") }

            Button(onClick = {
                scope.launch {
                    // ручной refresh по cookie (сервер берёт refresh из HttpOnly cookie)
                    runCatching {
                        client.post(REFRESH_COOKIE) {
                            // отправим CSRF, если сервер требует его на state-changing запросах
                            csrf?.let { header("X-CSRF", it) }
                        }.body<RefreshResponseDto>()
                    }.onSuccess { r ->
                        tokenManager.set(TokenPair(
                            accessToken = r.accessToken,
                            refreshToken = "COOKIE",
                            accessExpiresAt = null
                        ))
                        log("Refresh OK. access(short)=${r.accessToken.take(24)}…")
                    }.onFailure { e ->
                        log("Refresh EX: ${e.message}")
                    }
                }
            }) { Text("Refresh (cookie)") }

            Button(onClick = {
                scope.launch {
                    runCatching {
                        client.get(PING).body<String>()
                    }.onSuccess { pong ->
                        log("Ping: $pong")
                    }.onFailure { e ->
                        log("Ping EX: ${e.message}")
                    }
                }
            }) { Text("Ping") }
        }

        Spacer(Modifier.height(16.dp))
        Text("Output:")
        Box(
            Modifier.weight(1f).fillMaxWidth()
                .verticalScroll(rememberScrollState()).padding(8.dp)
        ) {
            Text(output.ifBlank { "—" }, fontFamily = FontFamily.Monospace)
        }
    }
}
