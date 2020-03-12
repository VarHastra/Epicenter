package me.alex.pet.apps.epicenter.data.db

import androidx.room.*

@Dao
abstract class PlaceDao {

    @Insert
    abstract fun insert(place: PlaceEntity)

    @Insert
    abstract fun insert(list: List<PlaceEntity>)

    @Query("SELECT * FROM place ORDER BY `position` ASC")
    abstract fun getAll(): List<PlaceEntity>

    @Query("SELECT * FROM place WHERE id=:id")
    abstract fun get(id: Int): PlaceEntity?

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

    @Query("UPDATE place SET 'position' = :position WHERE id = :id")
    abstract fun updateOrder(id: Int, position: Int)

    @Delete
    abstract fun delete(place: PlaceEntity)

    @Query("DELETE FROM place WHERE id = :id")
    abstract fun deleteById(id: Int)
}