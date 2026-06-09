package com.miquido.stravapoc.presentation.routelist

internal sealed class RouteListSideEffect {
    data class NavigateToDetail(val routeId: String) : RouteListSideEffect()
}
