package me.alex.pet.apps.epicenter.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.common.functionaltypes.Either
import me.alex.pet.apps.epicenter.domain.model.Coordinates
import me.alex.pet.apps.epicenter.domain.model.failures.Failure
import me.alex.pet.apps.epicenter.domain.repos.LocationRepository

class LoadLocationNameInteractor(private val locationRepository: LocationRepository) {

    suspend operator fun invoke(coordinates: Coordinates): Either<String, Failure> = withContext(Dispatchers.IO) {
        locationRepository.getLocationName(coordinates)
    }
}