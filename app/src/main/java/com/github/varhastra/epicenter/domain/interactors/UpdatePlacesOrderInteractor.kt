package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.repos.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdatePlacesOrderInteractor(private val placesRepository: PlacesRepository) {

    suspend operator fun invoke(places: List<Place>) = withContext(Dispatchers.IO) {
        placesRepository.updateOrderSuspending(places)
    }
}