package com.miquido.stravapoc.presentation.historydetail

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.di.AppModule

class HistoryDetailViewModel(
    private val workoutId: Long,
    application: Application
) : MviViewModel<HistoryDetailViewState>(HistoryDetailViewState(), MviDefaultConfig()) {

    private val workoutUseCases = AppModule.provideWorkoutUseCases(application)

    init {
        loadData()
    }

    private fun loadData() = launch {
        transform { copy(isLoading = true, error = null) }
        val entity = workoutUseCases.getById(workoutId)
        if (entity == null) {
            transform { copy(isLoading = false, error = "Nie znaleziono aktywności") }
            return@launch
        }
        val route = runCatching {
            AppModule.getRouteByIdUseCase(entity.routeId).getOrNull()
        }.getOrNull()
        transform { copy(entity = entity, route = route, isLoading = false) }
    }

    companion object {
        fun factory(workoutId: Long, application: Application): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { HistoryDetailViewModel(workoutId, application) }
            }
    }
}
