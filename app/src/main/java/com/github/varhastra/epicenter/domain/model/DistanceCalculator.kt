package com.github.varhastra.epicenter.domain.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


private const val EARTH_RADIUS_KM = 6371


/**
 * Calculates approximate distance in kilometers between two points on the
 * Earth's surface using Haversine formula.
 */
fun haversineDistance(point1: Coordinates, point2: Coordinates): Double {
    val dLat = toRadians(point2.latitude - point1.latitude)
    val dLon = toRadians(point2.longitude - point1.longitude)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(toRadians(point1.latitude)) * cos(toRadians(point2.latitude)) *
            sin(dLon / 2) * sin(dLon / 2)

    return 2 * atan2(sqrt(a), sqrt(1 - a)) * EARTH_RADIUS_KM
}