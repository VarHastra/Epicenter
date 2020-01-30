package com.github.varhastra.epicenter.presentation.main.feed

import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeLevel
import com.github.varhastra.epicenter.domain.model.sorting.SortCriterion
import com.github.varhastra.epicenter.domain.model.sorting.SortOrder
import com.github.varhastra.epicenter.presentation.BasePresenter
import com.github.varhastra.epicenter.presentation.BaseView
import com.github.varhastra.epicenter.presentation.common.UnitsLocale

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

        fun showTitle()

        fun showProgress(active: Boolean)

        fun showCurrentPlace(place: Place)

        fun showCurrentSortCriterion(sortCriterion: SortCriterion)

        fun showCurrentSortOrder(sortOrder: SortOrder)

        fun showCurrentMagnitudeFilter(magnitudeLevel: MagnitudeLevel)

        fun showPlaces(places: List<Place>, unitsLocale: UnitsLocale)

        fun showEvents(events: List<EventViewBlock>)

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

        fun setSortCriterion(sortCriterion: SortCriterion)

        fun setSortOrder(sortOrder: SortOrder)

        fun setMinMagnitude(magnitudeLevel: MagnitudeLevel)

        fun openPlacesEditor()

        fun openEventDetails(eventId: String)

        fun ignoreUpcomingStartCall()
    }
}