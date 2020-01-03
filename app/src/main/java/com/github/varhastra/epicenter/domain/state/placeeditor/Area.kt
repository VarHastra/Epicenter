package com.github.varhastra.epicenter.domain.state.placeeditor

import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.kmToM

data class Area(
        val center: Coordinates,
        val radiusKm: Double
) {
    val radiusM: Double
        get() = kmToM(radiusKm)

    init {
        if (radiusKm < MIN_RADIUS_KM || radiusKm > MAX_RADIUS_KM) {
            throw IllegalArgumentException()
        }
    }

    companion object {
        const val MIN_RADIUS_KM = 500.0
        const val MAX_RADIUS_KM = 5_000.0
    }
}