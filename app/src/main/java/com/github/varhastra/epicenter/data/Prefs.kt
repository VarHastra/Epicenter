package com.github.varhastra.epicenter.data

import android.content.Context
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.FeedFilter
import com.github.varhastra.epicenter.domain.model.MapFilter
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.state.FeedStateDataSource
import com.github.varhastra.epicenter.domain.state.MapState
import com.github.varhastra.epicenter.domain.state.MapStateDataSource
import com.github.varhastra.epicenter.utils.UnitsLocale
import com.github.varhastra.epicenter.utils.getDouble
import com.github.varhastra.epicenter.utils.putDouble
import org.jetbrains.anko.defaultSharedPreferences


object Prefs : FeedStateDataSource, MapStateDataSource {
    private const val PREF_FIRST_LAUNCH = "PREF_FIRST_LAUNCH"
    private const val PREF_UNITS = "PREF_UNITS"

    private const val PREF_FEED_PLACE_ID = "PREF_FEED_PLACE_ID"
    private const val PREF_FEED_FILTER_MAG = "PREF_FEED_FILTER_MAG"
    private const val PREF_FEED_FILTER_SORT = "PREF_FEED_FILTER_SORT"

    private const val PREF_MAP_FILTER_MAG = "PREF_MAP_FILTER_MAG"
    private const val PREF_MAP_FILTER_DAYS = "PREF_MAP_FILTER_DAYS"
    private const val PREF_MAP_ZOOM_LEVEL = "PREF_MAP_ZOOM_LEVEL"
    private const val PREF_MAP_CAM_LAT = "PREF_MAP_CAM_LAT"
    private const val PREF_MAP_CAM_LNG = "PREF_MAP_CAM_LNG"

    fun isFirstLaunch(context: Context = App.instance): Boolean {
        return context.defaultSharedPreferences.getBoolean(PREF_FIRST_LAUNCH, true)
    }

    fun saveFirstLaunchFinished(context: Context = App.instance) {
        context.defaultSharedPreferences.edit()
                .putBoolean(PREF_FIRST_LAUNCH, false)
                .apply()
    }

    fun getPreferredUnits(context: Context = App.instance): UnitsLocale {
        val prefUnits = context.defaultSharedPreferences.getString(PREF_UNITS, null)

        return when (prefUnits) {
            "0" -> UnitsLocale.getDefault()
            "1" -> UnitsLocale.METRIC
            "2" -> UnitsLocale.IMPERIAL
            else -> UnitsLocale.getDefault()
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FeedStateDataSource methods
    ////////////////////////////////////////////////////////////////////////////////////////////////

    override fun saveSelectedPlaceId(id: Int) = storeSelectedPlaceId(id)

    override fun getSelectedPlaceId() = retrieveSelectedPlaceId()

    override fun saveCurrentFilter(filter: FeedFilter) = storeCurrentFilter(filter)

    override fun getCurrentFilter(): FeedFilter = retrieveCurrentFilter()


    private fun storeSelectedPlaceId(id: Int, context: Context = App.instance) {
        context.defaultSharedPreferences.edit()
                .putInt(PREF_FEED_PLACE_ID, id)
                .apply()
    }

    private fun retrieveSelectedPlaceId(context: Context = App.instance): Int {
        return context.defaultSharedPreferences.getInt(PREF_FEED_PLACE_ID, Place.WORLD.id)
    }

    private fun storeCurrentFilter(filter: FeedFilter, context: Context = App.instance) {
        with(filter) {
            context.defaultSharedPreferences.edit()
                    .putDouble(PREF_FEED_FILTER_MAG, minMagnitude)
                    .putInt(PREF_FEED_FILTER_SORT, filter.sorting.id)
                    .apply()
        }
    }

    private fun retrieveCurrentFilter(context: Context = App.instance): FeedFilter {
        return with(context.defaultSharedPreferences) {
            val mag = getDouble(PREF_FEED_FILTER_MAG, -2.0)
            val sortingId = getInt(PREF_FEED_FILTER_SORT, 0)
            return FeedFilter(mag, FeedFilter.Sorting.fromId(sortingId))
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // MapStateDataSourceMethods
    ////////////////////////////////////////////////////////////////////////////////////////////////
    override fun saveMapState(mapState: MapState) {
        storeMapState(mapState)
    }

    private fun storeMapState(mapState: MapState, context: Context = App.instance) {
        context.defaultSharedPreferences.edit().apply {
            mapState.filter.also {
                putInt(PREF_MAP_FILTER_DAYS, it.periodDays)
                putDouble(PREF_MAP_FILTER_MAG, it.minMagnitude)
            }
            mapState.also {
                putFloat(PREF_MAP_ZOOM_LEVEL, it.zoomLevel)
                putDouble(PREF_MAP_CAM_LAT, it.cameraPosition.latitude)
                putDouble(PREF_MAP_CAM_LNG, it.cameraPosition.longitude)
            }
        }.apply()
    }

    override fun getMapState(): MapState {
        return retrieveMapState()
    }

    private fun retrieveMapState(context: Context = App.instance): MapState {
        return with (context.defaultSharedPreferences) {
            val days = getInt(PREF_MAP_FILTER_DAYS, 1)
            val mag = getDouble(PREF_MAP_FILTER_MAG, -2.0)
            val zoom = getFloat(PREF_MAP_ZOOM_LEVEL, 3.0f)
            val lat = getDouble(PREF_MAP_CAM_LAT, 0.0)
            val lng = getDouble(PREF_MAP_CAM_LNG, 0.0)
            MapState(MapFilter(mag, days), zoom, Coordinates(lat, lng))
        }
    }
}