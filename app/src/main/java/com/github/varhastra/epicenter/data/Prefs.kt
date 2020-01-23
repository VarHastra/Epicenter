package com.github.varhastra.epicenter.data

import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.bulk
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.common.doublePref
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.FeedFilter
import com.github.varhastra.epicenter.domain.model.MapFilter
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.repos.UnitsLocaleRepository
import com.github.varhastra.epicenter.domain.state.FeedStateDataSource
import com.github.varhastra.epicenter.domain.state.MapState
import com.github.varhastra.epicenter.domain.state.MapStateDataSource
import com.github.varhastra.epicenter.presentation.common.UnitsLocale

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

    override var filter: FeedFilter
        get() {
            return FeedFilter(_minMagnitude, FeedFilter.Sorting.fromId(_sorting))
        }
        set(value) {
            bulk {
                _minMagnitude = value.minMagnitude
                _sorting = value.sorting.id
            }
        }

    private var _minMagnitude by doublePref(key = R.string.pref_feed_min_mag)

    private var _sorting by intPref(key = R.string.pref_feed_sorting)
}


object MapState : KotprefModel(), MapStateDataSource {

    override var value: MapState
        get() {
            return MapState(MapFilter(_minMag, _daysAgo), _zoomLevel, Coordinates(_cameraLat, _cameraLon))
        }
        set(value) {
            _minMag = value.filter.minMagnitude
            _daysAgo = value.filter.periodDays
            _zoomLevel = value.zoomLevel
            _cameraLat = value.cameraPosition.latitude
            _cameraLon = value.cameraPosition.longitude
        }

    private var _minMag by doublePref(default = -2.0, key = R.string.pref_map_filter_min_mag)

    private var _daysAgo by intPref(default = 7, key = R.string.pref_map_filter_days_ago)

    private var _zoomLevel by floatPref(default = 3.0f, key = R.string.pref_map_camera_zoom)

    private var _cameraLat by doublePref(default = 0.0, key = R.string.pref_map_cam_lat)

    private var _cameraLon by doublePref(default = 0.0, key = R.string.pref_map_cam_lon)
}