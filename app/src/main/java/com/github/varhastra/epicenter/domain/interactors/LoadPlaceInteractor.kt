package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.repos.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadPlaceInteractor(private val placesRepository: PlacesRepository) {

    suspend operator fun invoke(placeId: Int): Either<Place, Throwable> = withContext(Dispatchers.IO) {
        placesRepository.getPlaceSuspending(placeId)
    }
}