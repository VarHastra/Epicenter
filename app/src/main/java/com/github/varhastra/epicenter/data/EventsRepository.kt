package com.github.varhastra.epicenter.data

import com.github.varhastra.epicenter.model.Event
import com.github.varhastra.epicenter.model.FeedFilter
import com.github.varhastra.epicenter.model.Place
import com.github.varhastra.epicenter.networking.EventServiceProvider
import com.github.varhastra.epicenter.networking.EventServiceResponse
import com.github.varhastra.epicenter.networking.usgs.UsgsServiceProvider
import org.jetbrains.anko.AnkoLogger

class EventsRepository private constructor(
    private val serviceProvider: EventServiceProvider
) {

    val logger = AnkoLogger(this.javaClass)

    fun getWeekFeed(callback: RepositoryCallback, filter: FeedFilter = FeedFilter(), place: Place = Place.WORLD) {
        serviceProvider.getWeekFeed(object : EventServiceProvider.ResponseCallback {
            override fun onResult(response: EventServiceResponse) {
                val list = response.mapToModel()
                callback.onResult(filter(list, filter, place))
            }

            override fun onFailure(t: Throwable?) {
                onFailure(t)
            }
        })
    }

    fun getDayFeed(callback: RepositoryCallback, filter: FeedFilter = FeedFilter(), place: Place = Place.WORLD) {
        serviceProvider.getDayFeed(object : EventServiceProvider.ResponseCallback {
            override fun onResult(response: EventServiceResponse) {
                val list = response.mapToModel()
                callback.onResult(filter(list, filter, place))
            }

            override fun onFailure(t: Throwable?) {
                onFailure(t)
            }
        })
    }

    private fun filter(events: List<Event>, filter: FeedFilter, place: Place): List<Event> {
        val result = events.filter { place.checkCoordinates(it.coordinates) }
        return filter.applyTo(result)
    }


    interface RepositoryCallback {
        fun onResult(events: List<Event>)

        fun onFailure(t: Throwable?)
    }

    companion object {
        private var instance: EventsRepository? = null

        fun getInstance(
            serviceProvider: EventServiceProvider = UsgsServiceProvider()
        ): EventsRepository {
            return instance ?: EventsRepository(serviceProvider)
        }
    }
}