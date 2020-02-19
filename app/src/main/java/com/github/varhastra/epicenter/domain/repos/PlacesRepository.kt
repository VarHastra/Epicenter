package com.github.varhastra.epicenter.domain.repos

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Place

interface PlacesRepository {

    fun getPlaces(callback: RepositoryCallback<List<Place>>)

    fun getPlace(callback: RepositoryCallback<Place>, placeId: Int)

    fun savePlace(place: Place)

    fun deletePlace(place: Place)

    fun deleteById(id: Int)

    fun updateOrder(places: List<Place>)

    suspend fun getPlacesSuspending(): Either<List<Place>, Throwable>

    suspend fun getPlaceSuspending(placeId: Int): Either<Place, Throwable>

    suspend fun savePlaceSuspending(place: Place)

    suspend fun insert(name: String, areaCenter: Coordinates, areaRadiusKm: Double)

    suspend fun deletePlaceSuspending(place: Place)

    suspend fun updateOrderSuspending(places: List<Place>)

    suspend fun update(id: Int, areaCenter: Coordinates, areaRadiusKm: Double)

    suspend fun update(id: Int, name: String)

    suspend fun update(id: Int, name: String, areaCenter: Coordinates, areaRadiusKm: Double)

    suspend fun updateOrderById(ids: List<Int>)
}