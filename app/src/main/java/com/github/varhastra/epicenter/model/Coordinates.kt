package com.github.varhastra.epicenter.model

import com.github.varhastra.epicenter.utils.haversineDistance

data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    fun distanceTo(other: Coordinates) = haversineDistance(this, other)
}