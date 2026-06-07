package com.miquido.stravapoc.data.usecase

import com.miquido.stravapoc.data.db.WorkoutResultEntity
import com.miquido.stravapoc.data.repository.WorkoutResultRepository

class GetWorkoutResultByIdUseCase(private val repository: WorkoutResultRepository) {
    suspend operator fun invoke(id: Long): WorkoutResultEntity? = repository.getById(id)
}
