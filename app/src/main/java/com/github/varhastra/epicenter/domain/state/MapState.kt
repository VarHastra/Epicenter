package com.github.varhastra.epicenter.domain.state

import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.MapFilter

data class MapState(
        val filter: MapFilter = MapFilter(),
        val zoomLevel: Float = 2.5f,
        val cameraPosition: Coordinates = Coordinates(0.0, 0.0)
)