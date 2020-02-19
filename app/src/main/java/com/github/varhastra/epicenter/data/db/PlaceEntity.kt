package com.github.varhastra.epicenter.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "place")
data class PlaceEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "latitude") val latitude: Double,
        @ColumnInfo(name = "longitude") val longitude: Double,
        @ColumnInfo(name = "radius") val radiusKm: Double?,
        @ColumnInfo(name = "position") val position: Int = defaultPosition
)

private const val defaultPosition = -100