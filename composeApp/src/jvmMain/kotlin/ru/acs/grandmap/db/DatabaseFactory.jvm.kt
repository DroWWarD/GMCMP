package ru.acs.grandmap.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.db.QueryResult

actual suspend fun provideSqlDriver(dbName: String): SqlDriver {
    val url = "jdbc:sqlite:$dbName"
    val driver = JdbcSqliteDriver(url)
    return driver
}


actual suspend fun ensureDbCreated(driver: SqlDriver) {
    val jdbc = driver as JdbcSqliteDriver
    val schema = AppDatabase.Schema.synchronous()
    val target = AppDatabase.Schema.version

    // 1) user_version текущей БД
    val current: Int = jdbc.executeQuery<Int>(
        identifier = null,
        sql = "PRAGMA user_version",
        mapper = { c ->
            val hasRow = c.next().value        // ← .value, не Boolean
            val v = if (hasRow) c.getLong(0)?.toInt() ?: 0 else 0
            QueryResult.Value(v)               // ← возвращаем QueryResult
        },
        parameters = 0,
        binders = {}                           // ← обязателен по сигнатуре
    ).value

    if (current == 0) {
        // 2) Есть ли пользовательские таблицы?
        val tablesCount: Long = jdbc.executeQuery<Long>(
            identifier = null,
            sql = "SELECT COUNT(*) FROM sqlite_master " +
                    "WHERE type='table' AND name NOT LIKE 'sqlite_%'",
            mapper = { c ->
                val hasRow = c.next().value
                val v = if (hasRow) c.getLong(0) ?: 0L else 0L
                QueryResult.Value(v)
            },
            parameters = 0,
            binders = {}
        ).value

        if (tablesCount == 0L) {
            // Чистая БД → создаём схему
            schema.create(driver)
            // опционально выставить версию:
            // jdbc.execute(null, "PRAGMA user_version = $target", 0) {}
        } else {
            // Файл уже содержал таблицы → ничего не создаём
            // (по желанию: можно проставить user_version вручную)
        }
    } else if (current < target) {
        // 3) Миграции
        schema.migrate(driver, current.toLong(), target)
    }
}