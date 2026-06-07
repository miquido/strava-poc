package com.miquido.stravapoc.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WorkoutResultEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutResultDao(): WorkoutResultDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stravapoc_db"
                ).build().also { INSTANCE = it }
            }
    }
}
