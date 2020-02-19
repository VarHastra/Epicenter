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

    @Query("UPDATE place SET latitude = :latitude, longitude = :longitude, radius = :areaRadiusKm WHERE id = :id")
    abstract fun update(id: Int, latitude: Double, longitude: Double, areaRadiusKm: Double)

    @Query("UPDATE place SET name = :name WHERE id = :id")
    abstract fun update(id: Int, name: String)

    @Query("""UPDATE place SET 
                    name = :name, 
                    latitude = :latitude,
                    longitude = :longitude, 
                    radius = :areaRadiusKm 
                    WHERE id = :id""")
    abstract fun update(id: Int, name: String, latitude: Double, longitude: Double, areaRadiusKm: Double)

    @Update
    abstract fun update(list: List<PlaceEntity>)

    @Query("UPDATE place SET 'order' = :order WHERE id = :id")
    abstract fun updateOrder(id: Int, order: Int)

    @Delete
    abstract fun delete(place: PlaceEntity)

    @Query("DELETE FROM place WHERE id = :id")
    abstract fun deleteById(id: Int)

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