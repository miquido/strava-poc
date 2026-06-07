package com.miquido.stravapoc.presentation.activitytype

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miquido.stravapoc.R
import com.miquido.stravapoc.library.data.model.ActivityType

@Composable
fun ActivityTypeContent(
    onNavigateToRouteList: (ActivityType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityTypeViewModel = viewModel(factory = ActivityTypeViewModel.Factory)
) {
    val state by viewModel.viewState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.collectSideEffect<ActivityTypeSideEffect>(this) { effect ->
            when (effect) {
                is ActivityTypeSideEffect.NavigateToRouteList ->
                    onNavigateToRouteList(effect.activityType)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        state.activityTypes.forEach { activityType ->
            ActivityTypeCard(
                activityType = activityType,
                onClick = { viewModel.onActivitySelected(activityType) }
            )
        }
    }
}

@Composable
internal fun ActivityTypeCard(
    activityType: ActivityType,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = activityType.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(activityType.displayNameRes),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

internal val ActivityType.displayNameRes: Int
    get() = when (this) {
        ActivityType.RUNNING -> R.string.activity_running
        ActivityType.CYCLING -> R.string.activity_cycling
        ActivityType.WALKING -> R.string.activity_walking
    }

internal val ActivityType.icon: ImageVector
    get() = when (this) {
        ActivityType.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
        ActivityType.CYCLING -> Icons.Filled.PedalBike
        ActivityType.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
    }
