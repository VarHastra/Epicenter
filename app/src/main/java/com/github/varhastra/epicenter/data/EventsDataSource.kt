package com.github.varhastra.epicenter.data

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.common.functionaltypes.ifSuccess
import com.github.varhastra.epicenter.data.network.EventServiceProvider
import com.github.varhastra.epicenter.data.network.EventServiceResponse
import com.github.varhastra.epicenter.data.network.usgs.UsgsServiceProvider
import com.github.varhastra.epicenter.domain.model.Event
import com.github.varhastra.epicenter.domain.repos.EventsRepository
import com.github.varhastra.epicenter.domain.repos.RepositoryCallback
import org.jetbrains.anko.AnkoLogger
import org.threeten.bp.Instant
import java.util.*

class EventsDataSource private constructor(
        private val serviceProvider: EventServiceProvider
) : EventsRepository {

    private val logger = AnkoLogger(this.javaClass)

    /**
     * Contains cached events week feed.
     */
    private val eventsFeedCache: MutableMap<String, Event> = mutableMapOf()

    /**
     * Stores the [Instant] of the last week feed update. By default
     * it is initialized with [Instant.EPOCH]
     */
    private var feedLastUpdated: Instant = Instant.EPOCH


    override fun getWeekFeed(callback: RepositoryCallback<List<Event>>, forceLoad: Boolean) {
        if (!forceLoad && eventsFeedCache.isNotEmpty()) {
            val list = eventsFeedCache.values.toList()
            callback.onResult(list)
            return
        }

        serviceProvider.getWeekFeed(object : EventServiceProvider.ResponseCallback {
            override fun onResult(response: EventServiceResponse) {
                val list = response.mapToModel()
                updateFeedCache(list)
                callback.onResult(list)
            }

            override fun onFailure(t: Throwable?) {
                callback.onFailure(t)
            }
        })
    }

    override suspend fun getWeekFeedSuspending(forceLoad: Boolean): Either<List<Event>, Throwable> {
        if (!forceLoad && eventsFeedCache.isNotEmpty()) {
            val list = eventsFeedCache.values.toList()
            return Either.Success(list)
        }

        return serviceProvider.getWeekFeedSuspending()
                .map { response -> response.mapToModel() }
                .ifSuccess { updateFeedCache(it) }
    }

    override fun getEvent(eventId: String, callback: RepositoryCallback<Event>) {
        val event = eventsFeedCache[eventId]

        if (event != null) {
            callback.onResult(event)
        } else {
            callback.onFailure(IllegalStateException("Events cache doesn't contain event with the given id $eventId."))
        }
    }

    override suspend fun getEventSuspending(eventId: UUID): Either<Event, Throwable> {
        val cachedEvent = eventsFeedCache[eventId.toString()]

        return if (cachedEvent != null) {
            Either.Success(cachedEvent)
        } else {
            Either.Failure(IllegalStateException("Unable to find an event with the given id: $eventId."))
        }
    }

    override fun getWeekFeedLastUpdated(): Instant {
        return feedLastUpdated
    }

    override fun isCacheAvailable(): Boolean {
        return !eventsFeedCache.isEmpty()
    }

    private fun updateFeedCache(list: List<Event>) {
        feedLastUpdated = Instant.now()
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
    }
}