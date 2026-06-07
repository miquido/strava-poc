package com.miquido.stravapoc.presentation.routelist

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.di.AppModule
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.usecase.GetRoutesUseCase

class RouteListViewModel(
    private val activityType: ActivityType,
    private val getRoutesUseCase: GetRoutesUseCase
) : MviViewModel<RouteListViewState>(RouteListViewState(activityType = activityType), MviDefaultConfig()) {

    init {
        loadRoutes()
    }

    private fun loadRoutes() = launch {
        transform { copy(isLoading = true, error = null) }
        getRoutesUseCase(type = activityType)
            .onSuccess { routes -> transform { copy(routes = routes, isLoading = false) } }
            .onFailure { e -> transform { copy(isLoading = false, error = e.message) } }
    }

    fun onRouteSelected(routeId: String) = launch {
        emitSideEffect(RouteListSideEffect.NavigateToDetail(routeId))
    }

    companion object {
        fun factory(activityType: ActivityType): ViewModelProvider.Factory = viewModelFactory {
            initializer { RouteListViewModel(activityType, AppModule.getRoutesUseCase) }
        }
    }
}
