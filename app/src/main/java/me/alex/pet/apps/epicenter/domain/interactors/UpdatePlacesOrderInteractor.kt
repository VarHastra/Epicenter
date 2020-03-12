package me.alex.pet.apps.epicenter.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.DB
import me.alex.pet.apps.epicenter.domain.repos.PlacesRepository

class UpdatePlacesOrderInteractor(private val placesRepository: PlacesRepository) {

    suspend operator fun invoke(ids: List<Int>) = withContext(Dispatchers.DB) {
        placesRepository.updateOrderById(ids)
    }
}