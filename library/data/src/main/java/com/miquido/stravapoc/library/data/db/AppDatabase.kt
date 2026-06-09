package com.miquido.stravapoc.library.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.miquido.stravapoc.library.data.db.dao.WorkoutResultDao
import com.miquido.stravapoc.library.data.db.entity.WorkoutResultEntity

@Database(entities = [WorkoutResultEntity::class], version = 2)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutResultDao(): WorkoutResultDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE workout_results ADD COLUMN trackedPointsJson TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stravapoc_db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
