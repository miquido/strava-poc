package com.miquido.stravapoc.library.data.datasource

import com.miquido.stravapoc.library.data.db.app.dao.WorkoutResultDao
import com.miquido.stravapoc.library.data.db.app.entity.WorkoutResultEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class WorkoutResultLocalDataSource @Inject constructor(
    private val dao: WorkoutResultDao
) {
    fun getAll(): Flow<List<WorkoutResultEntity>> = dao.getAllAsFlow()
    suspend fun getById(id: Long): WorkoutResultEntity? = dao.getById(id)
    suspend fun insert(entity: WorkoutResultEntity): Long = dao.insert(entity)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
