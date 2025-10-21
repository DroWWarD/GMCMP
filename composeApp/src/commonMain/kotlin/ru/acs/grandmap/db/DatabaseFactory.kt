package ru.acs.grandmap.db

import app.cash.sqldelight.db.SqlDriver

expect suspend fun provideSqlDriver(dbName: String = "grandmapp.db"): SqlDriver

expect suspend fun ensureDbCreated(driver: SqlDriver)

suspend fun createDatabase(dbName: String = "grandmapp.db"): AppDatabase {
    val driver = provideSqlDriver(dbName)
    ensureDbCreated(driver)           // идемпотентно
    return AppDatabase(driver)
}