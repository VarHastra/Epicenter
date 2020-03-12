package me.alex.pet.apps.epicenter.domain.state

import me.alex.pet.apps.epicenter.domain.model.Coordinates

data class CameraState(
        val zoomLevel: Float = 2.5f,
        val position: Coordinates = Coordinates(0.0, 0.0)
)