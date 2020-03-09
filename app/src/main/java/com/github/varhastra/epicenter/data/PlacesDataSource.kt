package com.github.varhastra.epicenter.data

import android.content.Context
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.data.db.PlaceDao
import com.github.varhastra.epicenter.data.db.PlaceEntity
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.GeoArea
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.PlaceName
import com.github.varhastra.epicenter.domain.model.failures.Failure
import com.github.varhastra.epicenter.domain.repos.LocationRepository
import com.github.varhastra.epicenter.domain.repos.PlacesRepository

class PlacesDataSource(
        private val locationRepository: LocationRepository,
        private val placeDao: PlaceDao,
        context: Context
) : PlacesRepository {

    private val context = context.applicationContext

    private val defaultPlaces = listOf(Place.CURRENT_LOCATION, Place.WORLD).map { substituteWithLocalizedName(it) }


    override suspend fun insert(name: String, areaCenter: Coordinates, areaRadiusKm: Double) {
        placeDao.insert(
                PlaceEntity(
                        name = name,
                        latitude = areaCenter.latitude,
                        longitude = areaCenter.longitude,
                        radiusKm = areaRadiusKm
                )
        )
    }

    override suspend fun getAll(): Either<List<Place>, Failure> {
        val places = defaultPlaces + placeDao.getAll().map { it.toPlace() }
        return Either.Success(places)
    }

    override suspend fun getAllPlaceNames(): List<PlaceName> {
        return defaultPlaces.map { it.toPlaceName() } + placeDao.getAll().map { it.toPlaceName() }
    }

    override suspend fun get(placeId: Int): Either<Place, Failure> {
        val place = getFromDefaultsOrDb(placeId)
                ?: return Either.Failure(Failure.PlacesFailure.NoSuchPlace(placeId))
        return if (placeId == Place.CURRENT_LOCATION.id) {
            locationRepository.getCoordinates().map { lastCoordinates ->
                place.copy(geoArea = GeoArea(lastCoordinates, place.radiusKm))
            }
        } else {
            Either.success(place)
        }
    }

    override suspend fun getPlaceName(placeId: Int): Either<PlaceName, Failure> {
        val place = getFromDefaultsOrDb(placeId)
                ?: return Either.Failure(Failure.PlacesFailure.NoSuchPlace(placeId))
        return Either.success(place.toPlaceName())
    }

    private fun getFromDefaultsOrDb(placeId: Int): Place? {
        return when (placeId) {
            Place.CURRENT_LOCATION.id -> Place.CURRENT_LOCATION
            Place.WORLD.id -> Place.WORLD
            else -> placeDao.get(placeId)?.toPlace()
        }
    }

    private fun substituteWithLocalizedName(place: Place): Place {
        return when (place.id) {
            Place.CURRENT_LOCATION.id -> place.copy(name = context.getString(R.string.app_place_current_location))
            Place.WORLD.id -> place.copy(name = context.getString(R.string.app_place_world))
            else -> place
        }
    }

    override suspend fun update(id: Int, areaCenter: Coordinates, areaRadiusKm: Double) {
        placeDao.update(id, areaCenter.latitude, areaCenter.longitude, areaRadiusKm)
    }

    override suspend fun update(id: Int, name: String) {
        placeDao.update(id, name)
    }

    override suspend fun update(id: Int, name: String, areaCenter: Coordinates, areaRadiusKm: Double) {
        placeDao.update(id, name, areaCenter.latitude, areaCenter.longitude, areaRadiusKm)
    }

    override suspend fun updateOrderById(ids: List<Int>) {
        ids.forEachIndexed { index, id ->
            if (id != Place.WORLD.id && id != Place.CURRENT_LOCATION.id) {
                placeDao.updateOrder(id, index)
            }
        }
    }

    override suspend fun deleteById(id: Int) {
        placeDao.deleteById(id)
    }
}


private fun PlaceEntity.toPlace() = Place(
        id,
        name,
        GeoArea(
                Coordinates(latitude, longitude),
                radiusKm
        )
)

private fun PlaceEntity.toPlaceName() = PlaceName(id, name)

private fun Place.toPlaceEntity() = PlaceEntity(
        id,
        name,
        latitude,
        longitude,
        radiusKm
)

private fun Place.toPlaceName() = PlaceName(id, name)