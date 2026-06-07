package com.miquido.stravapoc.presentation.history

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miquido.stravapoc.R
import com.miquido.stravapoc.data.db.WorkoutResultEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryContent(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.factory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val state by viewModel.viewState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.collectSideEffect<HistorySideEffect>(this) { effect ->
            when (effect) {
                is HistorySideEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            state.items.isEmpty() -> Text(
                text = stringResource(R.string.history_empty),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    HistoryItem(item = item, onClick = { viewModel.onItemSelected(item.id) })
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(item: WorkoutResultEntity, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
            ) {
                drawRect(color = Color(0xFFE8161B))
            }

            Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = item.routeName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${formatDate(item.timestamp)} • ${item.activityType.toDisplayName()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "%.1f km".format(item.totalDistanceKm),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    MetricLabel(value = formatDuration(item.totalDurationSeconds), label = stringResource(R.string.metric_time))
                    MetricLabel(value = formatPace(item.totalDistanceKm, item.totalDurationSeconds), label = stringResource(R.string.metric_pace_unit))
                    MetricLabel(value = item.laps.toString(), label = stringResource(R.string.metric_laps))
                }
            }
        }
    }
}

@Composable
private fun MetricLabel(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))

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
