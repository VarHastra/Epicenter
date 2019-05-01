package com.github.varhastra.epicenter.domain.model

import java.lang.IllegalArgumentException
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit

class MapFilter(val minMagnitude: Double = -1.0, val periodDays: Int = 1) {

    init {
        if (periodDays < 1 || periodDays > 7) {
            throw IllegalArgumentException("periodDays should be in [1..7]")
        }
    }


    fun filter(remoteEvent: RemoteEvent): Boolean {
        val now = Instant.now()
        return with(remoteEvent) {
            event.magnitude >= minMagnitude && ChronoUnit.DAYS.between(event.timestamp, now) <= periodDays
        }
    }

    fun copy(minMagnitude: Double = this.minMagnitude, periodDays: Int = this.periodDays) = MapFilter(minMagnitude, periodDays)

    override fun toString(): String {
        return "MapFilter(minMagnitude=$minMagnitude, periodDays=$periodDays)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapFilter

        return (minMagnitude == other.minMagnitude
                && periodDays == other.periodDays)
    }

    override fun hashCode(): Int {
        var result = minMagnitude.hashCode()
        result = 31 * result + periodDays
        return result
    }
}