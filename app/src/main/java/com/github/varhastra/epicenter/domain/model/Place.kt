package com.github.varhastra.epicenter.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.varhastra.epicenter.utils.latDegToKm
import com.github.varhastra.epicenter.utils.lngDegToMi
import kotlin.math.abs


@Entity(tableName = "place")
data class Place(
        @PrimaryKey(autoGenerate = true) val id: Int = 100,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "coordinates") val coordinates: Coordinates,
        @ColumnInfo(name = "radius") val radiusKm: Double?,
        @ColumnInfo(name = "order") val order: Int = -10
) {
    val latitude: Double
        get() = coordinates.latitude

    val longitude: Double
        get() = coordinates.longitude

    fun checkCoordinates(point: Coordinates): Boolean {
        return if (radiusKm == null) {
            true
        } else {
            val kLat = latDegToKm(1.0)
            val kLng = lngDegToMi(1.0, latitude)

            val y = abs(latitude - point.latitude) * kLat
            val x = abs(longitude - point.longitude) * kLng

            x * x + y * y <= radiusKm * radiusKm
        }
    }

    companion object {
        private const val MIN_RADIUS = 500.0
        val CURRENT_LOCATION = Place(1, "Current location", Coordinates(37.757815, -122.5076402), MIN_RADIUS, -100)
        val WORLD = Place(2, "World", Coordinates(37.757815, -122.5076402), null, -99)
    }
}