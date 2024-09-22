package com.eva4

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Place::class, PhotosSaved::class], version = 1)
abstract class DataBase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun photoSavedDao() : PhotoSavedDao
    companion object {
        @Volatile
        private var INSTANCE: DataBase? = null

        fun getDatabase(context: Context): DataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataBase::class.java,
                    "place_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}