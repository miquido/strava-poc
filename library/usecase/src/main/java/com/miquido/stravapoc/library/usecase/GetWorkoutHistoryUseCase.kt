package com.miquido.stravapoc.library.usecase

import com.miquido.stravapoc.library.data.model.WorkoutResult
import com.miquido.stravapoc.library.data.repository.WorkoutResultRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetWorkoutHistoryUseCase @Inject constructor(private val repository: WorkoutResultRepository) {
    operator fun invoke(): Flow<List<WorkoutResult>> = repository.getHistory()
}
