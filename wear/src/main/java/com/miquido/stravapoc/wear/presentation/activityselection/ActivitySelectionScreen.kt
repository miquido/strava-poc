package com.miquido.stravapoc.wear.presentation.activityselection

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun ActivitySelectionScreen(
    onNavigateToWorkout: (routeId: String) -> Unit,
    viewModel: ActivitySelectionViewModel = viewModel(
        factory = ActivitySelectionViewModel.factory(
            (LocalContext.current.applicationContext as Application)
        )
    )
) {
    val state by viewModel.viewState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.collectSideEffect<ActivitySelectionSideEffect>(this) { effect ->
            when (effect) {
                is ActivitySelectionSideEffect.NavigateToWorkout ->
                    onNavigateToWorkout(effect.routeId)
            }
        }
    }

    ScalingLazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Wybierz aktywność",
                style = MaterialTheme.typography.title3
            )
        }

        if (state.receivedRouteName != null) {
            item {
                Text(
                    text = "📍 ${state.receivedRouteName}",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.primary
                )
            }
        }

        items(state.activities) { activity ->
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.onActivitySelected(activity.id) },
                colors = ChipDefaults.primaryChipColors(),
                label = { Text("${activity.emoji} ${activity.label}") }
            )
        }
    }
}
