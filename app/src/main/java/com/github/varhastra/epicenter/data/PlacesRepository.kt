package com.github.varhastra.epicenter.data

import com.github.varhastra.epicenter.IO_EXECUTOR
import com.github.varhastra.epicenter.data.db.AppDb
import com.github.varhastra.epicenter.data.db.PlaceDao
import com.github.varhastra.epicenter.device.LocationProvider
import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.LocationDataSource
import com.github.varhastra.epicenter.domain.PlacesDataSource
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.Position
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class PlacesRepository private constructor(private val locationDataSource: LocationDataSource, private val placeDao: PlaceDao) : PlacesDataSource {

    override fun getPlaces(callback: DataSourceCallback<List<Place>>) {
        doAsync(executorService = IO_EXECUTOR) {
            val places = placeDao.getAll()
            uiThread {
                callback.onResult(places)
            }
        }
    }

    override fun getPlace(callback: DataSourceCallback<Place>, placeId: Int) {
        doAsync(executorService = IO_EXECUTOR) {
            val place = placeDao.get(placeId)
            if (placeId == Place.CURRENT_LOCATION.id) {
                locationDataSource.getLastLocation(object : DataSourceCallback<Position> {
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

    override fun savePlace(place: Place) {
        doAsync(executorService = IO_EXECUTOR) {
            placeDao.save(place)
        }
    }


    companion object {
        private var instance: PlacesRepository? = null

        fun getInstance(locationDataSource: LocationDataSource = LocationProvider(), placeDao: PlaceDao = AppDb.getInstance().getPlaceDao()): PlacesRepository {
            return instance ?: PlacesRepository(locationDataSource, placeDao)
        }
    }
}