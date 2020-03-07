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
import com.google.android.gms.common.api.ResolvableApiException

interface FeedContract {

    interface View : BaseView<Presenter> {

        fun isActive(): Boolean

        fun showProgress(active: Boolean)

        fun showSelectedPlaceName(name: String)

        fun showSelectedPlace(placeId: Int)

        fun showCurrentSortCriterion(sortCriterion: SortCriterion)

        fun showCurrentSortOrder(sortOrder: SortOrder)

        fun showCurrentMagnitudeFilter(magnitudeLevel: MagnitudeLevel)

        fun showPlaces(places: List<PlaceViewBlock>)

        fun showEvents(events: List<EventViewBlock>)

        fun showError(error: Error)

        fun showPlacesEditor()

        fun showEventDetails(eventId: String)

        fun renderLocationPermissionRequest()

        fun renderLocationSettingsPrompt(resolvableException: ResolvableApiException)
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

        fun onResolveError(error: Error)

        fun ignoreUpcomingStartCall()
    }
}

sealed class Error {

    sealed class TransientError(
            @StringRes val titleResId: Int,
            @StringRes val buttonResId: Int? = null
    ) : Error() {
        object NoConnection : TransientError(R.string.app_error_no_connection)
    }

    sealed class PersistentError(
            @StringRes val titleResId: Int,
            @StringRes val captionResId: Int,
            @DrawableRes val iconResId: Int,
            @StringRes val buttonResId: Int? = null
    ) : Error() {
        object NoEvents : PersistentError(
                R.string.app_error_no_events,
                R.string.app_error_no_events_capt,
                R.drawable.ic_error_earth_24px
        )

        object NoConnection : PersistentError(
                R.string.app_error_no_connection,
                R.string.app_error_no_connection_capt,
                R.drawable.ic_error_wifi_off_24px,
                R.string.app_action_retry
        )

        object NoLocationPermission : PersistentError(
                R.string.feed_error_no_location_permission,
                R.string.feed_error_no_location_permission_capt,
                R.drawable.ic_error_no_location_permission,
                R.string.app_action_grant_permission
        )

        class LocationIsOff(val resolvableException: ResolvableApiException) : PersistentError(
                R.string.feed_error_location_is_off,
                R.string.feed_error_location_is_off_capt,
                R.drawable.ic_error_location_is_off,
                R.string.app_action_enable_location
        )

        object Unknown : PersistentError(
                R.string.app_error_unknown,
                R.string.app_error_unknown_capt,
                R.drawable.ic_error_cloud_off_24dp,
                R.string.app_action_retry
        )
    }
}