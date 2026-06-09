package com.miquido.stravapoc.presentation.routedetail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.library.usecase.GetRouteByIdUseCase
import com.miquido.stravapoc.presentation.navigation.AppRoute
import com.miquido.stravapoc.sync.RouteSender
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class RouteDetailViewModel @Inject constructor(
    private val getRouteByIdUseCase: GetRouteByIdUseCase,
    private val routeSender: RouteSender,
    savedStateHandle: SavedStateHandle
) : MviViewModel<RouteDetailViewState>(RouteDetailViewState(), MviDefaultConfig()) {

    private val route: AppRoute.RouteDetail = savedStateHandle.toRoute()

    init {
        loadRoute()
    }

    private fun loadRoute() = launch {
        transform { copy(isLoading = true, error = null) }
        getRouteByIdUseCase(route.routeId)
            .onSuccess { r -> transform { copy(route = r, isLoading = false) } }
            .onFailure { e -> transform { copy(isLoading = false, error = e.message) } }
    }

    fun onSendToWatch() = launch {
        val r = viewState.value.route ?: return@launch
        transform { copy(isSending = true) }
        routeSender.send(r)
            .onSuccess {
                transform { copy(isSending = false) }
                emitSideEffect(RouteDetailSideEffect.RouteSentSuccess)
            }
            .onFailure { e ->
                transform { copy(isSending = false) }
                emitSideEffect(RouteDetailSideEffect.RouteSentError(e.message ?: "Failed to send route"))
            }
    }
}
