package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.DB
import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.repos.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadPlacesInteractor(private val placesRepository: PlacesRepository) {

    suspend operator fun invoke(): Either<List<Place>, Throwable> = withContext(Dispatchers.DB) {
        placesRepository.getAll()
    }
}