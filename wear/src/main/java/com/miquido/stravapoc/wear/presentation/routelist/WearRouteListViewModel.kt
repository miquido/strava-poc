package com.miquido.stravapoc.wear.presentation.routelist

import androidx.lifecycle.SavedStateHandle
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.usecase.GetWearRoutesUseCase
import com.miquido.stravapoc.wear.data.CUSTOM_ROUTE_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WearRouteListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRoutesUseCase: GetWearRoutesUseCase,
) : MviViewModel<WearRouteListViewState>(
    WearRouteListViewState(activityType = ActivityType.valueOf(
        checkNotNull(savedStateHandle.get<String>("activityType"))
    )),
    MviDefaultConfig()
) {
    private val activityType: ActivityType get() = viewState.value.activityType

    init {
        loadRoutes()
    }

    private fun loadRoutes() = launch {
        transform { copy(isLoading = true, error = null) }
        getRoutesUseCase(type = activityType)
            .onSuccess { routes -> transform { copy(routes = routes, isLoading = false) } }
            .onFailure { e -> transform { copy(isLoading = false, error = e.message) } }
    }

    fun onCustomActivitySelected() = launch {
        emitSideEffect(
            WearRouteListSideEffect.NavigateToWorkout(
                routeId = CUSTOM_ROUTE_ID,
                activityType = activityType
            )
        )
    }

    fun onRouteSelected(routeId: String, routeActivityType: ActivityType) = launch {
        emitSideEffect(
            WearRouteListSideEffect.NavigateToWorkout(
                routeId = routeId,
                activityType = routeActivityType
            )
        )
    }
}
