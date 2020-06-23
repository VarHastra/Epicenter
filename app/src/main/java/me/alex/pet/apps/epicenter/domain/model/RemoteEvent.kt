package me.alex.pet.apps.epicenter.domain.model

import org.threeten.bp.Instant

class RemoteEvent(
        id: String,
        magnitude: Double,
        placeName: String,
        timestamp: Instant,
        coordinates: Coordinates,
        link: String,
        feltReportsCount: Int,
        tsunamiAlert: Boolean,
        magnitudeType: String,
        depth: Double,
        val distanceToUser: Double?
) : Event(id, magnitude, placeName, timestamp, coordinates, link, feltReportsCount, tsunamiAlert, magnitudeType, depth) {

    override fun toString(): String {
        return "RemoteEvent(id='$id', magnitude=$magnitude, placeName='$placeName', timestamp=$timestamp, coordinates=$coordinates, depth=$depth)"
    }


    companion object {
        fun of(event: Event, point: Coordinates? = null): RemoteEvent {
            return with(event) {
                RemoteEvent(
                        id,
                        magnitude,
                        placeName,
                        timestamp,
                        coordinates,
                        link,
                        feltReportsCount,
                        tsunamiAlert,
                        magnitudeType,
                        depth,
                        point?.distanceTo(coordinates)
                )
            }
        }
    }
}