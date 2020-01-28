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

class LoadFeedInteractor(
        private val eventsRepository: EventsRepository,
        private val locationRepository: LocationRepository
) : Interactor<LoadFeedInteractor.RequestValues, List<RemoteEvent>> {

    suspend operator fun invoke(
            forceLoad: Boolean,
            filter: Filter<RemoteEvent>,
            sortingStrategy: Comparator<RemoteEvent>
    ): Either<List<RemoteEvent>, Throwable> = withContext(Dispatchers.IO) {

        val coordinates = locationRepository.getCoordinates().orNull()
        eventsRepository.getWeekFeedSuspending(forceLoad).map { events ->
            events.map { RemoteEvent.from(it, coordinates) }
                    .filter { filter(it) }
                    .sortedWith(sortingStrategy)
        }
    }

    override fun execute(arg: RequestValues, callback: InteractorCallback<List<RemoteEvent>>) {
        loadEvents(arg, callback)
    }

    private fun loadEvents(requestValues: RequestValues, callback: InteractorCallback<List<RemoteEvent>>) {
        locationRepository.getLastLocation(object : RepositoryCallback<Position> {
            override fun onResult(result: Position) {
                loadEvents(requestValues, result.coordinates, callback)
            }

            override fun onFailure(t: Throwable?) {
                loadEvents(requestValues, null, callback)
            }
        })
    }

    private fun loadEvents(
            requestValues: RequestValues,
            coordinates: Coordinates?,
            callback: InteractorCallback<List<RemoteEvent>>
    ) {
        eventsRepository.getWeekFeed(object : RepositoryCallback<List<Event>> {
            override fun onResult(result: List<Event>) {
                val (_, filter, comparator) = requestValues
                val events = result.map { RemoteEvent.from(it, coordinates) }
                        .filter { filter(it) }
                        .sortedWith(comparator)

                callback.onResult(events)
            }

            override fun onFailure(t: Throwable?) {
                callback.onFailure(t)
            }
        }, requestValues.forceLoad)
    }


    data class RequestValues(
            val forceLoad: Boolean,
            val filter: Filter<RemoteEvent>,
            val comparator: Comparator<RemoteEvent>
    )
}