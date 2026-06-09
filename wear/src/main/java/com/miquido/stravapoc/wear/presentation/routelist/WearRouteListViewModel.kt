package com.miquido.stravapoc.wear.presentation.routelist

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.usecase.GetRoutesUseCase
import com.miquido.stravapoc.wear.data.WorkoutService
import com.miquido.stravapoc.wear.di.WearAppModule

class WearRouteListViewModel(
    private val activityType: ActivityType,
    private val getRoutesUseCase: GetRoutesUseCase
) : MviViewModel<WearRouteListViewState>(
    WearRouteListViewState(activityType = activityType),
    MviDefaultConfig()
) {
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
                routeId = WorkoutService.CUSTOM_ROUTE_ID,
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

    companion object {
        fun factory(activityType: ActivityType, context: Context): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    WearRouteListViewModel(
                        activityType = activityType,
                        getRoutesUseCase = WearAppModule.getRoutesUseCase
                    )
                }
            }
    }
}
