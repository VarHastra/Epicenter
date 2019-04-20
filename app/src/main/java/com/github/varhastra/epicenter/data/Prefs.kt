package com.github.varhastra.epicenter.data

import android.content.Context
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.domain.FeedStateDataSource
import com.github.varhastra.epicenter.domain.model.FeedFilter
import com.github.varhastra.epicenter.utils.UnitsLocale
import org.jetbrains.anko.defaultSharedPreferences


object Prefs : FeedStateDataSource {
    private const val PREF_UNITS = "PREF_UNITS"

    fun getPreferredUnits(context: Context = App.instance): UnitsLocale {
        val prefUnits = context.defaultSharedPreferences.getString(PREF_UNITS, null)

        return when (prefUnits) {
            "0" -> UnitsLocale.getDefault()
            "1" -> UnitsLocale.METRIC
            "2" -> UnitsLocale.IMPERIAL
            else -> UnitsLocale.getDefault()
        }
    }

    override fun getSelectedPlaceId(): Int {
        // TODO: stub, implement when ready
        return 0
    }

    override fun getSelectedFilter(): FeedFilter {
        // TODO: stub, not implemented
        return FeedFilter()
    }
}