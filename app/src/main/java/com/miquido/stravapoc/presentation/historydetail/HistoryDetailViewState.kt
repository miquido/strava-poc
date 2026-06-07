package com.miquido.stravapoc.presentation.historydetail

import com.miquido.stravapoc.data.db.WorkoutResultEntity
import com.miquido.stravapoc.library.data.model.Route

data class HistoryDetailViewState(
    val entity: WorkoutResultEntity? = null,
    val route: Route? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
