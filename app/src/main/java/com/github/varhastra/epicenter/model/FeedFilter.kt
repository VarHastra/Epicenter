package com.github.varhastra.epicenter.model

/**
 * Represents a filter used to filter events in the feed.
 * Instance of this type created with default values represents "World" filter.
 */
class FeedFilter(
    minMagnitude: Double = -1.0,
    val sorting: Sorting = Sorting.DATE
) {

    var minMagnitude = minMagnitude
        private set(value) {
            if (value < 0.0 || value >= 10.0) {
                throw IllegalArgumentException("Min magnitude should be in [0.0, 10.0]")
            }
            field = value
        }


    fun applyTo(events: List<Event>): List<Event> {
        val result = events.filter { filter(it) }

        return sort(result)
    }

    private fun filter(event: Event): Boolean {
        return with(event) {
            magnitude >= minMagnitude
        }
    }

    private fun sort(events: List<Event>): List<Event> {
        return when (sorting) {
            Sorting.MAGNITUDE -> events.sortedBy { it.magnitude }
            Sorting.DATE -> events.sortedByDescending { it.timestamp }
            else -> events.sortedByDescending { it.timestamp }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FeedFilter

        if (sorting != other.sorting) return false
        if (minMagnitude != other.minMagnitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sorting.hashCode()
        result = 31 * result + minMagnitude.hashCode()
        return result
    }

    override fun toString(): String {
        return "FeedFilter(sorting=$sorting, minMagnitude=$minMagnitude)"
    }

    enum class Sorting {
        MAGNITUDE(),
        DATE(),
//        NEAREST()
    }
}