package com.github.varhastra.epicenter.domain.repos

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Place

interface PlacesRepository {

    suspend fun insert(name: String, areaCenter: Coordinates, areaRadiusKm: Double)

    suspend fun getAll(): Either<List<Place>, Throwable>

    suspend fun get(placeId: Int): Either<Place, Throwable>

    suspend fun update(id: Int, areaCenter: Coordinates, areaRadiusKm: Double)

    suspend fun update(id: Int, name: String)

    suspend fun update(id: Int, name: String, areaCenter: Coordinates, areaRadiusKm: Double)

    suspend fun updateOrderById(ids: List<Int>)

    suspend fun deleteById(id: Int)
}