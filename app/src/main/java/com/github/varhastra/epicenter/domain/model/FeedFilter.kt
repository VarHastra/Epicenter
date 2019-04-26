package com.github.varhastra.epicenter.domain.model

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


    fun applyTo(events: List<RemoteEvent>): List<RemoteEvent> {
        val result = events.filter { filter(it) }

        return sort(result)
    }

    private fun filter(remoteEvent: RemoteEvent): Boolean {
        return with(remoteEvent) {
            event.magnitude >= minMagnitude
        }
    }

    private fun sort(events: List<RemoteEvent>): List<RemoteEvent> {
        return when (sorting) {
            Sorting.MAGNITUDE_ASC -> events.sortedBy { it.event.magnitude }
            Sorting.MAGNITUDE_DESC -> events.sortedByDescending { it.event.magnitude }
            Sorting.DISTANCE -> events.sortedBy { it.distance }
            Sorting.DATE -> events.sortedByDescending { it.event.timestamp }
            else -> events.sortedByDescending { it.event.timestamp }
        }
    }

    fun copy(minMagnitude: Double = this.minMagnitude, sorting: Sorting = this.sorting) =
            FeedFilter(minMagnitude, sorting)

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

    enum class Sorting(val id: Int) {
        DATE(0),
        MAGNITUDE_ASC(1),
        MAGNITUDE_DESC(2),
        DISTANCE(3);

        companion object {
            fun fromId(id: Int) = Sorting.values().first { it.id == id }
        }
    }
}