package com.miquido.stravapoc.library.data.repository

import com.miquido.stravapoc.library.data.model.WorkoutResult
import kotlinx.coroutines.flow.Flow

interface WorkoutResultRepository {
    fun getHistory(): Flow<List<WorkoutResult>>
    suspend fun getById(id: Long): WorkoutResult?
    suspend fun save(result: WorkoutResult): Long
    suspend fun delete(id: Long)
}
