package com.github.varhastra.epicenter.utils

import com.github.varhastra.epicenter.domain.model.Coordinates
import kotlin.math.atan2
import kotlin.math.sin


private const val EARTH_RADIUS_KM = 6371


/**
 * Calculates approximate distance in kilometers between two points on the
 * Earth's surface using Haversine formula.
 */
fun haversineDistance(point1: Coordinates, point2: Coordinates): Double {
    val dLat = degToRad(point2.latitude - point1.latitude)
    val dLon = degToRad(point2.longitude - point1.longitude)
    val a =
        sin(dLat / 2) * sin(dLat / 2) +
                Math.cos(degToRad(point1.latitude)) * Math.cos(degToRad(point2.latitude)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

    return 2 * atan2(Math.sqrt(a), Math.sqrt(1 - a)) * EARTH_RADIUS_KM
}