// di/StorageModule.kt
package ru.acs.grandmap.di

import com.russhwolf.settings.Settings
import org.koin.dsl.module
import ru.acs.grandmap.core.auth.TokenStorage
import ru.acs.grandmap.core.auth.TokenStorageImpl

val storageModule = module {
    single { Settings() } // если захотите — сюда можно подложить платформенно-специфичный Settings
    single<TokenStorage> { TokenStorageImpl(get()) }
}
