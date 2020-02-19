package com.github.varhastra.epicenter.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.varhastra.epicenter.App

@Database(entities = [PlaceEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {

    abstract fun getPlaceDao(): PlaceDao

    companion object {
        private const val DB_NAME = "epicenter.db"

        private var instance: AppDb? = null

        fun getInstance(context: Context = App.instance): AppDb {
            return instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDb::class.java,
                    DB_NAME
            ).allowMainThreadQueries().build().apply {
                instance = this
            }
        }
    }
}