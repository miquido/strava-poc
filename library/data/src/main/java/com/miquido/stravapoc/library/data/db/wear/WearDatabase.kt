package com.miquido.stravapoc.library.data.db.wear

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WearRouteEntity::class, PendingWorkoutResultEntity::class],
    version = 3,
    exportSchema = false
)
abstract class WearDatabase : RoomDatabase() {
    abstract fun wearRouteDao(): WearRouteDao
    abstract fun pendingWorkoutResultDao(): PendingWorkoutResultDao

    companion object {
        @Volatile
        private var instance: WearDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `pending_workout_results`
                    (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                     `workoutResultJson` TEXT NOT NULL,
                     `createdAt` INTEGER NOT NULL)"""
                )
            }
        }

        fun getInstance(context: Context): WearDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WearDatabase::class.java,
                    "wear_db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
    }
}
