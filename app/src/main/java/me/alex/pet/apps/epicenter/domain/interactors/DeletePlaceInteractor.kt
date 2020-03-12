package me.alex.pet.apps.epicenter.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.DB
import me.alex.pet.apps.epicenter.domain.repos.PlacesRepository

class DeletePlaceInteractor(private val placesRepository: PlacesRepository) {

    suspend operator fun invoke(id: Int) = withContext(Dispatchers.DB) {
        placesRepository.deleteById(id)
    }
}