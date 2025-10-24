package ru.acs.grandmap.feature.profile.di

import com.arkivanov.decompose.ComponentContext
import ru.acs.grandmap.feature.profile.settings.sessions.SessionsComponent

typealias SessionsFactory = (ComponentContext) -> SessionsComponent
