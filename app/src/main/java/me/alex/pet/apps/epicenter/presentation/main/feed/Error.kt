package me.alex.pet.apps.epicenter.presentation.main.feed

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.gms.common.api.ResolvableApiException
import me.alex.pet.apps.epicenter.R

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