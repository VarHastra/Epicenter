package me.alex.pet.apps.epicenter.domain.model

data class Position(
        val coordinates: Coordinates,
        val depth: Double,
        val description: String
)