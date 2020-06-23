package me.alex.pet.apps.epicenter.domain.model

import org.threeten.bp.Instant

open class Event(
        val id: String,
        val position: Position,
        val magnitude: Magnitude,
        val timestamp: Instant,
        val sourceUrl: String,
        val feltReportsCount: Int,
        val tsunamiAlert: Boolean
) {

    val coordinates: Coordinates get() = position.coordinates

    val latitude: Double get() = coordinates.latitude

    val longitude: Double get() = coordinates.longitude

    val depth: Double get() = position.depth


    init {
        if (feltReportsCount < 0) {
            throw IllegalArgumentException("feltReportsCount must be >= 0 but was $feltReportsCount.")
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        if (id != other.id) return false
        if (position != other.position) return false
        if (magnitude != other.magnitude) return false
        if (timestamp != other.timestamp) return false
        if (sourceUrl != other.sourceUrl) return false
        if (feltReportsCount != other.feltReportsCount) return false
        if (tsunamiAlert != other.tsunamiAlert) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + position.hashCode()
        result = 31 * result + magnitude.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + sourceUrl.hashCode()
        result = 31 * result + feltReportsCount
        result = 31 * result + tsunamiAlert.hashCode()
        return result
    }

    override fun toString(): String {
        return "Event(id='$id', position=$position, magnitude=$magnitude, timestamp=$timestamp)"
    }
}