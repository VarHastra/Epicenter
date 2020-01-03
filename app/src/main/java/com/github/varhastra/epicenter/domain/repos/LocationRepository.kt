package com.github.varhastra.epicenter.domain.repos

import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Position

interface LocationRepository {

    fun getLastLocation(callback: RepositoryCallback<Position>)

    fun getLocationName(coordinates: Coordinates, callback: RepositoryCallback<String>)

    fun isGeoCodingAvailable(): Boolean
}