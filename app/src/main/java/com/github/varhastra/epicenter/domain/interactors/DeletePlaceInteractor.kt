package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.domain.repos.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeletePlaceInteractor(private val placesRepository: PlacesRepository) {

    suspend operator fun invoke(id: Int) = withContext(Dispatchers.IO) {
        placesRepository.deleteById(id)
    }
}