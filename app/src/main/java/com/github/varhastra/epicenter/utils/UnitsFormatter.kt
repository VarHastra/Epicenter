package com.github.varhastra.epicenter.utils

import android.content.Context
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.domain.model.kmToMi
import java.text.DecimalFormat
import kotlin.math.roundToInt

class UnitsFormatter(private val unitsLocale: UnitsLocale, decimalPlaces: Int = 1, val context: Context = App.instance) {

    private val decimalFormat = getDecimalFormat(decimalPlaces)

    init {
        if (decimalPlaces < 0 || decimalPlaces > 7) {
            throw IllegalArgumentException()
        }
    }

    fun getLocalizedDistance(distanceKm: Double): Double {
        return when (unitsLocale) {
            UnitsLocale.METRIC -> distanceKm
            UnitsLocale.IMPERIAL -> kmToMi(distanceKm)
            else -> distanceKm
        }
    }

    fun getLocalizedDistance(distanceKm: Int): Int {
        return when (unitsLocale) {
            UnitsLocale.METRIC -> distanceKm
            UnitsLocale.IMPERIAL -> kmToMi(distanceKm.toDouble()).roundToInt()
            else -> distanceKm
        }
    }

    fun getLocalizedDistanceString(distanceKm: Double?, alternativeStrId: Int = R.string.app_not_available_abbreviation): String {
        if (distanceKm == null) {
            return context.getString(alternativeStrId)
        }
        val localizedDistance = getLocalizedDistance(distanceKm)
        val distanceStr = decimalFormat.format(localizedDistance)
        return when (unitsLocale) {
            UnitsLocale.METRIC -> context.getString(R.string.app_format_kilometers_str, distanceStr)
            UnitsLocale.IMPERIAL -> context.getString(R.string.app_format_miles_str, distanceStr)
            else -> context.getString(R.string.app_format_kilometers_str, distanceStr)
        }
    }

    fun getLocalizedDistanceString(distanceKm: Int?, alternativeStrId: Int = R.string.app_not_available_abbreviation): String {
        if (distanceKm == null) {
            return context.getString(alternativeStrId)
        }
        val localizedDistance = getLocalizedDistance(distanceKm)
        val distanceStr = localizedDistance.toString()
        return when (unitsLocale) {
            UnitsLocale.METRIC -> context.getString(R.string.app_format_kilometers_str, distanceStr)
            UnitsLocale.IMPERIAL -> context.getString(R.string.app_format_miles_str, distanceStr)
            else -> context.getString(R.string.app_format_kilometers_str, distanceStr)
        }
    }

    private fun getDecimalFormat(decimalPlaces: Int): DecimalFormat {
        val digitPattern = "#"
        return if (decimalPlaces == 0) {
            DecimalFormat(digitPattern)
        } else {
            val pattern = StringBuilder(digitPattern).append(".").append(digitPattern.repeat(decimalPlaces)).toString()
            DecimalFormat(pattern)
        }
    }
}