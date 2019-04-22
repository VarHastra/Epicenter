package com.github.varhastra.epicenter.data

import com.github.varhastra.epicenter.data.networking.EventServiceProvider
import com.github.varhastra.epicenter.data.networking.EventServiceResponse
import com.github.varhastra.epicenter.data.networking.usgs.UsgsServiceProvider
import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.EventsDataSource
import com.github.varhastra.epicenter.domain.model.Event
import com.github.varhastra.epicenter.domain.model.FeedFilter
import com.github.varhastra.epicenter.domain.model.Place
import org.jetbrains.anko.AnkoLogger
import org.threeten.bp.Instant

class EventsRepository private constructor(
    private val serviceProvider: EventServiceProvider
) : EventsDataSource {

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


    override fun getWeekFeed(
        callback: DataSourceCallback<List<Event>>,
        filter: FeedFilter,
        place: Place,
        forceLoad: Boolean
    ) {
        if (!forceLoad && eventsFeedCache.isNotEmpty()) {
            val list = eventsFeedCache.values.toList()
            callback.onResult(filter(list, filter, place))
            return
        }

        serviceProvider.getWeekFeed(object : EventServiceProvider.ResponseCallback {
            override fun onResult(response: EventServiceResponse) {
                val list = response.mapToModel()
                updateFeedCache(list)
                callback.onResult(filter(list, filter, place))
            }

            override fun onFailure(t: Throwable?) {
                callback.onFailure(t)
            }
        })
    }

    override fun getWeekFeedLastUpdated(): Instant {
        return feedLastUpdated
    }

    //    override fun getDayFeed(callback: DataSourceCallback<List<Event>>, filter: FeedFilter, place: Place) {
//        serviceProvider.getDayFeed(object : EventServiceProvider.ResponseCallback {
//            override fun onResult(response: EventServiceResponse) {
//                val list = response.mapToModel()
//                callback.onResult(filter(list, filter, place))
//            }
//
//            override fun onFailure(t: Throwable?) {
//                callback.onFailure(t)
//            }
//        })
//    }

    private fun filter(events: List<Event>, filter: FeedFilter, place: Place): List<Event> {
        val result = events.filter { place.checkCoordinates(it.coordinates) }
        return filter.applyTo(result)
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
            return instance ?: EventsRepository(serviceProvider).apply {
                instance = this
            }
        }
    }
}