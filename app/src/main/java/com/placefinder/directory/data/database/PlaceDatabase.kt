package com.placefinder.directory.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.placefinder.directory.data.models.Place

/**
 * SQLite database backed by Room. Everything is device-local in Stage 1;
 * Stage 4 can add a remote sync layer on top of the same models.
 */
@Database(entities = [Place::class], version = 2, exportSchema = false)
abstract class PlaceDatabase : RoomDatabase() {

    abstract fun placeDao(): PlaceDao

    companion object {
        @Volatile
        private var instance: PlaceDatabase? = null

        fun get(context: Context): PlaceDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlaceDatabase::class.java,
                    "places.db"
                ).fallbackToDestructiveMigration()
                 .build()
                 .also { instance = it }
            }
        }
    }
}
