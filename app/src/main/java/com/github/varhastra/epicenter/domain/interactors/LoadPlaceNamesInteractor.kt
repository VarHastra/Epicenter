package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.DB
import com.github.varhastra.epicenter.domain.model.PlaceName
import com.github.varhastra.epicenter.domain.repos.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadPlaceNamesInteractor(private val placesRepository: PlacesRepository) {

    suspend operator fun invoke(): List<PlaceName> = withContext(Dispatchers.DB) {
        placesRepository.getAllPlaceNames()
    }
}