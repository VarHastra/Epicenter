package com.github.varhastra.epicenter.domain.repos

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Position

interface LocationRepository {

    fun getLastLocation(callback: RepositoryCallback<Position>)

    suspend fun getCoordinates(): Either<Coordinates, Throwable>

    suspend fun getLastCoordinates(): Either<Coordinates, Throwable>

    fun getLocationName(coordinates: Coordinates, callback: RepositoryCallback<String>)

    suspend fun getLocationName(coordinates: Coordinates): Either<String, Throwable>

    fun isGeoCodingAvailable(): Boolean
}