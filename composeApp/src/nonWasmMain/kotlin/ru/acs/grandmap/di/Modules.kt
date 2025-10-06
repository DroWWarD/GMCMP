package ru.acs.grandmap.di

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun initLoggingOnce() {
    runCatching { Napier.base(DebugAntilog()) }
}
