package me.alex.pet.apps.epicenter.domain.interactors

import kotlinx.coroutines.flow.Flow
import me.alex.pet.apps.epicenter.domain.model.PlaceName
import me.alex.pet.apps.epicenter.domain.repos.PlacesRepository

class ObservePlaceNames(private val placesRepository: PlacesRepository) {

    operator fun invoke(): Flow<List<PlaceName>> {
        return placesRepository.observeAllPlaceNames()
    }
}