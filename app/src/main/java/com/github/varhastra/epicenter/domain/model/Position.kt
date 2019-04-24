package com.github.varhastra.epicenter.domain.model

data class Position(
        val coordinates: Coordinates,
        val accuracy: Double,
        val timeMillis: Long
)