package com.miquido.stravapoc.wear.presentation.routelist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.wear.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.wear.presentation.activityselection.wearIcon
import com.miquido.stravapoc.wear.presentation.activityselection.wearLabel
import kotlinx.coroutines.flow.filterIsInstance

@Composable
fun WearRouteListScreen(
    activityType: ActivityType,
    onNavigateToWorkout: (routeId: String, activityType: ActivityType) -> Unit,
    viewModel: WearRouteListViewModel = viewModel(
        factory = WearRouteListViewModel.factory(activityType, LocalContext.current),
        key = activityType.name
    )
) {
    val state by viewModel.viewState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.filterIsInstance<WearRouteListSideEffect>().collect { effect ->
            when (effect) {
                is WearRouteListSideEffect.NavigateToWorkout ->
                    onNavigateToWorkout(effect.routeId, effect.activityType)
            }
        }
    }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 44.dp, end = 20.dp, bottom = 44.dp),
        autoCentering = null,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = activityType.wearIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = activityType.wearLabel,
                    style = MaterialTheme.typography.title3
                )
            }
        }

        // Custom Activity — zawsze widoczny, niezależnie od stanu ładowania
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.onCustomActivitySelected() },
                colors = ChipDefaults.primaryChipColors(),
                label = {
                    Text(
                        text = "Custom Activity",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            )
        }

        if (state.error != null) {
            item {
                Text(
                    text = state.error!!,
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.error
                )
            }
        } else if (!state.isLoading) {
            items(state.routes) { route ->
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.onRouteSelected(route.id, route.activityType) },
                    colors = ChipDefaults.secondaryChipColors(),
                    label = {
                        Text(
                            text = route.name,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = "%.1f km".format(route.distanceKm),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                )
            }
        }
    }
}
