package me.alex.pet.apps.epicenter.domain.model

data class Position(
        val coordinates: Coordinates,
        val accuracy: Double,
        val timeMillis: Long
)