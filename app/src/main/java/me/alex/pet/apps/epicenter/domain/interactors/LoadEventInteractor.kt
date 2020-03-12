package me.alex.pet.apps.epicenter.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.common.functionaltypes.Either
import me.alex.pet.apps.epicenter.common.functionaltypes.orNull
import me.alex.pet.apps.epicenter.domain.model.RemoteEvent
import me.alex.pet.apps.epicenter.domain.model.failures.Failure
import me.alex.pet.apps.epicenter.domain.repos.EventsRepository
import me.alex.pet.apps.epicenter.domain.repos.LocationRepository

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