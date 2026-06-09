package com.miquido.stravapoc.presentation.activitytype

import androidx.lifecycle.viewModelScope
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.library.data.model.ActivityType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class ActivityTypeViewModel @Inject constructor() :
    MviViewModel<ActivityTypeViewState>(ActivityTypeViewState(), MviDefaultConfig()) {

    fun onActivitySelected(activityType: ActivityType) = launch {
        emitSideEffect(ActivityTypeSideEffect.NavigateToRouteList(activityType))
    }
}
