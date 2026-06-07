package com.miquido.stravapoc.wear.presentation.activityselection

sealed class ActivitySelectionSideEffect {
    data class NavigateToWorkout(val activityId: String, val routeId: String) : ActivitySelectionSideEffect()
}
