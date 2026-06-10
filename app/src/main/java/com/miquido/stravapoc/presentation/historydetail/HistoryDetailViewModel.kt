package com.miquido.stravapoc.presentation.historydetail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.library.usecase.GetRouteByIdUseCase
import com.miquido.stravapoc.library.usecase.GetWorkoutResultByIdUseCase
import com.miquido.stravapoc.presentation.navigation.AppRoute.HistoryDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class HistoryDetailViewModel @Inject constructor(
    private val getWorkoutById: GetWorkoutResultByIdUseCase,
    private val getRouteById: GetRouteByIdUseCase,
    savedStateHandle: SavedStateHandle
) : MviViewModel<HistoryDetailViewState>(HistoryDetailViewState(), MviDefaultConfig()) {

    private val route: HistoryDetail = savedStateHandle.toRoute()

    init {
        loadData()
    }

    private fun loadData() = launch {
        transform { copy(isLoading = true, error = null) }
        val workout = getWorkoutById(route.workoutId)
        if (workout == null) {
            transform { copy(isLoading = false, error = "Activity not found") }
            return@launch
        }
        val route = runCatching { getRouteById(workout.routeId).getOrNull() }.getOrNull()
        transform {
            copy(
                entity = workout,
                route = route,
                trackedPoints = workout.trackedPoints,
                isLoading = false
            )
        }
    }
}
