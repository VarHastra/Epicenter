package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.common.functionaltypes.orNull
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Event
import com.github.varhastra.epicenter.domain.model.Position
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.domain.model.filters.Filter
import com.github.varhastra.epicenter.domain.repos.EventsRepository
import com.github.varhastra.epicenter.domain.repos.LocationRepository
import com.github.varhastra.epicenter.domain.repos.RepositoryCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadMapEventsInteractor(
        private val eventsRepository: EventsRepository,
        private val locationRepository: LocationRepository) : FunctionalInteractor<LoadMapEventsInteractor.RequestValues, List<RemoteEvent>> {

    override var onResult: ((List<RemoteEvent>) -> Unit)? = null
    override var onFailure: ((Throwable?) -> Unit)? = null


    suspend operator fun invoke(
            forceLoad: Boolean,
            filter: Filter<Event>,
            sortingStrategy: Comparator<RemoteEvent>
    ): Either<List<RemoteEvent>, Throwable> = withContext(Dispatchers.IO) {

        val coordinates = locationRepository.getCoordinates().orNull()
        eventsRepository.getWeekFeedSuspending(forceLoad).map { events ->
            events.filter { filter(it) }
                    .map { RemoteEvent.from(it, coordinates) }
                    .sortedWith(sortingStrategy)
        }
    }

    override fun execute(arg: RequestValues) {
        loadEvents(arg)
    }

    private fun loadEvents(requestValues: RequestValues) {
        // Try to get current user location first
        locationRepository.getLastLocation(object : RepositoryCallback<Position> {
            override fun onResult(result: Position) {
                // Location is available. Use it to calculate the distance to each event.
                loadEvents(requestValues, result.coordinates)
            }

            override fun onFailure(t: Throwable?) {
                // If current location is not available then just pass null
                // RemoteEvent will take care of it
                loadEvents(requestValues, null)
            }
        })
    }

    private fun loadEvents(
            requestValues: RequestValues,
            coordinates: Coordinates?
    ) {
        // Get events
        eventsRepository.getWeekFeed(object : RepositoryCallback<List<Event>> {
            override fun onResult(result: List<Event>) {
                val events = result.map { RemoteEvent.from(it, coordinates) }
                        .filter { requestValues.filter(it) }

                onResult?.invoke(events)
            }

            override fun onFailure(t: Throwable?) {
                onFailure?.invoke(t)
            }
        }, requestValues.forceLoad)
    }

    class RequestValues(
            val forceLoad: Boolean,
            val filter: Filter<RemoteEvent>
    )
}