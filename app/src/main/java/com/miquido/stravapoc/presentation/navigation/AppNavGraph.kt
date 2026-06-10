package com.miquido.stravapoc.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.miquido.stravapoc.presentation.historydetail.HistoryDetailScreen
import com.miquido.stravapoc.presentation.home.HomeScreen
import com.miquido.stravapoc.presentation.navigation.AppRoute.HistoryDetail
import com.miquido.stravapoc.presentation.navigation.AppRoute.RouteDetail
import com.miquido.stravapoc.presentation.navigation.AppRoute.RouteList
import com.miquido.stravapoc.presentation.routedetail.RouteDetailScreen
import com.miquido.stravapoc.presentation.routelist.RouteListScreen

@Composable
internal fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = AppRoute.Home) {

        composable<AppRoute.Home> {
            HomeScreen(
                onNavigateToRouteList = { activityType ->
                    navController.navigate(RouteList(activityType))
                },
                onNavigateToHistoryDetail = { workoutId ->
                    navController.navigate(HistoryDetail(workoutId))
                }
            )
        }

        composable<RouteList> {
            RouteListScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { routeId ->
                    navController.navigate(RouteDetail(routeId))
                }
            )
        }

        composable<RouteDetail> {
            RouteDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<HistoryDetail> {
            HistoryDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
