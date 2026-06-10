package com.miquido.stravapoc.wear.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.RoutePoint
import com.miquido.stravapoc.library.usecase.GetWearRouteByIdUseCase
import com.miquido.stravapoc.wear.R
import com.miquido.stravapoc.wear.data.location.MockLocationSource
import com.miquido.stravapoc.wear.data.location.RealLocationSource
import com.miquido.stravapoc.wear.data.location.haversineDistanceKm
import com.miquido.stravapoc.wear.data.location.trackingConstraints
import com.miquido.stravapoc.wear.presentation.MainActivity
import com.miquido.stravapoc.wear.presentation.workout.WorkoutViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutService : Service() {

    @Inject
    lateinit var getRouteByIdUseCase: GetWearRouteByIdUseCase

    @Inject
    lateinit var workoutResultHolder: WorkoutResultHolder

    @Inject
    lateinit var workoutPreferences: WorkoutPreferences

    inner class LocalBinder : Binder() {
        fun getService(): WorkoutService = this@WorkoutService
    }

    private val binder = LocalBinder()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Unhandled exception in WorkoutService coroutine", throwable)
    }
    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default + exceptionHandler)
    private var timerJob: Job? = null
    private var locationJob: Job? = null
    private var currentRouteId: String = ""

    private val _state = MutableStateFlow<WorkoutViewState>(WorkoutViewState.Idle)
    val state: StateFlow<WorkoutViewState> = _state.asStateFlow()

    private var distanceBaseLocation: RoutePoint? = null
    private var currentActivityType: ActivityType = ActivityType.RUNNING

    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StravaPoc::WorkoutWakeLock")
            .apply { setReferenceCounted(false) }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val routeId = intent?.getStringExtra(EXTRA_ROUTE_ID)
        Log.d(TAG, "onStartCommand routeId=$routeId state=${_state.value::class.simpleName}")
        if (routeId != null && routeId.isNotEmpty() && _state.value is WorkoutViewState.Idle) {
            val activityTypeName = intent.getStringExtra(EXTRA_ACTIVITY_TYPE)
            currentActivityType = activityTypeName
                ?.let { runCatching { ActivityType.valueOf(it) }.getOrNull() }
                ?: ActivityType.RUNNING
            distanceBaseLocation = null
            currentRouteId = routeId
            workoutPreferences.save(routeId)
            startForegroundWithNotification()
            launchWorkout(routeId)
        }
        return START_STICKY
    }

    private fun launchWorkout(routeId: String) {
        serviceScope.launch {
            if (routeId == CUSTOM_ROUTE_ID) {
                val activeState = WorkoutViewState.Active(routePoints = emptyList())
                _state.value = activeState
                wakeLock.acquire(4 * 60 * 60 * 1000L)
                startTimer()
                startLocationUpdates(routeId, initialProgressKm = 0.0, routePoints = emptyList())
                updateNotification()
            } else {
                getRouteByIdUseCase(routeId)
                    .onSuccess { route ->
                        currentActivityType = route.activityType
                        distanceBaseLocation = null
                        val activeState = WorkoutViewState.Active(routePoints = route.points)
                        _state.value = activeState
                        wakeLock.acquire(4 * 60 * 60 * 1000L)
                        startTimer()
                        startLocationUpdates(
                            routeId,
                            initialProgressKm = 0.0,
                            routePoints = route.points
                        )
                        updateNotification()
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Route not found: $routeId", e)
                        val activeState = WorkoutViewState.Active(routePoints = emptyList())
                        _state.value = activeState
                        wakeLock.acquire(4 * 60 * 60 * 1000L)
                        startTimer()
                        startLocationUpdates(
                            routeId,
                            initialProgressKm = 0.0,
                            routePoints = emptyList()
                        )
                        updateNotification()
                    }
            }
        }
    }

    private fun startLocationUpdates(
        routeId: String,
        initialProgressKm: Double,
        routePoints: List<RoutePoint>
    ) {
        locationJob?.cancel()
        val source = if (routeId == CUSTOM_ROUTE_ID) {
            RealLocationSource(applicationContext)
        } else {
            MockLocationSource(
                routePoints = routePoints,
                speedKmh = currentActivityType.trackingConstraints.mockSpeedKmh,
                initialProgressKm = initialProgressKm
            )
        }
        locationJob = serviceScope.launch {
            runCatching {
                source.locations.collect { point ->
                    updateFromLocation(point)
                }
            }.onFailure { e ->
                Log.e(TAG, "Location collection failed", e)
            }
        }
    }

    private fun updateFromLocation(point: RoutePoint) {
        _state.update { current ->
            val active = current as? WorkoutViewState.Active ?: return@update current
            val constraints = currentActivityType.trackingConstraints

            val delta = distanceBaseLocation?.let { haversineDistanceKm(it, point) } ?: 0.0
            if (delta.isNaN() || delta.isInfinite() || delta > constraints.maxDeltaKm) return@update current

            val effectiveDelta = if (delta < constraints.jitterKm) 0.0 else delta
            if (distanceBaseLocation == null || effectiveDelta > 0.0) {
                distanceBaseLocation = point
            }

            val newDistance = active.distanceKm + effectiveDelta
            active.copy(
                distanceKm = newDistance,
                lapDistanceKm = active.lapDistanceKm + effectiveDelta,
                pacePerKm = formatPace(newDistance, active.elapsedSeconds),
                currentLocation = point,
                trackedPoints = active.trackedPoints + point
            )
        }
    }

    fun pause() {
        timerJob?.cancel()
        locationJob?.cancel()
        val active = _state.value as? WorkoutViewState.Active ?: return
        _state.value = WorkoutViewState.Paused(active)
        updateNotification()
    }

    fun resume() {
        val paused = _state.value as? WorkoutViewState.Paused ?: return
        distanceBaseLocation = paused.snapshot.currentLocation
        _state.value = paused.snapshot
        startTimer()
        startLocationUpdates(
            routeId = currentRouteId,
            initialProgressKm = paused.snapshot.distanceKm,
            routePoints = paused.snapshot.routePoints
        )
        updateNotification()
    }

    fun lap() {
        val active = _state.value as? WorkoutViewState.Active ?: return
        _state.value = active.copy(lapNumber = active.lapNumber + 1, lapDistanceKm = 0.0)
    }

    fun finish() {
        timerJob?.cancel()
        locationJob?.cancel()
        val active = _state.value as? WorkoutViewState.Active
            ?: (_state.value as? WorkoutViewState.Paused)?.snapshot
            ?: return

        val raw = active.trackedPoints
        val step = maxOf(1, raw.size / 2000)
        workoutResultHolder.pendingTrackedPoints = raw.filterIndexed { i, _ -> i % step == 0 }

        _state.value = WorkoutViewState.Finished(
            totalDistanceKm = active.distanceKm,
            totalSeconds = active.elapsedSeconds,
            laps = active.lapNumber,
            routeId = currentRouteId
        )

        workoutPreferences.clear()
        if (wakeLock.isHeld) wakeLock.release()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            val active = _state.value as? WorkoutViewState.Active ?: return@launch
            val startRealtime =
                android.os.SystemClock.elapsedRealtime() - (active.elapsedSeconds * 1000L)

            while (true) {
                delay(1000)
                val totalElapsedSeconds =
                    (android.os.SystemClock.elapsedRealtime() - startRealtime) / 1000L
                _state.update { current ->
                    val a = current as? WorkoutViewState.Active ?: return@update current
                    a.copy(
                        elapsedSeconds = totalElapsedSeconds,
                        pacePerKm = formatPace(a.distanceKm, totalElapsedSeconds)
                    )
                }
            }
        }
    }

    private fun formatPace(distanceKm: Double, seconds: Long): String {
        if (distanceKm <= 0.0) return "--:--"
        val paceSeconds = (seconds / distanceKm).toLong()
        return "%d:%02d".format(paceSeconds / 60, paceSeconds % 60)
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun startForegroundWithNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Active workout",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
    }

    private fun buildNotification(): android.app.Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val currentState = _state.value
        val isPaused = currentState is WorkoutViewState.Paused

        val activeData = when (currentState) {
            is WorkoutViewState.Active -> currentState
            is WorkoutViewState.Paused -> currentState.snapshot
            else -> null
        }

        val elapsedSeconds = activeData?.elapsedSeconds ?: 0L
        val elapsedMillis = elapsedSeconds * 1000L

        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_run)
            .setContentTitle(if (isPaused) "Workout paused" else "Workout in progress")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)

        val statusBuilder = Status.Builder()

        if (isPaused) {
            notificationBuilder.setUsesChronometer(false)

            val hours = elapsedSeconds / 3600
            val minutes = (elapsedSeconds % 3600) / 60
            val seconds = elapsedSeconds % 60
            val staticTimeText = if (hours > 0) {
                String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
            }

            notificationBuilder.setContentText("Time: $staticTimeText")
            statusBuilder.addTemplate("#time#").addPart("time", Status.TextPart(staticTimeText))
        } else {
            val notificationBaseTime = System.currentTimeMillis() - elapsedMillis
            val ongoingActivityBaseTime = android.os.SystemClock.elapsedRealtime() - elapsedMillis

            notificationBuilder.setUsesChronometer(true)
            notificationBuilder.setWhen(notificationBaseTime)
            statusBuilder.addTemplate("#time#")
                .addPart("time", Status.StopwatchPart(ongoingActivityBaseTime))
        }

        val ongoingActivity =
            OngoingActivity.Builder(applicationContext, NOTIFICATION_ID, notificationBuilder)
                .setStaticIcon(R.drawable.ic_run)
                .setTouchIntent(pendingIntent)
                .setStatus(statusBuilder.build())
                .build()

        ongoingActivity.apply(applicationContext)

        return notificationBuilder.build()
    }

    override fun onDestroy() {
        timerJob?.cancel()
        locationJob?.cancel()
        serviceScope.cancel()
        if (wakeLock.isHeld) wakeLock.release()
        workoutPreferences.clear()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "WorkoutService"
        const val ACTION_START = "com.miquido.stravapoc.WORKOUT_START"
        const val EXTRA_ROUTE_ID = "extra_route_id"
        const val EXTRA_ACTIVITY_TYPE = "extra_activity_type"
        private const val CHANNEL_ID = "workout_channel_v2"
        private const val NOTIFICATION_ID = 2001
    }
}
