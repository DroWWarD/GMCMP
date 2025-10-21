package ru.acs.grandmap.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual suspend fun provideSqlDriver(dbName: String): SqlDriver {
    val url = "jdbc:sqlite:$dbName"
    val driver = JdbcSqliteDriver(url)
    return driver
}

actual suspend fun ensureDbCreated(driver: SqlDriver) {
    // создаём БД только если файла нет (упростим: если нужна строгая идемпотентность, проверяйте файл)
    AppDatabase.Schema.synchronous().create(driver)
}