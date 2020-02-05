package com.github.varhastra.epicenter.data

import android.annotation.SuppressLint
import android.content.Context
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.IO_EXECUTOR
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.data.db.AppDb
import com.github.varhastra.epicenter.data.db.PlaceDao
import com.github.varhastra.epicenter.device.LocationProvider
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.Position
import com.github.varhastra.epicenter.domain.repos.LocationRepository
import com.github.varhastra.epicenter.domain.repos.PlacesRepository
import com.github.varhastra.epicenter.domain.repos.RepositoryCallback
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class PlacesDataSource private constructor(
        private val locationRepository: LocationRepository,
        private val placeDao: PlaceDao,
        context: Context = App.instance) : PlacesRepository {

    private val context = context.applicationContext

    override fun getPlaces(callback: RepositoryCallback<List<Place>>) {
        doAsync(executorService = IO_EXECUTOR) {
            val places = placeDao.getAll().map { substituteWithLocalizedName(it.toPlace()) }.toList()
            uiThread {
                callback.onResult(places)
            }
        }
    }

    override fun getPlace(callback: RepositoryCallback<Place>, placeId: Int) {
        doAsync(executorService = IO_EXECUTOR) {
            val p = placeDao.get(placeId)
            if (p == null) {
                uiThread {
                    callback.onFailure(NoSuchElementException("Place with the given id doesn't exist: $placeId."))
                }
                return@doAsync
            }
            val place = substituteWithLocalizedName(p.toPlace())
            if (placeId == Place.CURRENT_LOCATION.id) {
                locationRepository.getLastLocation(object : RepositoryCallback<Position> {
                    override fun onResult(result: Position) {
                        uiThread {
                            callback.onResult(place.copy(coordinates = result.coordinates))
                        }
                    }

                    override fun onFailure(t: Throwable?) {
                        uiThread {
                            callback.onFailure(t)
                        }
                    }
                })
            } else {
                uiThread {
                    callback.onResult(place)
                }
            }
        }
    }

    private fun substituteWithLocalizedName(place: Place): Place {
        return when (place.id) {
            Place.CURRENT_LOCATION.id -> place.copy(name = context.getString(R.string.app_place_current_location))
            Place.WORLD.id -> place.copy(name = context.getString(R.string.app_place_world))
            else -> place
        }
    }

    override fun savePlace(place: Place) {
        doAsync(executorService = IO_EXECUTOR) {
            placeDao.save(place.toPlaceEntity())
        }
    }

    override fun deletePlace(place: Place) {
        doAsync(executorService = IO_EXECUTOR) {
            placeDao.delete(place.toPlaceEntity())
        }
    }

    override fun updateOrder(places: List<Place>) {
        doAsync(executorService = IO_EXECUTOR) {
            val updatedList = places
                    .filterNot { it.id == Place.CURRENT_LOCATION.id || it.id == Place.WORLD.id }
                    .mapIndexed { index, place -> place.copy(order = index).toPlaceEntity() }
                    .toList()
            placeDao.update(updatedList)
        }
    }

    override suspend fun getPlacesSuspending(): Either<List<Place>, Throwable> {
        val places = placeDao.getAll().map { substituteWithLocalizedName(it.toPlace()) }.toList()
        return Either.Success(places)
    }

    override suspend fun getPlaceSuspending(placeId: Int): Either<Place, Throwable> {
        val p = placeDao.get(placeId)
                ?: return Either.Failure(NoSuchElementException("Place with the given id doesn't exist: $placeId"))
        val place = substituteWithLocalizedName(p.toPlace())
        return if (placeId == Place.CURRENT_LOCATION.id) {
            locationRepository.getLastCoordinates().map { place.copy(coordinates = it) }
        } else {
            Either.success(place)
        }
    }

    override suspend fun savePlaceSuspending(place: Place) {
        placeDao.save(place.toPlaceEntity())
    }

    override suspend fun deletePlaceSuspending(place: Place) {
        placeDao.delete(place.toPlaceEntity())
    }

    override fun deleteById(id: Int) {
        placeDao.deleteById(id)
    }

    override suspend fun updateOrderSuspending(places: List<Place>) {
        val updatedList = places
                .filterNot { it.id == Place.CURRENT_LOCATION.id || it.id == Place.WORLD.id }
                .mapIndexed { index, place -> place.copy(order = index).toPlaceEntity() }
                .toList()
        placeDao.update(updatedList)
    }

    override suspend fun updateOrderById(ids: List<Int>) {
        ids.forEachIndexed { index, id ->
            if (id != Place.WORLD.id && id != Place.CURRENT_LOCATION.id) {
                placeDao.updateOrder(id, index)
            }
        }
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: PlacesRepository? = null

        fun getInstance(locationRepository: LocationRepository = LocationProvider(), placeDao: PlaceDao = AppDb.getInstance().getPlaceDao()): PlacesRepository {
            return instance ?: PlacesDataSource(locationRepository, placeDao)
        }
    }
}