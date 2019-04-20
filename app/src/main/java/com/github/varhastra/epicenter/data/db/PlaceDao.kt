package com.github.varhastra.epicenter.data.db

import androidx.room.Dao
import androidx.room.Query
import com.github.varhastra.epicenter.domain.model.Place

@Dao
interface PlaceDao {

    @Query("SELECT * FROM place")
    fun getAll(): List<Place>
}