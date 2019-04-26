package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.EventsDataSource
import com.github.varhastra.epicenter.domain.LocationDataSource
import com.github.varhastra.epicenter.domain.model.*

class FeedLoaderInteractor(
    private val eventsDataSource: EventsDataSource,
    private val locationDataSource: LocationDataSource
) : Interactor<FeedLoaderInteractor.RequestValues, List<RemoteEvent>> {


    override fun execute(arg: RequestValues, callback: InteractorCallback<List<RemoteEvent>>) {
        loadEvents(arg, callback)
    }

    private fun loadEvents(param: RequestValues, callback: InteractorCallback<List<RemoteEvent>>) {
        // Try to get current user location first
        locationDataSource.getLastLocation(object : DataSourceCallback<Position> {
            override fun onResult(result: Position) {
                // Location is available. Use it to calculate the distance to each event.
                loadEvents(param, result.coordinates, callback)
            }

            override fun onFailure(t: Throwable?) {
                // If current location is not available then just pass null
                // RemoteEvent will take care of it
                loadEvents(param, null, callback)
            }
        })
    }

    private fun loadEvents(
        param: RequestValues,
        coordinates: Coordinates?,
        callback: InteractorCallback<List<RemoteEvent>>
    ) {
        // Get events
        eventsDataSource.getWeekFeed(object : DataSourceCallback<List<Event>> {
            override fun onResult(result: List<Event>) {
                // Convert each Event to RemoteEvent (calculate the distance from the user's location)
                val events = RemoteEvent.from(result, coordinates)

                // Apply filters and return
                callback.onResult(filter(events, param.filter, param.place))
            }

            override fun onFailure(t: Throwable?) {
                callback.onFailure(t)
            }
        }, param.forceLoad)
    }

    private fun filter(events: List<RemoteEvent>, filter: FeedFilter, place: Place): List<RemoteEvent> {
        val result = events.filter { place.checkCoordinates(it.event.coordinates) }
        return filter.applyTo(result)
    }


    class RequestValues(
        val forceLoad: Boolean,
        val filter: FeedFilter,
        val place: Place
    )
}