package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.DB
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.repos.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InsertPlaceInteractor(private val placesRepository: PlacesRepository) {

    suspend operator fun invoke(name: String, areaCenter: Coordinates, areaRadiusKm: Double) {
        withContext(Dispatchers.DB) {
            placesRepository.insert(name, areaCenter, areaRadiusKm)
        }
    }
}