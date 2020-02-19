package com.github.varhastra.epicenter.data

import com.github.varhastra.epicenter.data.db.PlaceEntity
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.GeoArea
import com.github.varhastra.epicenter.domain.model.Place

fun PlaceEntity.toPlace(): Place {
    return Place(
            id,
            name,
            GeoArea(
                    Coordinates(latitude, longitude),
                    radiusKm
            )
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