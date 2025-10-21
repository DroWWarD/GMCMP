package ru.acs.grandmap.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual suspend fun provideSqlDriver(dbName: String): SqlDriver =
    NativeSqliteDriver(AppDatabase.Schema.synchronous(), dbName)

actual suspend fun ensureDbCreated(driver: SqlDriver) {
}