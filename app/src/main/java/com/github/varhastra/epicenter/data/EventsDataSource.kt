package com.github.varhastra.epicenter.data

import android.os.SystemClock
import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.common.functionaltypes.ifSuccess
import com.github.varhastra.epicenter.data.network.EventServiceProvider
import com.github.varhastra.epicenter.domain.model.Event
import com.github.varhastra.epicenter.domain.model.failures.Failure
import com.github.varhastra.epicenter.domain.repos.EventsRepository
import org.threeten.bp.Duration
import timber.log.Timber

class EventsDataSource(
        private val serviceProvider: EventServiceProvider
) : EventsRepository {

    private val eventsFeedCache: MutableMap<String, Event> = mutableMapOf()

    private var cacheUpdatedAtMillis: Long = 0

    private val millisSinceCacheUpdate get() = SystemClock.elapsedRealtime() - cacheUpdatedAtMillis

    private val cacheIsStale get() = millisSinceCacheUpdate > CACHE_OBSOLESCENCE_THRESHOLD_MS

    private val cacheIsAvailable get() = eventsFeedCache.isNotEmpty()


    override suspend fun getWeekFeedSuspending(forceLoad: Boolean): Either<List<Event>, Failure> {
        if (!forceLoad && cacheIsAvailable && !cacheIsStale) {
            val list = eventsFeedCache.values.toList()
            return Either.Success(list)
        }

        return serviceProvider.getWeekFeed()
                .map { response -> response.mapToModel() }
                .ifSuccess { updateFeedCache(it) }
    }

    override suspend fun getEventSuspending(eventId: String): Either<Event, Failure> {
        val cachedEvent = eventsFeedCache[eventId]

        return if (cachedEvent != null) {
            Either.Success(cachedEvent)
        } else {
            Timber.d("Can't find event with the given id: $eventId.")
            Either.Failure(Failure.EventsFailure.NoSuchEvent(eventId))
        }
    }

    private fun updateFeedCache(list: List<Event>) {
        cacheUpdatedAtMillis = SystemClock.elapsedRealtime()
        eventsFeedCache.clear()
        eventsFeedCache.putAll(list.associateBy { it.id })
    }


    companion object {

        private val CACHE_OBSOLESCENCE_THRESHOLD_MS = Duration.ofMinutes(10).toMillis()
    }
}