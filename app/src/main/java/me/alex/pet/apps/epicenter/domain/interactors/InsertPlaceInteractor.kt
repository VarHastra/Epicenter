package me.alex.pet.apps.epicenter.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.DB
import me.alex.pet.apps.epicenter.domain.model.Coordinates
import me.alex.pet.apps.epicenter.domain.repos.PlacesRepository

class InsertPlaceInteractor(private val placesRepository: PlacesRepository) {

    suspend operator fun invoke(name: String, areaCenter: Coordinates, areaRadiusKm: Double) {
        withContext(Dispatchers.DB) {
            placesRepository.insert(name, areaCenter, areaRadiusKm)
        }
    }
}