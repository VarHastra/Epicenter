package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.repos.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadLocationNameInteractor(private val locationRepository: LocationRepository) {

    suspend operator fun invoke(coordinates: Coordinates): Either<String, Throwable> = withContext(Dispatchers.IO) {
        locationRepository.getLocationName(coordinates)
    }
}