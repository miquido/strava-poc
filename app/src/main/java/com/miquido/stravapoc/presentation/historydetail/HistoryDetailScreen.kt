package com.miquido.stravapoc.presentation.historydetail

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miquido.stravapoc.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    workoutId: Long,
    onBack: () -> Unit,
    viewModel: HistoryDetailViewModel = viewModel(
        factory = HistoryDetailViewModel.factory(
            workoutId,
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val state by viewModel.viewState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.entity?.routeName ?: stringResource(R.string.history_detail_fallback_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    text = state.error!!,
                    modifier = Modifier.align(Alignment.Center)
                )
                state.entity != null -> {
                    val entity = state.entity!!
                    val points = state.route?.points?.map { LatLng(it.lat, it.lng) } ?: emptyList()
                    val center = points.firstOrNull() ?: LatLng(52.0, 21.0)
                    val cameraState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(center, 14f)
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Data + badge
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDateTime(entity.timestamp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = entity.activityType.toDisplayName(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Mapa
                        GoogleMap(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            cameraPositionState = cameraState
                        ) {
                            if (points.size >= 2) {
                                Polyline(points = points, width = 8f)
                            }
                        }

                        // Metryki 2x2
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricTile(
                                label = stringResource(R.string.metric_distance),
                                value = "%.1f".format(entity.totalDistanceKm),
                                unit = stringResource(R.string.unit_km),
                                modifier = Modifier.weight(1f)
                            )
                            MetricTile(
                                label = stringResource(R.string.metric_duration),
                                value = formatDuration(entity.totalDurationSeconds),
                                unit = stringResource(R.string.unit_mmss),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricTile(
                                label = stringResource(R.string.metric_pace),
                                value = formatPace(entity.totalDistanceKm, entity.totalDurationSeconds),
                                unit = stringResource(R.string.unit_min_km),
                                modifier = Modifier.weight(1f)
                            )
                            MetricTile(
                                label = stringResource(R.string.metric_laps_label),
                                value = entity.laps.toString(),
                                unit = stringResource(R.string.unit_laps),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%d:%02d:%02d".format(h, m, s)
}

private fun formatPace(distanceKm: Double, durationSeconds: Long): String {
    if (distanceKm == 0.0) return "--:--"
    val paceSeconds = (durationSeconds / distanceKm).toLong()
    val m = paceSeconds / 60
    val s = paceSeconds % 60
    return "%d:%02d".format(m, s)
}

private fun String.toDisplayName(): String = when (this) {
    "RUNNING" -> "Running"
    "CYCLING" -> "Cycling"
    "WALKING" -> "Walking"
    else -> this
}
