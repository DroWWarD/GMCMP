package ru.acs.grandmap.feature.news

import com.arkivanov.decompose.ComponentContext

interface NewsComponent {

}

class DefaultNewsComponent(
    componentContext: ComponentContext
) : NewsComponent, ComponentContext by componentContext {

}
