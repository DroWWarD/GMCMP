package ru.acs.grandmap.feature.work

import com.arkivanov.decompose.ComponentContext

interface WorkComponent {

}

class DefaultWorkComponent(
    componentContext: ComponentContext
) : WorkComponent, ComponentContext by componentContext {

}
