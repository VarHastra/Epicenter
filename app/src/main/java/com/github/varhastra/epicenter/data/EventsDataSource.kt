package com.github.varhastra.epicenter.data

import android.os.SystemClock
import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.common.functionaltypes.ifSuccess
import com.github.varhastra.epicenter.data.network.EventServiceProvider
import com.github.varhastra.epicenter.data.network.usgs.UsgsServiceProvider
import com.github.varhastra.epicenter.domain.model.Event
import com.github.varhastra.epicenter.domain.repos.EventsRepository
import org.threeten.bp.Duration

class EventsDataSource private constructor(
        private val serviceProvider: EventServiceProvider
) : EventsRepository {

    private val eventsFeedCache: MutableMap<String, Event> = mutableMapOf()

    private var cacheUpdatedAtMillis: Long = 0

    private val millisSinceCacheUpdate get() = SystemClock.elapsedRealtime() - cacheUpdatedAtMillis

    private val cacheIsStale get() = millisSinceCacheUpdate > CACHE_OBSOLESCENCE_THRESHOLD_MS

    private val cacheIsAvailable get() = eventsFeedCache.isNotEmpty()


    override suspend fun getWeekFeedSuspending(forceLoad: Boolean): Either<List<Event>, Throwable> {
        if (!forceLoad && cacheIsAvailable && !cacheIsStale) {
            val list = eventsFeedCache.values.toList()
            return Either.Success(list)
        }

        return serviceProvider.getWeekFeed()
                .map { response -> response.mapToModel() }
                .ifSuccess { updateFeedCache(it) }
    }

    override suspend fun getEventSuspending(eventId: String): Either<Event, Throwable> {
        val cachedEvent = eventsFeedCache[eventId]

        return if (cachedEvent != null) {
            Either.Success(cachedEvent)
        } else {
            Either.Failure(IllegalStateException("Unable to find an event with the given id: $eventId."))
        }
    }

    private fun updateFeedCache(list: List<Event>) {
        cacheUpdatedAtMillis = SystemClock.elapsedRealtime()
        eventsFeedCache.clear()
        eventsFeedCache.putAll(list.associateBy { it.id })
    }


    companion object {
        private var instance: EventsRepository? = null

        fun getInstance(
                serviceProvider: EventServiceProvider = UsgsServiceProvider()
        ): EventsRepository {
            return instance ?: EventsDataSource(serviceProvider).apply {
                instance = this
            }
        }

        private val CACHE_OBSOLESCENCE_THRESHOLD_MS = Duration.ofMinutes(10).toMillis()
    }
}