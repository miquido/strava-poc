package com.miquido.stravapoc.presentation.activitytype

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.library.data.model.ActivityType

class ActivityTypeViewModel : MviViewModel<ActivityTypeViewState>(
    ActivityTypeViewState(),
    MviDefaultConfig()
) {
    fun onActivitySelected(activityType: ActivityType) = launch {
        emitSideEffect(ActivityTypeSideEffect.NavigateToRouteList(activityType))
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { ActivityTypeViewModel() }
        }
    }
}
