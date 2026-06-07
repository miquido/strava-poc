package com.miquido.stravapoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.presentation.historydetail.HistoryDetailScreen
import com.miquido.stravapoc.presentation.home.HomeScreen
import com.miquido.stravapoc.presentation.routedetail.RouteDetailScreen
import com.miquido.stravapoc.presentation.routelist.RouteListScreen
import com.miquido.stravapoc.ui.theme.StravaPocTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StravaPocTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onNavigateToRouteList = { activityType ->
                                navController.navigate("route_list/${activityType.name}")
                            },
                            onNavigateToHistoryDetail = { workoutId ->
                                navController.navigate("history_detail/$workoutId")
                            }
                        )
                    }
                    composable("route_list/{activityType}") { backStackEntry ->
                        val typeName = backStackEntry.arguments?.getString("activityType")
                            ?: ActivityType.RUNNING.name
                        val activityType = runCatching { ActivityType.valueOf(typeName) }
                            .getOrDefault(ActivityType.RUNNING)
                        RouteListScreen(
                            activityType = activityType,
                            onBack = { navController.popBackStack() },
                            onNavigateToDetail = { routeId ->
                                navController.navigate("route_detail/$routeId")
                            }
                        )
                    }
                    composable("route_detail/{routeId}") { backStackEntry ->
                        val routeId = backStackEntry.arguments?.getString("routeId")
                            ?: return@composable
                        RouteDetailScreen(
                            routeId = routeId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("history_detail/{workoutId}") { backStackEntry ->
                        val workoutId = backStackEntry.arguments?.getString("workoutId")
                            ?.toLongOrNull() ?: return@composable
                        HistoryDetailScreen(
                            workoutId = workoutId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
