package com.github.varhastra.epicenter.domain.model

data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    fun distanceTo(other: Coordinates) = haversineDistance(this, other)
}