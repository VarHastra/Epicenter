package com.github.varhastra.epicenter.presentation.main.map

import android.content.Context
import com.github.varhastra.epicenter.domain.interactors.LoadMapEventsInteractor
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.domain.model.filters.AndFilter
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeFilter
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeLevel
import com.github.varhastra.epicenter.domain.model.filters.RecencyFilter
import com.github.varhastra.epicenter.domain.state.CameraState
import com.github.varhastra.epicenter.domain.state.MapStateDataSource
import com.github.varhastra.epicenter.presentation.common.EventMarker
import com.github.varhastra.epicenter.presentation.common.Mapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapPresenter(
        private val context: Context,
        private val view: MapContract.View,
        private val mapStateDataSource: MapStateDataSource,
        private val loadEventsInteractor: LoadMapEventsInteractor
) : MapContract.Presenter {

    private var state: CameraState = CameraState()

    private var minMagnitude: MagnitudeLevel = MagnitudeLevel.ZERO_OR_LESS

    private var numberOfDaysToShow: Int = 1

    init {
        view.attachPresenter(this)
    }

    override fun start() {
        state = mapStateDataSource.cameraState
        numberOfDaysToShow = mapStateDataSource.numberOfDaysToShow
        minMagnitude = mapStateDataSource.minMagnitude

        view.showTitle()
        view.showCurrentDaysFilter(numberOfDaysToShow)
        view.showCurrentMagnitudeFilter(minMagnitude)
        view.setCameraPosition(state.position, state.zoomLevel)

        loadEvents()
    }

    override fun loadEvents() {
        getEvents(false)
    }

    override fun reloadEvents() {
        getEvents(true)
    }

    private fun getEvents(forceLoad: Boolean) {
        val filter = AndFilter(MagnitudeFilter(minMagnitude), RecencyFilter(numberOfDaysToShow))

        view.showProgress(true)
        CoroutineScope(Dispatchers.Main).launch {
            loadEventsInteractor(forceLoad, filter).map { events -> mapEventsToMarkers(events) }
                    .fold(::handleResult, ::handleFailure)
        }
    }

    private suspend fun mapEventsToMarkers(events: List<RemoteEvent>) = withContext(Dispatchers.Default) {
        val mapper = Mapper(context)
        events.map { mapper.map(it) }
    }

    private fun handleResult(markers: List<EventMarker>) {
        if (!view.isActive()) {
            return
        }

        view.showProgress(false)
        view.showEventMarkers(markers)
    }

    private fun handleFailure(t: Throwable) {
        if (!view.isActive()) {
            return
        }

        view.showProgress(false)
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