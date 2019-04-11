package com.github.varhastra.epicenter.model

import com.github.varhastra.epicenter.utils.latDegToKm
import com.github.varhastra.epicenter.utils.lngDegToMi
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import kotlin.math.abs

/**
 * Represents a filter used to filter events in the feed.
 * Instance of this type created with default values represents "World" filter.
 */
class FeedFilter(
        minMagnitude: Double = -1.0,
        radius: Double? = null,
        val sorting: Sorting = Sorting.DATE
) {

    var minMagnitude = minMagnitude
        private set(value) {
            if (value < 0.0 || value >= 10.0) {
                throw IllegalArgumentException("Min magnitude should be in [0.0, 10.0]")
            }
            field = value
        }

    var radius = radius
        private set(value) {
            if (value != null && value <= 0.0) {
                throw IllegalArgumentException("Radius should be greater than 0")
            }
            field = value
        }


    fun applyTo(events: List<Event>, center: Coordinates): List<Event> {
        val result = mutableListOf<Event>()
        for (event in events) {
            if (filter(event, center)) {
                result.add(event)
            } else {
                AnkoLogger(this.javaClass).info("$event")
            }
        }

        when (sorting) {
            Sorting.MAGNITUDE -> result.sortBy { it.magnitude }
            Sorting.DATE -> result.sortBy { it.timestamp }
        }

        return result
    }

    private fun filter(event: Event, center: Coordinates): Boolean {
        return with(event) {
            magnitude >= minMagnitude &&
                    filterCoordinates(center, event.coordinates)
        }
    }

    private fun filterCoordinates(center: Coordinates, point: Coordinates): Boolean {
        return if (radius == null) {
            true
        } else {
            val kLat = latDegToKm(1.0)
            val kLng = lngDegToMi(1.0, center.latitude)

            val y = abs(center.latitude - point.latitude) * kLat
            val x = abs(center.longitude - point.longitude) * kLng

            x * x + y * y <= radius!! * radius!!
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FeedFilter

        if (sorting != other.sorting) return false
        if (minMagnitude != other.minMagnitude) return false
        if (radius != other.radius) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sorting.hashCode()
        result = 31 * result + minMagnitude.hashCode()
        result = 31 * result + radius.hashCode()
        return result
    }

    override fun toString(): String {
        return "FeedFilter(sorting=$sorting, minMagnitude=$minMagnitude, radius=$radius)"
    }

    enum class Sorting {
        MAGNITUDE(),
        DATE(),
//        NEAREST()
    }
}