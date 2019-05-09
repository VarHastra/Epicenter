package com.github.varhastra.epicenter.data.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.ioThread
import java.util.*

@Database(entities = [Place::class], version = 1)
@TypeConverters(AppDb.Converters::class)
abstract class AppDb : RoomDatabase() {

    abstract fun getPlaceDao(): PlaceDao


    companion object {
        private const val DB_NAME = "epicenter.db"
        private var instance: AppDb? = null
        val prepopulateData = listOf(
                Place.CURRENT_LOCATION,
                Place.WORLD
        )

        fun getInstance(context: Context = App.instance): AppDb {
            return instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDb::class.java,
                    DB_NAME
            )
                    .allowMainThreadQueries()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            ioThread {
                                getInstance().getPlaceDao().insert(prepopulateData)
                            }
                        }
                    })
                    .build().apply {
                        instance = this
                    }

        }
    }

    class Converters {
        @TypeConverter
        fun fromCoordinates(coordinates: Coordinates): String {
            return String.format(Locale.US, "%f:%f", coordinates.latitude, coordinates.longitude)
        }

        @TypeConverter
        fun toCoordinates(string: String): Coordinates {
            val parts = string.split(":")
            return Coordinates(
                    parts[0].toDoubleOrNull() ?: 0.0,
                    parts[1].toDoubleOrNull() ?: 0.0
            )
        }
    }
}