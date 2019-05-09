package com.github.varhastra.epicenter.data.db

import androidx.room.*
import com.github.varhastra.epicenter.domain.model.Place

@Dao
abstract class PlaceDao {

    @Query("SELECT * FROM place ORDER BY `order` ASC")
    abstract fun getAll(): List<Place>

    @Query("SELECT * FROM place WHERE id=:id")
    abstract fun get(id: Int): Place?

    @Insert
    abstract fun insert(place: Place)

    @Insert
    abstract fun insert(list: List<Place>)

    @Update
    abstract fun update(place: Place)

    @Update
    abstract fun update(list: List<Place>)

    @Delete
    abstract fun delete(place: Place)

    fun save(place: Place) {
        if (unsafeInsert(place) == -1L) {
            unsafeUpdate(place)
        }
    }

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun unsafeUpdate(place: Place)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun unsafeInsert(place: Place): Long
}