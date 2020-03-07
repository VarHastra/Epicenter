package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.common.functionaltypes.orNull
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.domain.model.failures.Failure
import com.github.varhastra.epicenter.domain.model.filters.Filter
import com.github.varhastra.epicenter.domain.repos.EventsRepository
import com.github.varhastra.epicenter.domain.repos.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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