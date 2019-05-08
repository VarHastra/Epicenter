package com.github.varhastra.epicenter.domain

import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Position

interface LocationDataSource {

    fun getLastLocation(callback: DataSourceCallback<Position>)

    fun getLocationName(coordinates: Coordinates, callback: DataSourceCallback<String>)

    fun isGeoCodingAvailable(): Boolean
}