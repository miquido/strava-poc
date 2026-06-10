package com.miquido.stravapoc.presentation.activitytype

import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.presentation.activitytype.ActivityTypeSideEffect.NavigateToRouteList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class ActivityTypeViewModel @Inject constructor() :
    MviViewModel<ActivityTypeViewState>(ActivityTypeViewState(), MviDefaultConfig()) {

    fun onActivitySelected(activityType: ActivityType) = launch {
        emitSideEffect(NavigateToRouteList(activityType))
    }
}
