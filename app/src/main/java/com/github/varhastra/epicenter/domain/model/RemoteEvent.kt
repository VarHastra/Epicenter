package com.github.varhastra.epicenter.domain.model

data class RemoteEvent(val event: Event, val distance: Double? = null) {

    companion object {
        fun of(event: Event, point: Coordinates? = null): RemoteEvent {
            return RemoteEvent(event, point?.distanceTo(event.coordinates))
        }
    }
}