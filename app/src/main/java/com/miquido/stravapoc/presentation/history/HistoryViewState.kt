package com.miquido.stravapoc.presentation.history

import com.miquido.stravapoc.library.data.model.WorkoutResult

internal data class HistoryViewState(
    val items: List<WorkoutResult> = emptyList(),
    val isLoading: Boolean = true
)
