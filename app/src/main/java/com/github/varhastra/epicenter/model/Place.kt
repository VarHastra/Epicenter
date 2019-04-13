package com.github.varhastra.epicenter.model

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
    @ColumnInfo(name = "radius") val radius: Int?
) {
    val latitude: Double
        get() = coordinates.latitude

    val longitude: Double
        get() = coordinates.longitude

    fun checkCoordinates(point: Coordinates): Boolean {
        return if (radius == null) {
            true
        } else {
            val kLat = latDegToKm(1.0)
            val kLng = lngDegToMi(1.0, latitude)

            val y = abs(latitude - point.latitude) * kLat
            val x = abs(longitude - point.longitude) * kLng

            x * x + y * y <= radius * radius
        }
    }

    companion object {
        val WORLD = Place(name = "San Francisco", coordinates = Coordinates(37.757815, -122.5076402), radius = null)
    }
}