package me.alex.pet.apps.epicenter.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.DB
import me.alex.pet.apps.epicenter.common.functionaltypes.Either
import me.alex.pet.apps.epicenter.domain.model.Place
import me.alex.pet.apps.epicenter.domain.model.failures.Failure
import me.alex.pet.apps.epicenter.domain.repos.PlacesRepository

class LoadPlaceInteractor(private val placesRepository: PlacesRepository) {

    suspend operator fun invoke(placeId: Int): Either<Place, Failure> = withContext(Dispatchers.DB) {
        placesRepository.get(placeId)
    }
}