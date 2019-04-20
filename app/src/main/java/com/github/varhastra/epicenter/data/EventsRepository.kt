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

class EventsRepository private constructor(
    private val serviceProvider: EventServiceProvider
) : EventsDataSource {

    val logger = AnkoLogger(this.javaClass)

    override fun getWeekFeed(callback: DataSourceCallback<List<Event>>, filter: FeedFilter, place: Place) {
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

    override fun getDayFeed(callback: DataSourceCallback<List<Event>>, filter: FeedFilter, place: Place) {
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


    companion object {
        private var instance: EventsRepository? = null

        fun getInstance(
            serviceProvider: EventServiceProvider = UsgsServiceProvider()
        ): EventsRepository {
            return instance ?: EventsRepository(serviceProvider)
        }
    }
}