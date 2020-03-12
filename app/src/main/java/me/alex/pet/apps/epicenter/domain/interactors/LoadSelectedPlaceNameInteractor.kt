package me.alex.pet.apps.epicenter.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.common.functionaltypes.Either
import me.alex.pet.apps.epicenter.domain.model.Place
import me.alex.pet.apps.epicenter.domain.model.PlaceName
import me.alex.pet.apps.epicenter.domain.model.failures.Failure
import me.alex.pet.apps.epicenter.domain.repos.PlacesRepository
import me.alex.pet.apps.epicenter.domain.state.FeedStateDataSource

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