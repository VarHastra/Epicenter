package com.github.varhastra.epicenter.presentation.common

import java.util.*

class UnitsLocale private constructor(val desc: String) {

    companion object {
        val METRIC = UnitsLocale("metric")
        val IMPERIAL = UnitsLocale("imperial")

        fun getDefault() = getFor(Locale.getDefault())

        fun getFor(locale: Locale): UnitsLocale {
            val countryCode = locale.country
            return when (countryCode) {
                in "US", "LR", "MM" -> IMPERIAL
                else -> METRIC
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnitsLocale

        if (desc != other.desc) return false

        return true
    }

    override fun hashCode(): Int {
        return desc.hashCode()
    }

    override fun toString(): String {
        return "UnitsLocale(desc='$desc')"
    }
}