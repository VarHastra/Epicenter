package me.alex.pet.apps.epicenter.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.DB
import me.alex.pet.apps.epicenter.domain.model.Coordinates
import me.alex.pet.apps.epicenter.domain.repos.PlacesRepository

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