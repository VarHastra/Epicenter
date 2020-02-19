package com.github.varhastra.epicenter.domain.model

data class Place(
        val id: Int = 100,
        val name: String,
        val geoArea: GeoArea
) {
    val coordinates get() = geoArea.center

    val latitude get() = coordinates.latitude

    val longitude get() = coordinates.longitude

    val radiusKm get() = geoArea.radiusKm

    operator fun contains(point: Coordinates) = point in geoArea

    companion object {
        private const val CURRENT_LOCATION_RADIUS = 500.0

        val CURRENT_LOCATION = Place(
                999998,
                "Current location",
                GeoArea(
                        Coordinates(0.0, 0.0),
                        CURRENT_LOCATION_RADIUS
                )
        )

        val WORLD = Place(
                999999,
                "World",
                GeoArea(
                        Coordinates(0.0, 0.0),
                        Double.POSITIVE_INFINITY
                )
        )
    }
}