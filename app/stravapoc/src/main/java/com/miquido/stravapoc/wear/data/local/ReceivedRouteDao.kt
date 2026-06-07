package com.miquido.stravapoc.wear.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceivedRouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRoute(entity: ReceivedRouteEntity)

    @Query("SELECT * FROM received_routes WHERE id = 0")
    fun observeRoute(): Flow<ReceivedRouteEntity?>
}
