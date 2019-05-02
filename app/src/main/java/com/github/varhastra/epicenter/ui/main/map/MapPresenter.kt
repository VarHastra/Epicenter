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
            if (view.isActive()) {
                val markers = events.map { EventMarker.fromRemoteEvent(it) }
                if (view.isReady()) {
                    view.showEventMarkers(markers)
                } else {
                    Runnable {
                        for (i in 1..3) {
                            Thread.sleep(1000)
                            if (view.isReady()) {
                                view.showEventMarkers(markers)
                                break
                            }
                        }
                    }.run()
                }
            }
        }

        mapEventsLoaderInteractor.onFailure = {
            // TODO
        }
    }

    override fun start() {
        view.showTitle()
        state = mapStateDataSource.getMapState()
        with(state.filter) {
            view.showCurrentDaysFilter(periodDays)
            view.showCurrentMagnitudeFilter(minMagnitude.toInt())
        }
        loadEvents()
    }

    override fun viewReady() {
        // Do nothing
    }

    override fun loadEvents() {
        getEvents(false)
    }

    override fun reloadEvents() {
        getEvents(true)
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

    override fun openFilters() {
        view.showFilters()
    }

    override fun setMinMagnitude(minMagnitude: Int) {
        state = state.copy(filter = state.filter.copy(minMagnitude = minMagnitude.toDouble()))
        mapStateDataSource.saveMapState(state)
        loadEvents()
    }

    override fun setPeriod(days: Int) {
        state = state.copy(filter = state.filter.copy(periodDays = days))
        mapStateDataSource.saveMapState(state)
        loadEvents()
    }

    override fun openEventDetails(eventId: String) {
        view.showEventDetails(eventId)
    }

    companion object {
        const val FORCE_LOAD_RATE_MINS = 10
    }
}