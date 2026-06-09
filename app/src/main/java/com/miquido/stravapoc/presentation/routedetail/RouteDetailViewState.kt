package com.miquido.stravapoc.presentation.routedetail

import com.miquido.stravapoc.library.data.model.Route

internal data class RouteDetailViewState(
    val route: Route? = null,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)
