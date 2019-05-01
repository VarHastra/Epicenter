package com.github.varhastra.epicenter.ui.main.map

import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import org.threeten.bp.Instant

class EventMarker(
        val eventId: String,
        val coordinates: Coordinates,
        val title: String,
        val instant: Instant,
        val magnitude: Double,
        val alertLevel: AlertLevel
) {
    enum class AlertLevel {
        ALERT_0,
        ALERT_2,
        ALERT_4,
        ALERT_6,
        ALERT_8;

        companion object {
            fun fromMagnitudeValue(magnitude: Int): AlertLevel {
                return when (magnitude) {
                    in -2 until 2 -> ALERT_0
                    in 2 until 4 -> ALERT_2
                    in 4 until 6 -> ALERT_4
                    in 6 until 8 -> ALERT_6
                    in 8..10 -> ALERT_8
                    else -> ALERT_0
                }
            }
        }
    }


    override fun toString(): String {
        return "EventMarker(eventId='$eventId', coordinates=$coordinates, title='$title', instant=$instant, magnitude=$magnitude, alertLevel=$alertLevel)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventMarker

        if (eventId != other.eventId) return false

        return true
    }

    override fun hashCode(): Int {
        return eventId.hashCode()
    }


    companion object {
        fun fromRemoteEvent(remoteEvent: RemoteEvent): EventMarker {
            return with(remoteEvent) {
                EventMarker(event.id, event.coordinates, event.placeName, event.timestamp, event.magnitude, AlertLevel.fromMagnitudeValue(event.magnitude.toInt()))
            }
        }
    }
}