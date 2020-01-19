package com.github.varhastra.epicenter.domain.repos

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Place

interface PlacesRepository {

    fun getPlaces(callback: RepositoryCallback<List<Place>>)

    fun getPlace(callback: RepositoryCallback<Place>, placeId: Int)

    fun savePlace(place: Place)

    fun deletePlace(place: Place)

    fun updateOrder(places: List<Place>)

    suspend fun getPlacesSuspending(): Either<List<Place>, Throwable>

    suspend fun getPlaceSuspending(placeId: Int): Either<Place, Throwable>

    suspend fun savePlaceSuspending(place: Place)

    suspend fun deletePlaceSuspending(place: Place)

    suspend fun updateOrderSuspending(places: List<Place>)
}