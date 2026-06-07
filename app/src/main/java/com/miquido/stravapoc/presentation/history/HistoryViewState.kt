package com.miquido.stravapoc.presentation.history

import com.miquido.stravapoc.data.db.WorkoutResultEntity

data class HistoryViewState(
    val items: List<WorkoutResultEntity> = emptyList(),
    val isLoading: Boolean = true
)
