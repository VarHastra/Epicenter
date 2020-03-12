package me.alex.pet.apps.epicenter.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PlaceEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {

    abstract fun getPlaceDao(): PlaceDao
}