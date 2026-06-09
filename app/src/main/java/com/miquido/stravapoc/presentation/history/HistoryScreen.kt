package com.miquido.stravapoc.presentation.history

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.miquido.stravapoc.R
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.WorkoutResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

@Composable
internal fun HistoryRoute(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.viewState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.filterIsInstance<HistorySideEffect>().collect { effect ->
            when (effect) {
                is HistorySideEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
            }
        }
    }

    HistoryScreen(
        state = state,
        onItemSelected = viewModel::onItemSelected,
        onItemDeleted = viewModel::onDeleteItem
    )
}

@Composable
private fun HistoryScreen(
    state: HistoryViewState,
    onItemSelected: (Long) -> Unit,
    onItemDeleted: (Long) -> Unit
) {
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }

    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onItemDeleted(pendingDeleteId!!)
                    pendingDeleteId = null
                }) {
                    Text(
                        text = stringResource(R.string.delete_dialog_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text(stringResource(R.string.delete_dialog_cancel))
                }
            }
        )
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
            else -> {
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current
                val peekPx = remember(configuration.screenWidthDp) {
                    with(density) { (configuration.screenWidthDp / 4).dp.toPx() }
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        SwipeToReveal(
                            peekPx = peekPx,
                            isPeeked = pendingDeleteId == item.id,
                            onRevealed = { pendingDeleteId = item.id }
                        ) {
                            HistoryItem(
                                item = item,
                                onClick = { onItemSelected(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeToReveal(
    peekPx: Float,
    isPeeked: Boolean,
    onRevealed: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(isPeeked) {
        if (!isPeeked) {
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFFC4C02), shape = CardDefaults.shape)
                .padding(end = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.cd_delete),
                tint = Color.Black
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = animatable.value }
                .pointerInput(isPeeked) {
                    if (isPeeked) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (animatable.value < -peekPx * 0.4f) {
                                    animatable.animateTo(
                                        targetValue = -peekPx,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    )
                                    onRevealed()
                                } else {
                                    animatable.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                animatable.animateTo(0f, spring())
                            }
                        },
                        onHorizontalDrag = { _, delta ->
                            scope.launch {
                                animatable.snapTo(
                                    (animatable.value + delta).coerceIn(-peekPx, 0f)
                                )
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

@Composable
private fun HistoryItem(item: WorkoutResult, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
            ) {
                drawRect(color = Color(0xFFFC4C02))
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
                    MetricLabel(
                        value = formatDuration(item.totalDurationSeconds),
                        label = stringResource(R.string.metric_time)
                    )
                    MetricLabel(
                        value = formatPace(item.totalDistanceKm, item.totalDurationSeconds),
                        label = stringResource(R.string.metric_pace_unit)
                    )
                    MetricLabel(
                        value = item.laps.toString(),
                        label = stringResource(R.string.metric_laps)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricLabel(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

private fun ActivityType.toDisplayName(): String = when (this) {
    ActivityType.RUNNING -> "Running"
    ActivityType.CYCLING -> "Cycling"
    ActivityType.WALKING -> "Walking"
}
