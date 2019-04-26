package com.github.varhastra.epicenter.data

import android.content.Context
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.domain.model.FeedFilter
import com.github.varhastra.epicenter.domain.state.FeedStateDataSource
import com.github.varhastra.epicenter.utils.UnitsLocale
import com.github.varhastra.epicenter.utils.getDouble
import com.github.varhastra.epicenter.utils.putDouble
import org.jetbrains.anko.defaultSharedPreferences


object Prefs : FeedStateDataSource {
    private const val PREF_UNITS = "PREF_UNITS"
    private const val PREF_FEED_PLACE_ID = "PREF_FEED_PLACE_ID"
    private const val PREF_FEED_FILTER_MAG = "PREF_FEED_FILTER_MAG"
    private const val PREF_FEED_FILTER_SORT = "PREF_FEED_FILTER_SORT"

    fun getPreferredUnits(context: Context = App.instance): UnitsLocale {
        val prefUnits = context.defaultSharedPreferences.getString(PREF_UNITS, null)

        return when (prefUnits) {
            "0" -> UnitsLocale.getDefault()
            "1" -> UnitsLocale.METRIC
            "2" -> UnitsLocale.IMPERIAL
            else -> UnitsLocale.getDefault()
        }
    }

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
        // The default value is 1 because the "World" place id is 1
        return context.defaultSharedPreferences.getInt(PREF_FEED_PLACE_ID, 1)
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
}