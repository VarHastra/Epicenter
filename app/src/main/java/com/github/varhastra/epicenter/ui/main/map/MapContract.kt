package com.github.varhastra.epicenter.ui.main.map

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView
import com.github.varhastra.epicenter.domain.model.Coordinates

interface MapContract {

    interface View : BaseView<Presenter> {
        fun showTitle()

        fun showEventMarkers(markers: List<EventMarker>)

        fun isActive(): Boolean

        fun isReady(): Boolean

        fun showProgress(show: Boolean)

        fun showEventDetails(eventId: String)

        fun showFilters()

        fun showCurrentMagnitudeFilter(magnitude: Int)

        fun showCurrentDaysFilter(days: Int)

        fun setCameraPosition(coordinates: Coordinates, zoom: Float)
    }

    interface Presenter : BasePresenter {
        fun loadEvents()

        fun reloadEvents()

        fun openFilters()

        fun setMinMagnitude(minMagnitude: Int)

        fun setPeriod(days: Int)

        fun viewReady()

        fun openEventDetails(eventId: String)

        fun saveCameraPosition(coordinates: Coordinates, zoom: Float)
    }
}