package me.alex.pet.apps.epicenter.domain.model

import org.threeten.bp.Instant

class RemoteEvent(
        id: String,
        position: Position,
        magnitude: Magnitude,
        timestamp: Instant,
        link: String,
        feltReportsCount: Int,
        tsunamiAlert: Boolean,
        val distanceToUser: Double?
) : Event(id, position, magnitude, timestamp, link, feltReportsCount, tsunamiAlert) {

    override fun toString(): String {
        return "RemoteEvent(id='$id', position=$position, magnitude=$magnitude, timestamp=$timestamp)"
    }


    companion object {
        fun of(event: Event, point: Coordinates? = null): RemoteEvent {
            return with(event) {
                RemoteEvent(
                        id,
                        position,
                        magnitude,
                        timestamp,
                        sourceUrl,
                        feltReportsCount,
                        tsunamiAlert,
                        point?.distanceTo(position.coordinates)
                )
            }
        }
    }
}