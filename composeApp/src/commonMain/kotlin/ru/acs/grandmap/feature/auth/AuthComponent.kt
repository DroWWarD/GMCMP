package ru.acs.grandmap.feature.auth

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
import ru.acs.grandmap.core.toUserMessage
import ru.acs.grandmap.feature.auth.dto.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val AUTH_BASE         = "/api/Auth"
private const val START_PHONE_SMS   = "$AUTH_BASE/start-phone-sms"
private const val CONFIRM_PHONE_SMS = "$AUTH_BASE/confirm-phone-sms"

interface AuthComponent {
    val uiState: State<UiState>
    fun onPhoneChange(v: String)
    fun onCodeChange(v: String)
    fun sendSms()         // начальная и повторная отправка
    fun confirmCode()
    fun backToPhone()     // вернуться к вводу номера
}

data class UiState(
    val step: Step = Step.Phone,
    val phone: String = "",
    val code: String = "",
    val loading: Boolean = false,
    val error: String? = null,
) {
    enum class Step { Phone, Code }
}

@OptIn(ExperimentalTime::class)
private fun nowMs(): Long = Clock.System.now().toEpochMilliseconds()

class DefaultAuthComponent(
    componentContext: ComponentContext,
    private val tokenManager: TokenManager,
    private val onAuthorized: (EmployeeDto) -> Unit,
    private val httpClient: HttpClient,
    private val useCookiesDefault: Boolean = defaultUseCookies()
) : AuthComponent, ComponentContext by componentContext {

    private var _otpId: String? = null
    private var _retryAfterSec: Int? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = mutableStateOf(UiState())
    override val uiState: State<UiState> = _state

    override fun onPhoneChange(v: String) { _state.value = _state.value.copy(phone = v, error = null) }
    override fun onCodeChange(v: String) { _state.value = _state.value.copy(code = v, error = null) }

    /** Вернуться на шаг ввода номера */
    override fun backToPhone() {
        _otpId = null
        _retryAfterSec = null
        _state.value = _state.value.copy(
            step = UiState.Step.Phone,
            code = "",
            error = null,
            loading = false
        )
    }

    /** Начальная/повторная отправка SMS. Проверяет кулдаун и сохраняет отметку. */
    override fun sendSms() {
        var phone = _state.value.phone.trim()
        if (phone.isEmpty()) {
            _state.value = _state.value.copy(error = "Введите номер телефона")
            return
        }
        val digits = phone.filter(Char::isDigit)
        if (!digits.matches(Regex("""^\d{10}$"""))) {
            _state.value = _state.value.copy(error = "Некорректный номер (нужно 10 цифр)")
            return
        }

        val phoneKey = "7$digits"
        val left = smsRemainingSeconds(phoneKey)
        if (left > 0) {
            _state.value = _state.value.copy(
                // остаёмся на шаге телефона, показываем понятный текст
                step = UiState.Step.Phone,
                error = "СМС отправлено. Повторно через ${formatMmSs(left)}"
            )
            return
        }

        phone = "+7$digits"
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
                val cd = (resp.retryAfterSec ?: 60).toLong()
                // ← кулдаун фиксируем ТОЛЬКО на успехе
                smsMarkSent(phoneKey, cooldownSec = cd)
                _state.value = _state.value.copy(step = UiState.Step.Code, loading = false, error = null)
            }.onFailure { e ->
                // ← остаёмся на Step.Phone и показываем ошибку
                _state.value = _state.value.copy(loading = false, error = e.toUserMessage())
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
                tokenManager.setCsrf(resp.csrfToken)

                // очистим локальное состояние шага
                _otpId = null
                _retryAfterSec = null
                _state.value = _state.value.copy(
                    loading = false,
                    code = "",
                    phone = "",
                    step = UiState.Step.Phone,
                    error = null
                )
                onAuthorized(resp.employee)
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.toUserMessage())
            }
        }
    }
}
