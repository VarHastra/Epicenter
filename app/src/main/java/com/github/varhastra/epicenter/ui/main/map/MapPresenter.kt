package com.github.varhastra.epicenter.ui.main.map

import com.github.varhastra.epicenter.domain.ConnectivityDataSource
import com.github.varhastra.epicenter.domain.EventsDataSource
import com.github.varhastra.epicenter.domain.LocationDataSource
import com.github.varhastra.epicenter.domain.interactors.MapEventsLoaderInteractor
import com.github.varhastra.epicenter.domain.state.MapState
import com.github.varhastra.epicenter.domain.state.MapStateDataSource
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit

class MapPresenter(
        private val view: MapContract.View,
        private val mapStateDataSource: MapStateDataSource,
        private val eventsDataSource: EventsDataSource,
        private val locationDataSource: LocationDataSource,
        private val connectivityDataSource: ConnectivityDataSource
) : MapContract.Presenter {

    private var state: MapState = MapState()
    private val mapEventsLoaderInteractor: MapEventsLoaderInteractor = MapEventsLoaderInteractor(eventsDataSource, locationDataSource)

    init {
        view.attachPresenter(this)

        mapEventsLoaderInteractor.onResult = { events ->
            val markers = events.map { EventMarker.fromRemoteEvent(it) }
            view.showEventMarkers(markers)
        }

        mapEventsLoaderInteractor.onFailure = {
            // TODO
        }
    }

    override fun start() {
        view.showTitle()
        state = mapStateDataSource.getMapState()
        loadEvents()
    }

    override fun viewReady() {
        loadEvents()
    }

    override fun loadEvents() {
        getEvents(false)
    }

    private fun getEvents(requestForceLoad: Boolean) {
        val connectionAvailable = connectivityDataSource.isNetworkConnected()
        if (requestForceLoad && !connectionAvailable) {
            // TODO
//            view.showErrorNoConnection()
        }

        val minsSinceLastUpd = ChronoUnit.MINUTES.between(eventsDataSource.getWeekFeedLastUpdated(), Instant.now())
        val forceLoad = (requestForceLoad || minsSinceLastUpd > FORCE_LOAD_RATE_MINS) && connectionAvailable

        val requestValues = MapEventsLoaderInteractor.RequestValues(state.filter, forceLoad)
        mapEventsLoaderInteractor.execute(requestValues)
    }

    override fun setMinMagnitude(minMagnitude: Int) {
        state.filter.copy(minMagnitude = minMagnitude.toDouble())
        loadEvents()
    }

    override fun setPeriod(days: Int) {
        state.filter.copy(periodDays = days)
        loadEvents()
    }

    override fun openEventDetails(eventId: String) {
        view.showEventDetails(eventId)
    }

    companion object {
        const val FORCE_LOAD_RATE_MINS = 10
    }
}