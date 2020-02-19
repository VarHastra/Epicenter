package com.github.varhastra.epicenter.data

import com.github.varhastra.epicenter.data.db.PlaceEntity
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Place

fun PlaceEntity.toPlace(): Place {
    return Place(
            id,
            name,
            Coordinates(latitude, longitude),
            radiusKm
    )
}

fun Place.toPlaceEntity(): PlaceEntity {
    return PlaceEntity(
            id,
            name,
            latitude,
            longitude,
            radiusKm
    )
}