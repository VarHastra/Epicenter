package com.github.varhastra.epicenter.data

import com.github.varhastra.epicenter.model.Event
import com.github.varhastra.epicenter.model.FeedFilter
import com.github.varhastra.epicenter.model.Place
import com.github.varhastra.epicenter.networking.EventServiceProvider
import com.github.varhastra.epicenter.networking.EventServiceResponse
import com.github.varhastra.epicenter.networking.usgs.UsgsServiceProvider
import org.jetbrains.anko.AnkoLogger

class Repository private constructor(
        private val serviceProvider: EventServiceProvider
) {

    val logger = AnkoLogger(this.javaClass)

    fun getWeekFeed(callback: RepositoryCallback, filter: FeedFilter = FeedFilter(), place: Place = Place.DEFAULT) {
        serviceProvider.getWeekFeed(object : EventServiceProvider.ResponseCallback {
            override fun onResult(response: EventServiceResponse) {
                val list = response.mapToModel()
                callback.onResult(filter.applyTo(list, place.coordinates))
            }

            override fun onFailure(t: Throwable?) {
                onFailure(t)
            }
        })
    }

    fun getDayFeed(callback: RepositoryCallback, filter: FeedFilter = FeedFilter(), place: Place = Place.DEFAULT) {
        serviceProvider.getDayFeed(object : EventServiceProvider.ResponseCallback {
            override fun onResult(response: EventServiceResponse) {
                val list = response.mapToModel()
                callback.onResult(filter.applyTo(list, place.coordinates))
            }

            override fun onFailure(t: Throwable?) {
                onFailure(t)
            }
        })
    }


    interface RepositoryCallback {
        fun onResult(events: List<Event>)

        fun onFailure(t: Throwable?)
    }

    companion object {
        private var instance: Repository? = null

        fun getInstance(
                serviceProvider: EventServiceProvider = UsgsServiceProvider()
        ): Repository {
            return instance ?: Repository(serviceProvider)
        }
    }
}