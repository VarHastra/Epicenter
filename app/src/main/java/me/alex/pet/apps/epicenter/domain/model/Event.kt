package me.alex.pet.apps.epicenter.domain.model

import me.alex.pet.apps.epicenter.common.extensions.toLocalDateTime
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime

open class Event(
        val id: String,
        val magnitude: Double,
        val placeName: String,
        val timestamp: Instant,
        val coordinates: Coordinates,
        val link: String,
        val feltReportsCount: Int,
        val tsunamiAlert: Boolean,
        val magnitudeType: String,
        val depth: Double
) {

    val latitude: Double
        get() {
            return coordinates.latitude
        }

    val longitude: Double
        get() {
            return coordinates.longitude
        }

    val localDatetime: LocalDateTime
        get() {
            return timestamp.toLocalDateTime()
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        if (id != other.id) return false
        if (magnitude != other.magnitude) return false
        if (placeName != other.placeName) return false
        if (timestamp != other.timestamp) return false
        if (coordinates != other.coordinates) return false
        if (link != other.link) return false
        if (feltReportsCount != other.feltReportsCount) return false
        if (tsunamiAlert != other.tsunamiAlert) return false
        if (magnitudeType != other.magnitudeType) return false
        if (depth != other.depth) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + magnitude.hashCode()
        result = 31 * result + placeName.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + coordinates.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + feltReportsCount
        result = 31 * result + tsunamiAlert.hashCode()
        result = 31 * result + magnitudeType.hashCode()
        result = 31 * result + depth.hashCode()
        return result
    }

    override fun toString(): String {
        return "Event(id='$id', magnitude=$magnitude, placeName='$placeName', timestamp=$timestamp, coordinates=$coordinates, depth=$depth)"
    }
}