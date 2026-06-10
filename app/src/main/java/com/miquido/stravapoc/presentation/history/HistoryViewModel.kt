package com.miquido.stravapoc.presentation.history

import androidx.lifecycle.viewModelScope
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.library.usecase.DeleteWorkoutResultUseCase
import com.miquido.stravapoc.library.usecase.GetWorkoutHistoryUseCase
import com.miquido.stravapoc.presentation.history.HistorySideEffect.NavigateToDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
internal class HistoryViewModel @Inject constructor(
    getHistoryUseCase: GetWorkoutHistoryUseCase,
    private val deleteWorkoutResultUseCase: DeleteWorkoutResultUseCase
) : MviViewModel<HistoryViewState>(HistoryViewState(), MviDefaultConfig()) {

    init {
        getHistoryUseCase()
            .onEach { items -> transform { copy(items = items, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    fun onItemSelected(id: Long) = launch {
        emitSideEffect(NavigateToDetail(id))
    }

    fun onDeleteItem(id: Long) = launch {
        deleteWorkoutResultUseCase(id)
    }
}
