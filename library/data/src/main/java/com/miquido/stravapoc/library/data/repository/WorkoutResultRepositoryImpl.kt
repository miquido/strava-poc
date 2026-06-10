package com.miquido.stravapoc.library.data.repository

import com.miquido.stravapoc.library.data.datasource.WorkoutResultLocalDataSource
import com.miquido.stravapoc.library.data.db.app.entity.toDomain
import com.miquido.stravapoc.library.data.db.app.entity.toEntity
import com.miquido.stravapoc.library.data.model.WorkoutResult
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class WorkoutResultRepositoryImpl @Inject constructor(
    private val dataSource: WorkoutResultLocalDataSource
) : WorkoutResultRepository {
    override fun getHistory(): Flow<List<WorkoutResult>> =
        dataSource.getAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Long): WorkoutResult? = dataSource.getById(id)?.toDomain()
    override suspend fun save(result: WorkoutResult): Long = dataSource.insert(result.toEntity())
    override suspend fun delete(id: Long) = dataSource.deleteById(id)
}
