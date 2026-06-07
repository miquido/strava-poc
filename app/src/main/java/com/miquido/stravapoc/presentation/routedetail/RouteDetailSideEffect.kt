package com.miquido.stravapoc.presentation.routedetail

sealed class RouteDetailSideEffect {
    object RouteSentSuccess : RouteDetailSideEffect()
    data class RouteSentError(val message: String) : RouteDetailSideEffect()
}
