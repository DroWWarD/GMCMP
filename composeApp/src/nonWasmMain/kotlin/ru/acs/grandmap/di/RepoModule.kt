// di/RepoModule.kt
package ru.acs.grandmap.di

import org.koin.dsl.module
import ru.acs.grandmap.data.auth.AuthRepository
import ru.acs.grandmap.data.auth.AuthRepositoryImpl

val repoModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}
