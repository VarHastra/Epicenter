package me.alex.pet.apps.epicenter.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.DB
import me.alex.pet.apps.epicenter.domain.model.PlaceName
import me.alex.pet.apps.epicenter.domain.repos.PlacesRepository

class LoadPlaceNamesInteractor(private val placesRepository: PlacesRepository) {

    suspend operator fun invoke(): List<PlaceName> = withContext(Dispatchers.DB) {
        placesRepository.getAllPlaceNames()
    }
}