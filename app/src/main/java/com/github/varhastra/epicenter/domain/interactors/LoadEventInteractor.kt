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

class LoadEventInteractor(
        private val eventsRepository: EventsRepository,
        private val locationRepository: LocationRepository
) : Interactor<LoadEventInteractor.RequestValues, RemoteEvent> {

    suspend operator fun invoke(
            eventId: String
    ): Either<RemoteEvent, Throwable> = withContext(Dispatchers.IO) {

        val coordinates = locationRepository.getCoordinates().orNull()
        eventsRepository.getEventSuspending(eventId).map { RemoteEvent.of(it, coordinates) }
    }

    override fun execute(arg: LoadEventInteractor.RequestValues, callback: InteractorCallback<RemoteEvent>) {

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
                val remoteEvent = RemoteEvent.of(result, position?.coordinates)
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