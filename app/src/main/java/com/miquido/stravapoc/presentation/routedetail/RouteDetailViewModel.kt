package com.miquido.stravapoc.presentation.routedetail

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.di.AppModule
import com.miquido.stravapoc.library.data.usecase.GetRouteByIdUseCase
import com.miquido.stravapoc.sync.WearSyncManager

class RouteDetailViewModel(
    private val routeId: String,
    private val getRouteByIdUseCase: GetRouteByIdUseCase,
    private val wearSyncManager: WearSyncManager
) : MviViewModel<RouteDetailViewState>(RouteDetailViewState(), MviDefaultConfig()) {

    init {
        loadRoute()
    }

    private fun loadRoute() = launch {
        transform { copy(isLoading = true, error = null) }
        getRouteByIdUseCase(routeId)
            .onSuccess { route -> transform { copy(route = route, isLoading = false) } }
            .onFailure { e -> transform { copy(isLoading = false, error = e.message) } }
    }

    fun onSendToWatch() = launch {
        val route = viewState.value.route ?: return@launch
        transform { copy(isSending = true) }
        wearSyncManager.sendRoute(route)
            .onSuccess {
                transform { copy(isSending = false) }
                emitSideEffect(RouteDetailSideEffect.RouteSentSuccess)
            }
            .onFailure { e ->
                transform { copy(isSending = false) }
                emitSideEffect(RouteDetailSideEffect.RouteSentError(e.message ?: "Błąd wysyłki"))
            }
    }

    companion object {
        fun factory(routeId: String, application: Application): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    RouteDetailViewModel(
                        routeId = routeId,
                        getRouteByIdUseCase = AppModule.getRouteByIdUseCase,
                        wearSyncManager = WearSyncManager(application)
                    )
                }
            }
    }
}
