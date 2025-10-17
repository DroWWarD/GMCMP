package ru.acs.grandmap.feature.profile

import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.acs.grandmap.feature.auth.dto.EmployeeDto
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Лёгкий кэш профиля без БД (Multiplatform Settings).
 * При желании потом можно заменить на SQLDelight — интерфейс сохраню простой.
 */
class ProfileRepository(
    private val api: ProfileApi,
    private val settings: Settings = Settings(),
    private val json: Json = Json { ignoreUnknownKeys = true; explicitNulls = false }
) {

    private object Keys {
        const val PROFILE_JSON = "profile.json"
        const val PROFILE_SYNC_TS = "profile.synced.epochMs"
    }

    data class Cached @OptIn(ExperimentalTime::class) constructor(
        val employee: EmployeeDto,
        val lastSync: Instant? // когда мы последний раз синкались с сервером
    )

    @OptIn(ExperimentalTime::class)
    fun readCached(): Cached? {
        val raw = settings.getStringOrNull(Keys.PROFILE_JSON) ?: return null
        val ts  = settings.getLongOrNull(Keys.PROFILE_SYNC_TS)
        return runCatching {
            val dto = json.decodeFromString<EmployeeDto>(raw)
            Cached(dto, ts?.let { Instant.fromEpochMilliseconds(it) })
        }.getOrNull()
    }

    @OptIn(ExperimentalTime::class)
    private fun saveCache(employee: EmployeeDto, syncedAt: Instant = Clock.System.now()) {
        settings.putString(Keys.PROFILE_JSON, json.encodeToString(employee))
        settings.putLong(Keys.PROFILE_SYNC_TS, syncedAt.toEpochMilliseconds())
    }

    @OptIn(ExperimentalTime::class)
    suspend fun fetchRemoteAndCache(): Cached {
        val dto = api.getProfile()
        val now = Clock.System.now()
        saveCache(dto, now)
        return Cached(dto, now)
    }

    /**
     * Быстрая загрузка:
     * - если forceRefresh: сразу идём в сеть и кэшируем.
     * - иначе: отдаём кэш (если есть) и уже UI сам может вызвать refresh().
     */
    suspend fun load(forceRefresh: Boolean = false): Cached? =
        if (forceRefresh) fetchRemoteAndCache() else readCached()
}
