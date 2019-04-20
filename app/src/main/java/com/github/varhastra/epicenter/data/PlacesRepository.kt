package com.github.varhastra.epicenter.data

import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.PlacesDataSource
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Place

class PlacesRepository() : PlacesDataSource {

    override fun getPlaces(callback: DataSourceCallback<List<Place>>) {
        // TODO: stub, implement when ready
        val places = listOf(
            Place(0, "Current Location", Coordinates(34.0207305, -118.6919151), 1000),
            Place(1, "World", Coordinates(0.0, 0.0), null),
            Place(2, "Port Moresby", Coordinates(-9.4374361, 147.1552399), 2000),
            Place(3, "Anchorage, Alaska", Coordinates(61.1083688, -150.0006822), 3000)
        )
        callback.onResult(places)
    }

    override fun getPlace(callback: DataSourceCallback<Place>, placeId: Int) {
        // TODO: stub, implement when ready
        val places = listOf(
            Place(0, "Current Location", Coordinates(34.0207305, -118.6919151), 1000),
            Place(1, "World", Coordinates(0.0, 0.0), null),
            Place(2, "Port Moresby", Coordinates(-9.4374361, 147.1552399), 2000),
            Place(3, "Anchorage, Alaska", Coordinates(61.1083688, -150.0006822), 3000)
        )

        callback.onResult(places.first { it.id == placeId })
    }

    override fun savePlace(place: Place) {
        TODO("stub, not implemented")
    }
}