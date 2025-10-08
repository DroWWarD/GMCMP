package ru.acs.grandmap.navigation

import androidx.compose.runtime.*
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.acs.grandmap.core.auth.TokenManager
import ru.acs.grandmap.core.auth.TokenPair
import ru.acs.grandmap.feature.auth.*
import ru.acs.grandmap.feature.auth.dto.*

private const val AUTH_BASE             = "/api/Auth"
private const val START_PHONE_SMS       = "$AUTH_BASE/start-phone-sms"
private const val CONFIRM_PHONE_SMS     = "$AUTH_BASE/confirm-phone-sms"

interface AuthComponent {
    val uiState: State<UiState>
    fun onPhoneChange(v: String)
    fun onCodeChange(v: String)
    fun sendSms()
    fun confirmCode()
}

data class UiState(
    val step: Step = Step.Phone,
    val phone: String = "",
    val code: String = "",
    val loading: Boolean = false,
    val error: String? = null
) {
    enum class Step { Phone, Code }
}

class DefaultAuthComponent(
    componentContext: ComponentContext,
    private val tokenManager: TokenManager,
    private val onAuthorized: (EmployeeDto) -> Unit,
    private val httpClient: HttpClient,                      // <— ПРОКИНУЛИ
    private val useCookiesDefault: Boolean = defaultUseCookies()
) : AuthComponent, ComponentContext by componentContext {

    private var _csrfToken: String? = null
    private var _otpId: String? = null
    private var _retryAfterSec: Int? = null
    // корректный scope, привязанный к lifecycle компонента
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = mutableStateOf(UiState())
    override val uiState: State<UiState> = _state

    override fun onPhoneChange(v: String) { _state.value = _state.value.copy(phone = v, error = null) }
    override fun onCodeChange(v: String) { _state.value = _state.value.copy(code = v, error = null) }

    override fun sendSms() {
        val phone = _state.value.phone.trim()
        if (phone.isEmpty()) {
            _state.value = _state.value.copy(error = "Введите номер телефона")
            return
        }
        scope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                httpClient.post(START_PHONE_SMS) {
                    contentType(ContentType.Application.Json)
                    setBody(StartPhoneSmsRequestDto(phoneE164 = phone))
                }.body<StartPhoneSmsResponseDto>()
            }.onSuccess { resp ->
                _otpId = resp.otpId
                _retryAfterSec = resp.retryAfterSec
                // csrf обычно не приходит на этом шаге, но если будет — сохраним
                _state.value = _state.value.copy(step = UiState.Step.Code, loading = false)
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message ?: "Ошибка")
            }
        }
    }

    override fun confirmCode() {
        val code = _state.value.code.trim()
        val otpId = _otpId
        if (otpId.isNullOrEmpty()) {
            _state.value = _state.value.copy(error = "Сначала запросите код")
            return
        }
        if (code.length < 4) {
            _state.value = _state.value.copy(error = "Введите 4 цифры кода")
            return
        }
        scope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                httpClient.post(CONFIRM_PHONE_SMS) {
                    contentType(ContentType.Application.Json)
                    _csrfToken?.let { header("X-CSRF", it) } // если сервер ожидает
                    setBody(
                        ConfirmPhoneSmsRequestDto(
                            otpId = otpId,
                            code = code,
                            useCookies = useCookiesDefault,
                            deviceId = deviceId(),
                            deviceTitle = deviceTitle(),
                            platform = platformCode()
                        )
                    )
                }.body<ConfirmPhoneSmsResponseDto>()
            }.onSuccess { resp ->
                val refresh = resp.refreshToken ?: "COOKIE"
                tokenManager.set(
                    TokenPair(
                        accessToken = resp.accessToken,
                        refreshToken = refresh,
                        accessExpiresAt = null
                    )
                )
                _csrfToken = resp.csrfToken
                // передаём employee наверх — покажем профиль
                _state.value = _state.value.copy(loading = false)
                onAuthorized(resp.employee)
                // если хочешь — возвращай тут resp.employee через коллбэк
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message ?: "Неверный код")
            }
        }
    }
}
