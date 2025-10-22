package ru.acs.grandmap.feature.profile.settings

import com.arkivanov.decompose.ComponentContext

interface SettingsComponent {
    fun back()
    fun openSessions()
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    private val onOpenSessions: () -> Unit
) : SettingsComponent, ComponentContext by componentContext {

    override fun back() = onBack()
    override fun openSessions() = onOpenSessions()
}
