package me.alex.pet.apps.epicenter.domain.repos

import kotlinx.coroutines.flow.Flow
import me.alex.pet.apps.epicenter.common.functionaltypes.Either
import me.alex.pet.apps.epicenter.domain.model.Coordinates
import me.alex.pet.apps.epicenter.domain.model.Place
import me.alex.pet.apps.epicenter.domain.model.PlaceName
import me.alex.pet.apps.epicenter.domain.model.failures.Failure

interface PlacesRepository {

    suspend fun insert(name: String, areaCenter: Coordinates, areaRadiusKm: Double)

    suspend fun getAll(): Either<List<Place>, Failure>

    fun observeAllPlaceNames(): Flow<List<PlaceName>>

    suspend fun getAllPlaceNames(): List<PlaceName>

    suspend fun get(placeId: Int): Either<Place, Failure>

    suspend fun getPlaceName(placeId: Int): Either<PlaceName, Failure>

    suspend fun update(id: Int, areaCenter: Coordinates, areaRadiusKm: Double)

    suspend fun update(id: Int, name: String)

    suspend fun update(id: Int, name: String, areaCenter: Coordinates, areaRadiusKm: Double)

    suspend fun updateOrderById(ids: List<Int>)

    suspend fun deleteById(id: Int)
}