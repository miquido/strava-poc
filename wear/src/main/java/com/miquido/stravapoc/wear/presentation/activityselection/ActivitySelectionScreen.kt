package com.miquido.stravapoc.wear.presentation.activityselection

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.miquido.stravapoc.library.data.model.ActivityType
import kotlinx.coroutines.flow.filterIsInstance

@Composable
fun ActivitySelectionScreen(
    onNavigateToRouteList: (ActivityType) -> Unit,
    viewModel: ActivitySelectionViewModel = hiltViewModel()
) {
    val state by viewModel.viewState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.filterIsInstance<ActivitySelectionSideEffect>().collect { effect ->
            when (effect) {
                is ActivitySelectionSideEffect.NavigateToRouteList ->
                    onNavigateToRouteList(effect.activityType)
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
            Text(
                text = "Select activity",
                style = MaterialTheme.typography.title3
            )
        }

        items(state.activities) { activity ->
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.onActivitySelected(activity) },
                colors = ChipDefaults.primaryChipColors(),
                icon = {
                    Icon(
                        imageVector = activity.wearIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = activity.wearLabel,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            )
        }
    }
}
