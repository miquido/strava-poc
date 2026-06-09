package com.miquido.stravapoc.wear.presentation.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun MetricsPage(
    state: WorkoutViewState.Active,
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onLap: () -> Unit,
    onFinish: () -> Unit
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, top = 36.dp, end = 12.dp, bottom = 44.dp),
        autoCentering = null,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- PRIORYTET 1: Czas (największy) ---
        item {
            val timeColor = if (isPaused) Color.White.copy(alpha = 0.45f) else Color.White
            MetricItemPrimary(
                label = if (isPaused) "TIME  ⏸" else "TIME",
                value = formatDuration(state.elapsedSeconds),
                valueColor = timeColor
            )
        }

        // --- PRIORYTET 2: Dystans + Tempo obok siebie ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItemSecondary(
                    label = "DISTANCE",
                    value = "%.2f".format(state.distanceKm),
                    unit = "km"
                )
                MetricItemSecondary(
                    label = "PACE",
                    value = state.pacePerKm,
                    unit = "min/km"
                )
            }
        }

        // --- PRIORYTET 3: Okrążenie (dyskretne) ---
        item {
            Text(
                text = "Lap ${state.lapNumber}  ·  ${"%.2f".format(state.lapDistanceKm)} km",
                style = MaterialTheme.typography.caption1,
                color = MaterialTheme.colors.onSurfaceVariant
            )
        }

        // --- Przyciski (pionowo, jeden pod drugim, 75% szerokości) ---
        item {
            Button(
                onClick = onPauseResume,
                modifier = Modifier.fillMaxWidth(0.75f),
                colors = ButtonDefaults.secondaryButtonColors()
            ) {
                Text(
                    text = if (isPaused) "Resume" else "Pause",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
        item {
            Button(
                onClick = onLap,
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                Text(
                    text = "Lap",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
        item {
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(0.75f),
                colors = ButtonDefaults.primaryButtonColors()
            ) {
                Text(
                    text = "Finish",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Duży — czas; dominuje wizualnie na ekranie */
@Composable
private fun MetricItemPrimary(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.caption1,
            color = MaterialTheme.colors.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/** Średni — dystans i tempo */
@Composable
private fun MetricItemSecondary(label: String, value: String, unit: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.caption1,
            color = MaterialTheme.colors.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(2.dp))
                Text(
                    text = unit,
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
