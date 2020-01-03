package com.github.varhastra.epicenter.presentation.main.map

import com.github.varhastra.epicenter.domain.ConnectivityRepository
import com.github.varhastra.epicenter.domain.EventsRepository
import com.github.varhastra.epicenter.domain.LocationRepository
import com.github.varhastra.epicenter.domain.interactors.MapEventsLoaderInteractor
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.state.MapState
import com.github.varhastra.epicenter.domain.state.MapStateDataSource
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit

class MapPresenter(
        private val view: MapContract.View,
        private val mapStateDataSource: MapStateDataSource,
        private val eventsRepository: EventsRepository,
        private val locationRepository: LocationRepository,
        private val connectivityRepository: ConnectivityRepository
) : MapContract.Presenter {

    private var state: MapState = MapState()
    private val mapEventsLoaderInteractor: MapEventsLoaderInteractor = MapEventsLoaderInteractor(eventsRepository, locationRepository)

    init {
        view.attachPresenter(this)

        mapEventsLoaderInteractor.onResult = { events ->
            if (view.isActive()) {
                val markers = events.map { EventMarker.fromRemoteEvent(it) }
                view.showProgress(false)
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
            if (view.isActive()) {
                view.showProgress(false)
            }
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
        view.setCameraPosition(state.cameraPosition, state.zoomLevel)
    }

    override fun loadEvents() {
        getEvents(false)
    }

    override fun reloadEvents() {
        getEvents(true)
    }

    private fun getEvents(requestForceLoad: Boolean) {
        val connectionAvailable = connectivityRepository.isNetworkConnected()
        if (requestForceLoad && !connectionAvailable) {
            // TODO
//            view.showErrorNoConnection()
        }

        view.showProgress(true)

        val minsSinceLastUpd = ChronoUnit.MINUTES.between(eventsRepository.getWeekFeedLastUpdated(), Instant.now())
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

    override fun saveCameraPosition(coordinates: Coordinates, zoom: Float) {
        state = state.copy(cameraPosition = coordinates, zoomLevel = zoom)
        mapStateDataSource.saveMapState(state)
    }

    override fun openEventDetails(eventId: String) {
        view.showEventDetails(eventId)
    }

    companion object {
        const val FORCE_LOAD_RATE_MINS = 10
    }
}