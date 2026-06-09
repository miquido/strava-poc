package com.miquido.stravapoc.library.data.repository

import com.miquido.stravapoc.library.data.db.dao.WorkoutResultDao
import com.miquido.stravapoc.library.data.db.entity.toDomain
import com.miquido.stravapoc.library.data.db.entity.toEntity
import com.miquido.stravapoc.library.data.model.WorkoutResult
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class WorkoutResultRepositoryImpl @Inject constructor(
    private val dao: WorkoutResultDao
) : WorkoutResultRepository {
    override fun getHistory(): Flow<List<WorkoutResult>> =
        dao.getAllAsFlow().map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Long): WorkoutResult? = dao.getById(id)?.toDomain()
    override suspend fun save(result: WorkoutResult): Long = dao.insert(result.toEntity())
    override suspend fun delete(id: Long) = dao.deleteById(id)
}
