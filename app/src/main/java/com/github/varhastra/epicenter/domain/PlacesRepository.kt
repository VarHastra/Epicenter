package com.github.varhastra.epicenter.domain

import com.github.varhastra.epicenter.domain.model.Place

interface PlacesRepository {

    fun getPlaces(callback: RepositoryCallback<List<Place>>)

    fun getPlace(callback: RepositoryCallback<Place>, placeId: Int)

    fun savePlace(place: Place)

    fun deletePlace(place: Place)

    fun updateOrder(places: List<Place>)
}