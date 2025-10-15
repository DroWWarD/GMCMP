package ru.acs.grandmap.feature.auth

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import kotlin.math.max
import kotlin.time.ExperimentalTime

private const val KEY_PREFIX_TIME     = "auth.sms.lastSent."
private const val KEY_PREFIX_COOLDOWN = "auth.sms.cooldown."
private const val COOLDOWN_SEC_DEFAULT = 10L

private fun keyTime(phoneKey: String) = KEY_PREFIX_TIME + phoneKey
private fun keyCooldown(phoneKey: String) = KEY_PREFIX_COOLDOWN + phoneKey
@OptIn(ExperimentalTime::class)
private fun nowSec(): Long = (kotlin.time.Clock.System.now().epochSeconds)

// сохранить факт отправки и (опционально) пришедший с сервера кулдаун
fun smsMarkSent(phoneKey: String, cooldownSec: Long? = null, settings: Settings = Settings()) {
    settings[keyTime(phoneKey)] = nowSec()
    cooldownSec?.let { settings[keyCooldown(phoneKey)] = it }
}

// секунды до следующей отправки
fun smsRemainingSeconds(
    phoneKey: String,
    defaultCooldownSec: Long = COOLDOWN_SEC_DEFAULT,
    settings: Settings = Settings()
): Long {
    val last = settings.getLongOrNull(keyTime(phoneKey)) ?: return 0
    val storedCooldown = settings.getLongOrNull(keyCooldown(phoneKey))
    val cd = storedCooldown ?: defaultCooldownSec
    return max(0, last + cd - nowSec())
}

/** удобный композабл-таймер, сам тикает раз в секунду, пока не дойдёт до нуля */
@Composable
fun rememberSmsRemainingTimer(
    phoneKey: String,
    defaultCooldownSec: Long = COOLDOWN_SEC_DEFAULT
): State<Long> {
    val state = remember(phoneKey) {
        mutableStateOf(smsRemainingSeconds(phoneKey, defaultCooldownSec))
    }
    LaunchedEffect(phoneKey) {
        while (true) {
            state.value = smsRemainingSeconds(phoneKey, defaultCooldownSec)
            kotlinx.coroutines.delay(1000)
        }
    }
    return state
}

/** форматирование 75 -> "01:15" */
fun formatMmSs(sec: Long): String {
    val total = if (sec < 0) 0 else sec
    val m = total / 60
    val s = total % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
