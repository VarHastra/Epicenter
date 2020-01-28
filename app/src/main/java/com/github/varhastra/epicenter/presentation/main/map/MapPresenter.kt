package com.github.varhastra.epicenter.presentation.main.map

import com.github.varhastra.epicenter.domain.interactors.LoadMapEventsInteractor
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.filters.AndFilter
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeFilter
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeLevel
import com.github.varhastra.epicenter.domain.model.filters.RecencyFilter
import com.github.varhastra.epicenter.domain.repos.EventsRepository
import com.github.varhastra.epicenter.domain.repos.LocationRepository
import com.github.varhastra.epicenter.domain.state.CameraState
import com.github.varhastra.epicenter.domain.state.MapStateDataSource

class MapPresenter(
        private val view: MapContract.View,
        private val mapStateDataSource: MapStateDataSource,
        private val eventsRepository: EventsRepository,
        private val locationRepository: LocationRepository
) : MapContract.Presenter {

    private var state: CameraState = CameraState()
    private var minMagnitude: MagnitudeLevel = MagnitudeLevel.ZERO_OR_LESS
    private var numberOfDaysToShow: Int = 1
    private val loadMapEventsInteractor: LoadMapEventsInteractor = LoadMapEventsInteractor(eventsRepository, locationRepository)

    init {
        view.attachPresenter(this)

        loadMapEventsInteractor.onResult = { events ->
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

        loadMapEventsInteractor.onFailure = {
            if (view.isActive()) {
                view.showProgress(false)
            }
            // TODO
        }
    }

    override fun start() {
        state = mapStateDataSource.cameraState
        numberOfDaysToShow = mapStateDataSource.numberOfDaysToShow
        minMagnitude = mapStateDataSource.minMagnitude

        view.showTitle()
        view.showCurrentDaysFilter(numberOfDaysToShow)
        view.showCurrentMagnitudeFilter(minMagnitude)

        loadEvents()
    }

    override fun viewReady() {
        view.setCameraPosition(state.position, state.zoomLevel)
    }

    override fun loadEvents() {
        getEvents(false)
    }

    override fun reloadEvents() {
        getEvents(true)
    }

    private fun getEvents(forceLoad: Boolean) {
        view.showProgress(true)

        val filter = AndFilter(MagnitudeFilter(minMagnitude), RecencyFilter(numberOfDaysToShow))
        val requestValues = LoadMapEventsInteractor.RequestValues(forceLoad, filter)
        loadMapEventsInteractor.execute(requestValues)
    }

    override fun openFilters() {
        view.showFilters()
    }

    override fun setMinMagnitude(magnitudeLevel: MagnitudeLevel) {
        this.minMagnitude = magnitudeLevel
        mapStateDataSource.minMagnitude = magnitudeLevel
        loadEvents()
    }

    override fun setNumberOfDaysToShow(days: Int) {
        this.numberOfDaysToShow = days
        mapStateDataSource.numberOfDaysToShow = days
        loadEvents()
    }

    override fun saveCameraPosition(coordinates: Coordinates, zoom: Float) {
        state = state.copy(position = coordinates, zoomLevel = zoom)
        mapStateDataSource.cameraState = state
    }

    override fun openEventDetails(eventId: String) {
        view.showEventDetails(eventId)
    }
}