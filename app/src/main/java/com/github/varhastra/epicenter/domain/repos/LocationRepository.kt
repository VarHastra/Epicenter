package com.github.varhastra.epicenter.domain.repos

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.failures.Failure

interface LocationRepository {

    suspend fun getCoordinates(): Either<Coordinates, Failure>

    suspend fun getLastCoordinates(): Either<Coordinates, Failure>

    suspend fun getLocationName(coordinates: Coordinates): Either<String, Failure>

    fun isGeoCodingAvailable(): Boolean

    val isLocationPermissionGranted: Boolean
}