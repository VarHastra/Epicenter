package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.EventsDataSource
import com.github.varhastra.epicenter.domain.LocationRepository
import com.github.varhastra.epicenter.domain.model.*

class MapEventsLoaderInteractor(
        private val eventsDataSource: EventsDataSource,
        private val locationRepository: LocationRepository) : FunctionalInteractor<MapEventsLoaderInteractor.RequestValues, List<RemoteEvent>> {

    override var onResult: ((List<RemoteEvent>) -> Unit)? = null
    override var onFailure: ((Throwable?) -> Unit)? = null


    override fun execute(arg: RequestValues) {
        loadEvents(arg)
    }

    private fun loadEvents(param: RequestValues) {
        // Try to get current user location first
        locationRepository.getLastLocation(object : DataSourceCallback<Position> {
            override fun onResult(result: Position) {
                // Location is available. Use it to calculate the distance to each event.
                loadEvents(param, result.coordinates)
            }

            override fun onFailure(t: Throwable?) {
                // If current location is not available then just pass null
                // RemoteEvent will take care of it
                loadEvents(param, null)
            }
        })
    }

    private fun loadEvents(
            param: RequestValues,
            coordinates: Coordinates?
    ) {
        // Get events
        eventsDataSource.getWeekFeed(object : DataSourceCallback<List<Event>> {
            override fun onResult(result: List<Event>) {
                // Convert each Event to RemoteEvent (calculate the distance from the user's location)
                val events = RemoteEvent.from(result, coordinates)

                // Apply filters and return
                onResult?.invoke(filter(events, param.filter))
            }

            override fun onFailure(t: Throwable?) {
                onFailure?.invoke(t)
            }
        }, param.forceLoad)
    }

    private fun filter(events: List<RemoteEvent>, mapFilter: MapFilter): List<RemoteEvent> {
        return events.filter { mapFilter.filter(it) }
    }

    class RequestValues(
            val filter: MapFilter,
            val forceLoad: Boolean
    )
}