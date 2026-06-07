package com.miquido.stravapoc.wear.presentation.activityselection

data class ActivityType(val id: String, val label: String, val emoji: String)

data class ActivitySelectionViewState(
    val activities: List<ActivityType> = listOf(
        ActivityType("running", "Bieganie", "🏃"),
        ActivityType("cycling", "Jazda rowerem", "🚴"),
        ActivityType("walking", "Chodzenie", "🚶")
    ),
    val receivedRouteId: String? = null,
    val receivedRouteName: String? = null
)
