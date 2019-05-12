package com.github.varhastra.epicenter.domain.model

class RemoteEvent(val event: Event, point: Coordinates? = null) {
    var distance: Double? = null
        private set

    init {
        point?.let {
            distance = event.coordinates.distanceTo(it)
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RemoteEvent

        if (event != other.event) return false
        if (distance != other.distance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = event.hashCode()
        result = 31 * result + (distance?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "RemoteEvent(event=$event, distance=$distance)"
    }


    companion object {
        fun from(event: Event, point: Coordinates? = null) = RemoteEvent(event, point)

        fun from(events: List<Event>, point: Coordinates? = null): List<RemoteEvent> = events.map { RemoteEvent(it, point) }
    }
}