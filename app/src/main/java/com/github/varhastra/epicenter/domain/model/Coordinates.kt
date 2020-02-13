package com.github.varhastra.epicenter.domain.model

import java.io.Serializable

data class Coordinates(
    val latitude: Double,
    val longitude: Double
) : Serializable {
    fun distanceTo(other: Coordinates) = haversineDistance(this, other)
}