package com.github.varhastra.epicenter.domain

import com.github.varhastra.epicenter.domain.model.Place

interface PlacesRepository {

    fun getPlaces(callback: DataSourceCallback<List<Place>>)

    fun getPlace(callback: DataSourceCallback<Place>, placeId: Int)

    fun savePlace(place: Place)

    fun deletePlace(place: Place)

    fun updateOrder(places: List<Place>)
}