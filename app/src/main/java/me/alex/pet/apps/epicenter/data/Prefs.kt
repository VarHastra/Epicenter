package me.alex.pet.apps.epicenter.data

import com.chibatching.kotpref.KotprefModel
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.doublePref
import me.alex.pet.apps.epicenter.domain.model.Coordinates
import me.alex.pet.apps.epicenter.domain.model.Place
import me.alex.pet.apps.epicenter.domain.model.filters.MagnitudeLevel
import me.alex.pet.apps.epicenter.domain.model.sorting.SortCriterion
import me.alex.pet.apps.epicenter.domain.model.sorting.SortOrder
import me.alex.pet.apps.epicenter.domain.repos.UnitsLocaleRepository
import me.alex.pet.apps.epicenter.domain.state.CameraState
import me.alex.pet.apps.epicenter.domain.state.FeedStateDataSource
import me.alex.pet.apps.epicenter.domain.state.MapStateDataSource
import me.alex.pet.apps.epicenter.presentation.common.UnitsLocale

object AppState : KotprefModel() {
    var isFirstLaunch by booleanPref(default = true, key = R.string.pref_first_launch_key)
}


object AppSettings : KotprefModel(), UnitsLocaleRepository {
    override val kotprefName: String
        get() = context.getString(R.string.prefs_app_settings)

    override val preferredUnits
        get() = when (_preferredUnits) {
            "0" -> UnitsLocale.getDefault()
            "1" -> UnitsLocale.METRIC
            "2" -> UnitsLocale.IMPERIAL
            else -> UnitsLocale.getDefault()
        }

    private val _preferredUnits by stringPref(default = "0", key = R.string.pref_units_key)
}


object FeedState : KotprefModel(), FeedStateDataSource {

    override var selectedPlaceId by intPref(default = Place.WORLD.id, key = R.string.pref_feed_selected_place)

    override var sortCriterion
        get() = SortCriterion.fromValue(_sortCriterion)
        set(value) {
            _sortCriterion = value.value
        }

    var _sortCriterion by intPref(default = SortCriterion.DATE.value, key = R.string.pref_feed_sort_criterion)

    override var sortOrder
        get() = SortOrder.fromValue(_sortOrder)
        set(value) {
            _sortOrder = value.value
        }

    var _sortOrder by intPref(default = SortOrder.ASCENDING.value, key = R.string.pref_feed_sort_order)

    override var minMagnitude
        get() = MagnitudeLevel.fromValue(_minMagnitude)
        set(value) {
            _minMagnitude = value.value
        }

    var _minMagnitude by intPref(default = MagnitudeLevel.ZERO_OR_LESS.value, key = R.string.pref_feed_min_mag)
}


object MapState : KotprefModel(), MapStateDataSource {

    override var cameraState: CameraState
        get() {
            return CameraState(_zoomLevel, Coordinates(_cameraLat, _cameraLon))
        }
        set(value) {
            _zoomLevel = value.zoomLevel
            _cameraLat = value.position.latitude
            _cameraLon = value.position.longitude
        }

    private var _zoomLevel by floatPref(default = 3.0f, key = R.string.pref_map_camera_zoom)

    private var _cameraLat by doublePref(default = 0.0, key = R.string.pref_map_cam_lat)

    private var _cameraLon by doublePref(default = 0.0, key = R.string.pref_map_cam_lon)

    override var minMagnitude: MagnitudeLevel
        get() = MagnitudeLevel.fromValue(_minMag)
        set(value) {
            _minMag = value.value
        }

    private var _minMag by intPref(default = MagnitudeLevel.ZERO_OR_LESS.value, key = R.string.pref_map_filter_min_mag)

    override var numberOfDaysToShow by intPref(default = 7, key = R.string.pref_map_filter_days_ago)
}