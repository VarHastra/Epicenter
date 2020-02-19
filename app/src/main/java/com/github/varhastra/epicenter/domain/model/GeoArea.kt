package com.github.varhastra.epicenter.domain.model

import kotlin.math.abs

class GeoArea(
        val center: Coordinates,
        radiusKm: Double
) {

    val radiusKm: Double

    val latitude get() = center.latitude

    val longitude get() = center.longitude

    init {
        if (radiusKm < 0) {
            throw IllegalArgumentException("Negative radius values are not allowed. The value was $radiusKm.")
        }
        this.radiusKm = radiusKm
    }

    operator fun contains(point: Coordinates): Boolean {
        val kLat = latDegToKm(1.0)
        val kLng = lngDegToKm(1.0, latitude)

        val y = abs(latitude - point.latitude) * kLat
        val x = abs(longitude - point.longitude) * kLng

        return x * x + y * y <= radiusKm * radiusKm
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeoArea

        if (center != other.center) return false
        if (radiusKm != other.radiusKm) return false

        return true
    }

    override fun hashCode(): Int {
        var result = center.hashCode()
        result = 31 * result + radiusKm.hashCode()
        return result
    }

    override fun toString(): String {
        return "GeoArea(center=$center, radiusKm=$radiusKm)"
    }
}