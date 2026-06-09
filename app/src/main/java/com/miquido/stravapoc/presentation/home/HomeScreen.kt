package com.miquido.stravapoc.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.miquido.stravapoc.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.presentation.activitytype.ActivityTypeRoute
import com.miquido.stravapoc.presentation.history.HistoryRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onNavigateToRouteList: (ActivityType) -> Unit,
    onNavigateToHistoryDetail: (Long) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
        bottomBar = {
            NavigationBar {
                val navItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                )
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_routes)) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.History, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_history)) },
                    colors = navItemColors
                )
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = selectedTab,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            label = "tab_content"
        ) { tab ->
            when (tab) {
                0 -> ActivityTypeRoute(onNavigateToRouteList = onNavigateToRouteList)
                1 -> HistoryRoute(onNavigateToDetail = onNavigateToHistoryDetail)
            }
        }
    }
}
