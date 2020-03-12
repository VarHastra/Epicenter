package me.alex.pet.apps.epicenter.domain.repos

import me.alex.pet.apps.epicenter.common.functionaltypes.Either
import me.alex.pet.apps.epicenter.domain.model.Coordinates
import me.alex.pet.apps.epicenter.domain.model.failures.Failure

interface LocationRepository {

    suspend fun getCoordinates(): Either<Coordinates, Failure>

    suspend fun getLastCoordinates(): Either<Coordinates, Failure>

    suspend fun getLocationName(coordinates: Coordinates): Either<String, Failure>

    fun isGeoCodingAvailable(): Boolean

    val isLocationPermissionGranted: Boolean
}