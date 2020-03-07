package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.common.functionaltypes.orNull
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.domain.model.failures.Failure
import com.github.varhastra.epicenter.domain.repos.EventsRepository
import com.github.varhastra.epicenter.domain.repos.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadEventInteractor(
        private val eventsRepository: EventsRepository,
        private val locationRepository: LocationRepository
) {

    suspend operator fun invoke(
            eventId: String
    ): Either<RemoteEvent, Failure> = withContext(Dispatchers.IO) {

        val coordinates = locationRepository.getCoordinates().orNull()
        eventsRepository.getEventSuspending(eventId).map { RemoteEvent.of(it, coordinates) }
    }
}