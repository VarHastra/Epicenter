package me.alex.pet.apps.epicenter.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.common.functionaltypes.Either
import me.alex.pet.apps.epicenter.common.functionaltypes.orNull
import me.alex.pet.apps.epicenter.domain.model.RemoteEvent
import me.alex.pet.apps.epicenter.domain.model.failures.Failure
import me.alex.pet.apps.epicenter.domain.model.filters.Filter
import me.alex.pet.apps.epicenter.domain.repos.EventsRepository
import me.alex.pet.apps.epicenter.domain.repos.LocationRepository

class LoadMapEventsInteractor(
        private val eventsRepository: EventsRepository,
        private val locationRepository: LocationRepository
) {

    suspend operator fun invoke(
            forceLoad: Boolean,
            filter: Filter<RemoteEvent>
    ): Either<List<RemoteEvent>, Failure> = withContext(Dispatchers.IO) {

        val coordinates = locationRepository.getCoordinates().orNull()
        eventsRepository.getWeekFeedSuspending(forceLoad).map { events ->
            events.map { RemoteEvent.of(it, coordinates) }
                    .filter { filter(it) }
        }
    }
}