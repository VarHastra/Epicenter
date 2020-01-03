package com.github.varhastra.epicenter.data

import android.annotation.SuppressLint
import android.content.Context
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.IO_EXECUTOR
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.data.db.AppDb
import com.github.varhastra.epicenter.data.db.PlaceDao
import com.github.varhastra.epicenter.device.LocationProvider
import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.LocationRepository
import com.github.varhastra.epicenter.domain.PlacesRepository
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.Position
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class PlacesDataSource private constructor(
        private val locationRepository: LocationRepository,
        private val placeDao: PlaceDao,
        context: Context = App.instance) : PlacesRepository {

    private val context = context.applicationContext

    override fun getPlaces(callback: DataSourceCallback<List<Place>>) {
        doAsync(executorService = IO_EXECUTOR) {
            val places = placeDao.getAll().map { substituteWithLocalizedName(it) }.toList()
            uiThread {
                callback.onResult(places)
            }
        }
    }

    override fun getPlace(callback: DataSourceCallback<Place>, placeId: Int) {
        doAsync(executorService = IO_EXECUTOR) {
            val p = placeDao.get(placeId)
            if (p == null) {
                uiThread {
                    callback.onFailure(NoSuchElementException("Place with the given id doesn't exist: $placeId."))
                }
                return@doAsync
            }
            val place = substituteWithLocalizedName(p)
            if (placeId == Place.CURRENT_LOCATION.id) {
                locationRepository.getLastLocation(object : DataSourceCallback<Position> {
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
            placeDao.save(place)
        }
    }

    override fun deletePlace(place: Place) {
        doAsync(executorService = IO_EXECUTOR) {
            placeDao.delete(place)
        }
    }

    override fun updateOrder(places: List<Place>) {
        doAsync(executorService = IO_EXECUTOR) {
            val updatedList = places
                    .mapIndexed { index, place -> place.copy(order = index) }
                    .filterNot { it.id == Place.CURRENT_LOCATION.id || it.id == Place.WORLD.id }
                    .toList()
            placeDao.update(updatedList)
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