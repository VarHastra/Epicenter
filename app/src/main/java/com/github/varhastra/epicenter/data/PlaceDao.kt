package com.github.varhastra.epicenter.data

import androidx.room.Dao
import androidx.room.Query
import com.github.varhastra.epicenter.model.Place

@Dao
interface PlaceDao {

    @Query("SELECT * FROM place")
    fun getAll(): List<Place>
}