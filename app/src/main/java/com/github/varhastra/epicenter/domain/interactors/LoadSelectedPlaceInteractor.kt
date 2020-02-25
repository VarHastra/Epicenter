package com.github.varhastra.epicenter.domain.interactors

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.repos.PlacesRepository
import com.github.varhastra.epicenter.domain.state.FeedStateDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadSelectedPlaceInteractor(
        private val feedStateDataSource: FeedStateDataSource,
        private val placesRepository: PlacesRepository
) {

    suspend operator fun invoke(): Place = withContext(Dispatchers.IO) {
        val previouslySelectedId = feedStateDataSource.selectedPlaceId
        placesRepository.get(previouslySelectedId).fold(
                { onResult(it) },
                { onFailure(it) }
        )
    }

    fun onResult(place: Place) = place

    suspend fun onFailure(t: Throwable) = (placesRepository.get(Place.WORLD.id) as Either.Success).data
}