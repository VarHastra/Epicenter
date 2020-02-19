package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.DB
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.repos.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdatePlaceInteractor(private val placesRepository: PlacesRepository) {

    suspend operator fun invoke(id: Int, areaCenter: Coordinates, areaRadiusKm: Double) = withContext(Dispatchers.DB) {
        placesRepository.update(id, areaCenter, areaRadiusKm)
    }

    suspend operator fun invoke(id: Int, name: String) = withContext(Dispatchers.DB) {
        placesRepository.update(id, name)
    }

    suspend operator fun invoke(id: Int, name: String, areaCenter: Coordinates, areaRadiusKm: Double) {
        withContext(Dispatchers.DB) {
            placesRepository.update(id, name, areaCenter, areaRadiusKm)
        }
    }
}