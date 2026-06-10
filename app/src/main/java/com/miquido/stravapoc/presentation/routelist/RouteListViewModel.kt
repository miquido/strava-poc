package com.miquido.stravapoc.presentation.routelist

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.library.usecase.GetRoutesUseCase
import com.miquido.stravapoc.presentation.navigation.AppRoute
import com.miquido.stravapoc.presentation.routelist.RouteListSideEffect.NavigateToDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class RouteListViewModel @Inject constructor(
    private val getRoutesUseCase: GetRoutesUseCase,
    savedStateHandle: SavedStateHandle
) : MviViewModel<RouteListViewState>(RouteListViewState(), MviDefaultConfig()) {

    private val route: AppRoute.RouteList = savedStateHandle.toRoute()

    init {
        transform { copy(activityType = route.activityType) }
        loadRoutes()
    }

    private fun loadRoutes() = launch {
        transform { copy(isLoading = true, error = null) }
        getRoutesUseCase(type = route.activityType)
            .onSuccess { routes -> transform { copy(routes = routes, isLoading = false) } }
            .onFailure { e -> transform { copy(isLoading = false, error = e.message) } }
    }

    fun onRouteSelected(routeId: String) = launch {
        emitSideEffect(NavigateToDetail(routeId))
    }
}
