package ru.acs.grandmap.feature.chat

import com.arkivanov.decompose.ComponentContext

interface ChatComponent {

}

class DefaultChatComponent(
    componentContext: ComponentContext
) : ChatComponent, ComponentContext by componentContext {

}
