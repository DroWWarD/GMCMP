package ru.acs.grandmap.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import app.cash.sqldelight.async.coroutines.awaitCreate
import org.w3c.dom.Worker

// 1) ЕДИНСТВЕННОЕ выражение в теле функции — js("...").
//    Внутри создаём ESM-воркера и сразу передаём { type: 'module' }.
private fun newSqlDelightWorker(): Worker =
    js("new Worker(new URL('@cashapp/sqldelight-sqljs-worker/sqljs.worker.js', import.meta.url), { type: 'module' })")

// 2) Можно закешировать воркера (по желанию)
private val sqlDelightWorker: Worker by lazy { newSqlDelightWorker() }

// 3) Драйвер
actual suspend fun provideSqlDriver(dbName: String): SqlDriver =
    WebWorkerDriver(sqlDelightWorker)

/** Вызывается из createDatabase(); делает init идемпотентным. */
actual suspend fun ensureDbCreated(driver: SqlDriver) {
    try {
        AppDatabase.Schema.awaitCreate(driver)   // создаёт схему на чистой БД
    } catch (t: Throwable) {
        // Повторный запуск на уже созданной БД даёт ошибки вида "table X already exists".
        val msg = t.message?.lowercase() ?: ""
        if (!msg.contains("already exists")) throw t
        // иначе — игнорируем, схема уже есть
    }
}