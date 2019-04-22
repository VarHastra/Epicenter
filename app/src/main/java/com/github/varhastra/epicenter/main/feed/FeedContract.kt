package com.github.varhastra.epicenter.main.feed

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView
import com.github.varhastra.epicenter.domain.model.Event
import com.github.varhastra.epicenter.domain.model.FeedFilter
import com.github.varhastra.epicenter.domain.model.Place

interface FeedContract {

    interface View : BaseView<Presenter> {
        enum class ErrorReason {
            ERR_NO_EVENTS,
            ERR_NO_CONNECTION,
            ERR_UNKNOWN
        }

        fun isActive(): Boolean

        fun showProgress(active: Boolean)

        fun showCurrentPlace(place: Place)

        fun showCurrentFilter(filter: FeedFilter)

        fun showPlaces(places: List<Place>)

        fun showEvents(events: List<Event>)

        fun showError(reason: ErrorReason)
    }

    interface Presenter : BasePresenter {
        fun init()

        fun loadPlaces()

        fun loadEvents()

        fun setPlaceAndReload(place: Place)

        fun setPlaceAndReload(placeId: Int)

        fun setFilterAndReload(filter: FeedFilter)

        fun setMagnitudeFilterAndReload(minMag: Int)

        fun setSortingAndReload(sorting: FeedFilter.Sorting)
    }
}