package com.github.varhastra.epicenter.domain.repos

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Coordinates

interface LocationRepository {

    suspend fun getCoordinates(): Either<Coordinates, Throwable>

    suspend fun getLastCoordinates(): Either<Coordinates, Throwable>

    suspend fun getLocationName(coordinates: Coordinates): Either<String, Throwable>

    fun isGeoCodingAvailable(): Boolean

    val isLocationPermissionGranted: Boolean
}