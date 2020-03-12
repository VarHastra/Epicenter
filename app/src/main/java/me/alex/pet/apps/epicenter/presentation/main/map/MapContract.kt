package me.alex.pet.apps.epicenter.presentation.main.map

import me.alex.pet.apps.epicenter.domain.model.Coordinates
import me.alex.pet.apps.epicenter.domain.model.filters.MagnitudeLevel
import me.alex.pet.apps.epicenter.presentation.BasePresenter
import me.alex.pet.apps.epicenter.presentation.BaseView
import me.alex.pet.apps.epicenter.presentation.common.EventMarker

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