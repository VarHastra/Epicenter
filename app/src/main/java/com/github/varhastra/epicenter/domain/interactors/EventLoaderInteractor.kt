package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.EventsDataSource
import com.github.varhastra.epicenter.domain.LocationRepository
import com.github.varhastra.epicenter.domain.model.Event
import com.github.varhastra.epicenter.domain.model.Position
import com.github.varhastra.epicenter.domain.model.RemoteEvent

class EventLoaderInteractor(
        private val eventsDataSource: EventsDataSource,
        private val locationRepository: LocationRepository
) : Interactor<EventLoaderInteractor.RequestValues, RemoteEvent> {

    override fun execute(arg: EventLoaderInteractor.RequestValues, callback: InteractorCallback<RemoteEvent>) {

        locationRepository.getLastLocation(object : DataSourceCallback<Position> {
            override fun onResult(result: Position) {
                loadEvent(result, arg, callback)
            }

            override fun onFailure(t: Throwable?) {
                loadEvent(null, arg, callback)
            }
        })

    }

    private fun loadEvent(position: Position?, arg: RequestValues, callback: InteractorCallback<RemoteEvent>) {
        eventsDataSource.getEvent(arg.eventId, object : DataSourceCallback<Event> {
            override fun onResult(result: Event) {
                val remoteEvent = RemoteEvent.from(result, position?.coordinates)
                callback.onResult(remoteEvent)
            }

            override fun onFailure(t: Throwable?) {
                callback.onFailure(t)
            }
        })
    }

    class RequestValues(
            val eventId: String
    )
}