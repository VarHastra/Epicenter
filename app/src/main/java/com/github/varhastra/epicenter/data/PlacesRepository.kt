package com.github.varhastra.epicenter.data

import com.github.varhastra.epicenter.device.LocationProvider
import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.LocationDataSource
import com.github.varhastra.epicenter.domain.PlacesDataSource
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.Position

class PlacesRepository private constructor(val locationDataSource: LocationDataSource) : PlacesDataSource {

    override fun getPlaces(callback: DataSourceCallback<List<Place>>) {
        // TODO: stub, implement when ready
        val places = listOf(
                Place(0, "Current Location", Coordinates(34.0207305, -118.6919151), 4000.0),
                Place(1, "World", Coordinates(0.0, 0.0), null),
                Place(2, "Port Moresby", Coordinates(-9.4374361, 147.1552399), 2000.0),
                Place(3, "Anchorage, Alaska", Coordinates(61.1083688, -150.0006822), 3000.0)
        )
        callback.onResult(places)
    }

    override fun getPlace(callback: DataSourceCallback<Place>, placeId: Int) {
        // TODO: stub, implement when ready
        val places = listOf(
                Place(0, "Current Location", Coordinates(34.0207305, -118.6919151), 4000.0),
                Place(1, "World", Coordinates(0.0, 0.0), null),
                Place(2, "Port Moresby", Coordinates(-9.4374361, 147.1552399), 2000.0),
                Place(3, "Anchorage, Alaska", Coordinates(61.1083688, -150.0006822), 3000.0)
        )

        val place = places.first { it.id == placeId }
        if (placeId == Place.CURRENT_LOCATION.id) {
            locationDataSource.getLastLocation(object : DataSourceCallback<Position> {
                override fun onResult(result: Position) {
                    callback.onResult(place.copy(coordinates = result.coordinates))
                }

                override fun onFailure(t: Throwable?) {
                    callback.onFailure(t)
                }
            })
        } else {
            callback.onResult(place)
        }
    }

    override fun savePlace(place: Place) {
        TODO("stub, not implemented")
    }


    companion object {
        private var instance: PlacesRepository? = null

        fun getInstance(locationDataSource: LocationDataSource = LocationProvider()): PlacesRepository {
            return instance ?: PlacesRepository(locationDataSource)
        }
    }
}