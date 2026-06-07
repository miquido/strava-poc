package com.miquido.stravapoc.presentation.history

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.di.AppModule
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HistoryViewModel(
    application: Application
) : MviViewModel<HistoryViewState>(HistoryViewState(), MviDefaultConfig()) {

    private val useCases = AppModule.provideWorkoutUseCases(application)

    init {
        useCases.getHistory()
            .onEach { items ->
                transform { copy(items = items, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onItemSelected(id: Long) = launch {
        emitSideEffect(HistorySideEffect.NavigateToDetail(id))
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer { HistoryViewModel(application) }
        }
    }
}
