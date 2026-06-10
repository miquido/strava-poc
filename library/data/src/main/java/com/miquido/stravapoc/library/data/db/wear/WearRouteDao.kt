package com.miquido.stravapoc.library.data.db.wear

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WearRouteDao {
    @Query("SELECT * FROM wear_routes ORDER BY receivedAt DESC")
    suspend fun getAll(): List<WearRouteEntity>

    @Query("SELECT * FROM wear_routes WHERE activityType = :type ORDER BY receivedAt DESC")
    suspend fun getByActivityType(type: String): List<WearRouteEntity>

    @Query("SELECT * FROM wear_routes WHERE id = :id")
    suspend fun getById(id: String): WearRouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: WearRouteEntity)
}
