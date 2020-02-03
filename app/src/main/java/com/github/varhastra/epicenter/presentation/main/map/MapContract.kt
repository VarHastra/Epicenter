package com.github.varhastra.epicenter.presentation.main.map

import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeLevel
import com.github.varhastra.epicenter.presentation.BasePresenter
import com.github.varhastra.epicenter.presentation.BaseView
import com.github.varhastra.epicenter.presentation.common.EventMarker

interface MapContract {

    interface View : BaseView<Presenter> {
        fun showTitle()

        fun showEventMarkers(markers: List<EventMarker>)

        fun isActive(): Boolean

        fun showProgress(show: Boolean)

        fun showEventDetails(eventId: String)

        fun showFilters()

        fun showCurrentMagnitudeFilter(magnitudeLevel: MagnitudeLevel)

        fun showCurrentDaysFilter(days: Int)

        fun setCameraPosition(coordinates: Coordinates, zoom: Float)

        fun zoomIn(latitude: Double, longitude: Double)
    }

    interface Presenter : BasePresenter {
        fun loadEvents()

        fun reloadEvents()

        fun openFilters()

        fun setMinMagnitude(magnitudeLevel: MagnitudeLevel)

        fun setNumberOfDaysToShow(days: Int)

        fun openEventDetails(eventId: String)

        fun saveCameraPosition(coordinates: Coordinates, zoom: Float)

        fun onZoomIn(latitude: Double, longitude: Double)
    }
}