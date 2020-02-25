package com.github.varhastra.epicenter.presentation.main.feed

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeLevel
import com.github.varhastra.epicenter.domain.model.sorting.SortCriterion
import com.github.varhastra.epicenter.domain.model.sorting.SortOrder
import com.github.varhastra.epicenter.presentation.BasePresenter
import com.github.varhastra.epicenter.presentation.BaseView

interface FeedContract {

    interface View : BaseView<Presenter> {
        enum class ErrorType(
                @StringRes val titleResId: Int,
                @StringRes val bodyResId: Int,
                @DrawableRes val iconResId: Int
        ) {
            NO_EVENTS(
                    R.string.app_error_no_events,
                    R.string.app_error_no_events_capt,
                    R.drawable.ic_error_earth_24px
            ),
            NO_CONNECTION(
                    R.string.app_error_no_connection,
                    R.string.app_error_no_connection_capt,
                    R.drawable.ic_error_wifi_off_24px
            ),
            UNKNOWN(
                    R.string.app_error_unknown,
                    R.string.app_error_unknown_capt,
                    R.drawable.ic_error_cloud_off_24dp
            )
        }

        interface PermissionRequestCallback {
            fun onGranted()

            fun onDenied()
        }

        fun isActive(): Boolean

        fun showProgress(active: Boolean)

        fun showSelectedPlaceName(name: String)

        fun showSelectedPlace(placeId: Int)

        fun showCurrentSortCriterion(sortCriterion: SortCriterion)

        fun showCurrentSortOrder(sortOrder: SortOrder)

        fun showCurrentMagnitudeFilter(magnitudeLevel: MagnitudeLevel)

        fun showPlaces(places: List<PlaceViewBlock>)

        fun showEvents(events: List<EventViewBlock>)

        fun showErrorNoData(errorType: ErrorType)

        fun showErrorLocationNotAvailable()

        fun showErrorNoConnection()

        fun showPlacesEditor()

        fun showEventDetails(eventId: String)
    }

    interface Presenter : BasePresenter {
        fun init()

        fun loadEvents()

        fun refreshEvents()

        fun setPlaceAndReload(place: Place)

        fun setPlaceAndReload(placeId: Int)

        fun setSortCriterion(sortCriterion: SortCriterion)

        fun setSortOrder(sortOrder: SortOrder)

        fun setMinMagnitude(magnitudeLevel: MagnitudeLevel)

        fun openPlacesEditor()

        fun openEventDetails(eventId: String)

        fun ignoreUpcomingStartCall()
    }
}