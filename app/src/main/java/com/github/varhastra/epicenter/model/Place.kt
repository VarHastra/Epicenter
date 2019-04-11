package com.github.varhastra.epicenter.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "place")
data class Place(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "coordinates") val coordinates: Coordinates
) {
    val latitude: Double
        get() = coordinates.latitude

    val longitude: Double
        get() = coordinates.longitude
}