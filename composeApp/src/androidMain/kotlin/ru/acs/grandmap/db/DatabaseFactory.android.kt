package ru.acs.grandmap.db

import android.annotation.SuppressLint
import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

@SuppressLint("StaticFieldLeak")
private lateinit var appCtx: Context

/** Вызываем один раз при старте (например, в MainActivity.onCreate). */
fun initDatabase(context: Context) {
    appCtx = context.applicationContext
}

actual suspend fun provideSqlDriver(dbName: String): SqlDriver {
    check(::appCtx.isInitialized) { "Call initDatabase(context) before using the DB" }
    return AndroidSqliteDriver(
        schema  = AppDatabase.Schema.synchronous(),
        context = appCtx,
        name    = dbName
    )
}

actual suspend fun ensureDbCreated(driver: SqlDriver) { /* no-op на Android */ }