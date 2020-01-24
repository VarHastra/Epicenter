package com.github.varhastra.epicenter.domain.state

import com.github.varhastra.epicenter.domain.model.Coordinates

data class CameraState(
        val zoomLevel: Float = 2.5f,
        val position: Coordinates = Coordinates(0.0, 0.0)
)