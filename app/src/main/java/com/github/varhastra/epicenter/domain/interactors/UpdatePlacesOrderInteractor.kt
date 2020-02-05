package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.DB
import com.github.varhastra.epicenter.domain.repos.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdatePlacesOrderInteractor(private val placesRepository: PlacesRepository) {

    suspend operator fun invoke(ids: List<Int>) = withContext(Dispatchers.DB) {
        placesRepository.updateOrderById(ids)
    }
}