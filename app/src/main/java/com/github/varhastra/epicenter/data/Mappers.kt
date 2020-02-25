package com.github.varhastra.epicenter.data

import com.github.varhastra.epicenter.data.db.PlaceEntity
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.GeoArea
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.PlaceName

fun PlaceEntity.toPlace() = Place(
        id,
        name,
        GeoArea(
                Coordinates(latitude, longitude),
                radiusKm
        )
)

fun PlaceEntity.toPlaceName() = PlaceName(id, name)

fun Place.toPlaceEntity() = PlaceEntity(
        id,
        name,
        latitude,
        longitude,
        radiusKm
)

fun Place.toPlaceName() = PlaceName(id, name)