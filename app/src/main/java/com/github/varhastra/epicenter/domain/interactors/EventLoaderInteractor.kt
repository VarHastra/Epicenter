package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.common.functionaltypes.orNull
import com.github.varhastra.epicenter.domain.model.Event
import com.github.varhastra.epicenter.domain.model.Position
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.domain.repos.EventsRepository
import com.github.varhastra.epicenter.domain.repos.LocationRepository
import com.github.varhastra.epicenter.domain.repos.RepositoryCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventLoaderInteractor(
        private val eventsRepository: EventsRepository,
        private val locationRepository: LocationRepository
) : Interactor<EventLoaderInteractor.RequestValues, RemoteEvent> {

    suspend operator fun invoke(
            eventId: String
    ): Either<RemoteEvent, Throwable> = withContext(Dispatchers.IO) {

        val coordinates = locationRepository.getCoordinates().orNull()
        eventsRepository.getEventSuspending(eventId).map { RemoteEvent.from(it, coordinates) }
    }

    override fun execute(arg: EventLoaderInteractor.RequestValues, callback: InteractorCallback<RemoteEvent>) {

        locationRepository.getLastLocation(object : RepositoryCallback<Position> {
            override fun onResult(result: Position) {
                loadEvent(result, arg, callback)
            }

            override fun onFailure(t: Throwable?) {
                loadEvent(null, arg, callback)
            }
        })

    }

    private fun loadEvent(position: Position?, arg: RequestValues, callback: InteractorCallback<RemoteEvent>) {
        eventsRepository.getEvent(arg.eventId, object : RepositoryCallback<Event> {
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