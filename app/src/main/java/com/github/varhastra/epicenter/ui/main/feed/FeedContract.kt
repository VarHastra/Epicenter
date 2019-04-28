package com.github.varhastra.epicenter.ui.main.feed

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView
import com.github.varhastra.epicenter.domain.model.FeedFilter
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.RemoteEvent

interface FeedContract {

    interface View : BaseView<Presenter> {
        enum class ErrorReason {
            ERR_NO_EVENTS,
            ERR_NO_CONNECTION,
            ERR_UNKNOWN
        }

        interface PermissionRequestCallback {
            fun onGranted()

            fun onDenied()
        }

        fun isActive(): Boolean

        fun showProgress(active: Boolean)

        fun showCurrentPlace(place: Place)

        fun showCurrentFilter(filter: FeedFilter)

        fun showPlaces(places: List<Place>)

        fun showEvents(events: List<RemoteEvent>)

        fun showErrorNoData(reason: ErrorReason)

        fun showLocationPermissionRequest(callback: PermissionRequestCallback)

        fun showErrorLocationNotAvailable()

        fun showErrorNoConnection()

        fun showPlacesEditor()

        fun showEventDetails(eventId: String)
    }

    interface Presenter : BasePresenter {
        fun init()

        fun loadPlaces()

        fun loadEvents()

        fun refreshEvents()

        fun setPlaceAndReload(place: Place)

        fun setFilterAndReload(filter: FeedFilter)

        fun setMagnitudeFilterAndReload(minMag: Int)

        fun setSortingAndReload(sorting: FeedFilter.Sorting)

        fun openPlacesEditor()

        fun openEventDetails(eventId: String)

        fun ignoreUpcomingStartCall()
    }
}