package com.github.varhastra.epicenter.data.db

import androidx.room.*

@Dao
abstract class PlaceDao {

    @Query("SELECT * FROM place ORDER BY `order` ASC")
    abstract fun getAll(): List<PlaceEntity>

    @Query("SELECT * FROM place WHERE id=:id")
    abstract fun get(id: Int): PlaceEntity?

    @Insert
    abstract fun insert(place: PlaceEntity)

    @Insert
    abstract fun insert(list: List<PlaceEntity>)

    @Update
    abstract fun update(place: PlaceEntity)

    @Update
    abstract fun update(list: List<PlaceEntity>)

    @Delete
    abstract fun delete(place: PlaceEntity)

    fun save(place: PlaceEntity) {
        if (unsafeInsert(place) == -1L) {
            unsafeUpdate(place)
        }
    }

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun unsafeUpdate(place: PlaceEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun unsafeInsert(place: PlaceEntity): Long
}