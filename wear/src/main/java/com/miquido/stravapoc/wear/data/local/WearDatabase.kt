package com.miquido.stravapoc.wear.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ReceivedRouteEntity::class], version = 1, exportSchema = false)
abstract class WearDatabase : RoomDatabase() {
    abstract fun receivedRouteDao(): ReceivedRouteDao

    companion object {
        @Volatile
        private var instance: WearDatabase? = null

        fun getInstance(context: Context): WearDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WearDatabase::class.java,
                    "wear_db"
                ).build().also { instance = it }
            }
    }
}
