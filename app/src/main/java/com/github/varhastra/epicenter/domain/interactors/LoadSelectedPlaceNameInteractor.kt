package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.PlaceName
import com.github.varhastra.epicenter.domain.model.failures.Failure
import com.github.varhastra.epicenter.domain.repos.PlacesRepository
import com.github.varhastra.epicenter.domain.state.FeedStateDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadSelectedPlaceNameInteractor(
        private val feedStateDataSource: FeedStateDataSource,
        private val placesRepository: PlacesRepository
) {

    suspend operator fun invoke(): PlaceName = withContext(Dispatchers.IO) {
        val previouslySelectedId = feedStateDataSource.selectedPlaceId
        placesRepository.getPlaceName(previouslySelectedId).fold(
                { onResult(it) },
                { onFailure(it) }
        )
    }

    fun onResult(place: PlaceName) = place

    suspend fun onFailure(t: Failure) = (placesRepository.getPlaceName(Place.WORLD.id) as Either.Success).data
}