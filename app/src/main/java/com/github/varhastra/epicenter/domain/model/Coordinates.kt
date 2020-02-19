package com.github.varhastra.epicenter.domain.model

import java.io.Serializable

class Coordinates(
        latitude: Double,
        longitude: Double
) : Serializable {

    val latitude = clamp(-90.0, latitude, 90.0)

    val longitude = clamp(-180.0, longitude, 180.0)

    fun distanceTo(other: Coordinates) = haversineDistance(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Coordinates

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        return result
    }

    override fun toString(): String {
        return "Coordinates(latitude=$latitude, longitude=$longitude)"
    }

    operator fun component1() = latitude

    operator fun component2() = longitude
}

private fun clamp(min: Double, value: Double, max: Double) = when {
    value < min -> min
    value > max -> max
    else -> value
}