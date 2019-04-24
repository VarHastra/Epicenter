package com.github.varhastra.epicenter.domain.model

class RemoteEvent(val event: Event, point: Coordinates? = null) {
    var distance: Double? = null
        private set

    init {
        point?.let {
            distance = event.coordinates.distanceTo(it)
        }
    }

    companion object {
        fun from(event: Event, point: Coordinates? = null) = RemoteEvent(event, point)

        fun from(events: List<Event>, point: Coordinates? = null): List<RemoteEvent> = events.map { RemoteEvent(it, point) }
    }
}