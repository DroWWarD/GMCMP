package ru.acs.grandmap.core

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val MONTH_SHORT_RU = arrayOf(
    "янв","фев","мар","апр","мая","июн","июл","авг","сен","окт","ноя","дек"
)

private fun Int.dd2() = toString().padStart(2, '0')

/** Гибко парсим ISO-строку: c офсетом/Z, без офсета (+добавим Z), либо как LocalDateTime. */
@OptIn(ExperimentalTime::class)
fun String?.toInstantFlexible(
    assumeTimeZone: TimeZone = TimeZone.UTC
): Instant? = this?.let { s ->
    runCatching { Instant.parse(s) }.getOrElse {
        runCatching { Instant.parse("${s}Z") }.getOrElse {
            runCatching { LocalDateTime.parse(s).toInstant(assumeTimeZone) }.getOrNull()
        }
    }
}

/** Человекочитаемый формат: сегодня/вчера/дд МММ [,yyyy], HH:mm */
@OptIn(ExperimentalTime::class)
fun Instant.formatHuman(
    displayTz: TimeZone = TimeZone.currentSystemDefault(),
    now: Instant = Clock.System.now()
): String {
    val nowDt = now.toLocalDateTime(displayTz)
    val dt    = toLocalDateTime(displayTz)

    val today     = nowDt.date
    val yesterday = today.minus(DatePeriod(days = 1))
    val time      = "${dt.hour.dd2()}:${dt.minute.dd2()}"

    return when (dt.date) {
        today     -> "сегодня $time"
        yesterday -> "вчера $time"
        else -> {
            val m = MONTH_SHORT_RU[dt.monthNumber - 1]
            if (dt.year == nowDt.year) "${dt.dayOfMonth} $m, $time"
            else "${dt.dayOfMonth} $m ${dt.year}, $time"
        }
    }
}
@OptIn(ExperimentalTime::class)
fun Instant?.formatHumanOrDash(
    displayTz: TimeZone = TimeZone.currentSystemDefault(),
    now: Instant = Clock.System.now()
): String = this?.formatHuman(displayTz, now) ?: "—"

/** Комбо: строка -> Instant (гибко) -> человекочитаемо */
@OptIn(ExperimentalTime::class)
fun String?.formatIsoHumanOrDash(
    assumeTimeZone: TimeZone = TimeZone.UTC,                // для строк без офсета
    displayTz: TimeZone = TimeZone.currentSystemDefault(),
    now: Instant = Clock.System.now()
): String = toInstantFlexible(assumeTimeZone).formatHumanOrDash(displayTz, now)