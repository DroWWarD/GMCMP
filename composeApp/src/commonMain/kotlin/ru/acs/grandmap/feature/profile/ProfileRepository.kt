package ru.acs.grandmap.feature.profile

import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.acs.grandmap.db.AppDatabase
import ru.acs.grandmap.db.createDatabase
import ru.acs.grandmap.feature.auth.dto.EmployeeDto
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


class ProfileRepository(
    private val api: ProfileApi,
    private val databaseProvider: suspend () -> AppDatabase = { createDatabase() },
    private val json: Json = Json { ignoreUnknownKeys = true; explicitNulls = false }
) {
    data class Cached @OptIn(ExperimentalTime::class) constructor(
        val employee: EmployeeDto,
        val lastSync: Instant? // когда мы последний раз синкались с сервером
    )

    private var db: AppDatabase? = null
    private suspend fun database(): AppDatabase = db ?: databaseProvider().also { db = it }

    /** Читаем кэш из БД (suspend, чтобы быть единым и для Wasm). */
    @OptIn(ExperimentalTime::class)
    suspend fun readCached(): Cached? {
        val row = database().profileQueries.getProfile().awaitAsOneOrNull() ?: return null
        val dto = runCatching { json.decodeFromString<EmployeeDto>(row.json) }.getOrNull() ?: return null
        val ts  = row.lastSyncMs?.let { Instant.fromEpochMilliseconds(it) }
        return Cached(dto, ts)
    }

    /** Тянем из сети и кладём в БД. */
    @OptIn(ExperimentalTime::class)
    suspend fun fetchRemoteAndCache(): Cached {
        val dto = api.getProfile()
        val now = Clock.System.now()
        database().profileQueries.upsert(
            json = json.encodeToString(dto),
            lastSyncMs = now.toEpochMilliseconds()
        )
        db?.profileQueries?.getProfile()?.awaitAsOneOrNull()
        return Cached(dto, now)
    }

    suspend fun clear() {
        database().profileQueries.clear()
    }
    // ---- DEBUG / INSPECTOR ----
    data class DebugRow(
        val id: Long,
        val jsonSample: String,
        val jsonLen: Int,
        val lastSyncMs: Long?
    )

    /** Вернёт содержимое таблицы для отображения в инспекторе. */
    suspend fun debugDump(): List<DebugRow> {
        val db = database()
        val rows = db.profileQueries.dump().awaitAsList()
        return rows.map { row ->
            DebugRow(
                id = row.id.toLong(),
                jsonSample = row.json.take(160),
                jsonLen = row.json.length,
                lastSyncMs = row.lastSyncMs
            )
        }
    }

}
