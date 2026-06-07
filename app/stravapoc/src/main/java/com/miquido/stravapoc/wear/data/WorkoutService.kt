package com.miquido.stravapoc.wear.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.miquido.stravapoc.library.data.model.WorkoutResult
import com.miquido.stravapoc.wear.R
import com.miquido.stravapoc.wear.di.WearAppModule
import com.miquido.stravapoc.wear.presentation.MainActivity
import com.miquido.stravapoc.wear.presentation.workout.WorkoutViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): WorkoutService = this@WorkoutService
    }

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null
    private var currentRouteId: String = ""

    private val _state = MutableStateFlow<WorkoutViewState>(WorkoutViewState.Idle)
    val state: StateFlow<WorkoutViewState> = _state.asStateFlow()

    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StravaPoc::WorkoutWakeLock")
            .apply { setReferenceCounted(false) }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val routeId = intent?.getStringExtra(EXTRA_ROUTE_ID)
        Log.d(TAG, "onStartCommand routeId=$routeId state=${_state.value::class.simpleName}")
        if (routeId != null && _state.value is WorkoutViewState.Idle) {
            currentRouteId = routeId
            startForegroundWithNotification()
            launchWorkout(routeId)
        }
        return START_STICKY
    }

    private fun launchWorkout(routeId: String) {
        serviceScope.launch {
            WearAppModule.getRouteByIdUseCase(routeId)
                .onSuccess { route ->
                    _state.value = WorkoutViewState.Active(routePoints = route.points)
                    wakeLock.acquire(4 * 60 * 60 * 1000L)
                    startTimer()
                    updateNotification()
                }
        }
    }

    fun pause() {
        timerJob?.cancel()
        val active = _state.value as? WorkoutViewState.Active ?: return
        _state.value = WorkoutViewState.Paused(active)
        updateNotification()
    }

    fun resume() {
        val paused = _state.value as? WorkoutViewState.Paused ?: return
        _state.value = paused.snapshot
        startTimer()
        updateNotification()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    fun lap() {
        val active = _state.value as? WorkoutViewState.Active ?: return
        _state.value = active.copy(lapNumber = active.lapNumber + 1, lapDistanceKm = 0.0)
    }

    fun finish() {
        timerJob?.cancel()
        val active = _state.value as? WorkoutViewState.Active
            ?: (_state.value as? WorkoutViewState.Paused)?.snapshot
            ?: return

        _state.value = WorkoutViewState.Finished(
            totalDistanceKm = active.distanceKm,
            totalSeconds = active.elapsedSeconds,
            laps = active.lapNumber,
            routeId = currentRouteId
        )

        serviceScope.launch {
            WearAppModule.workoutResultSender(applicationContext).sendResult(
                WorkoutResult(
                    routeId = currentRouteId,
                    totalDistanceKm = active.distanceKm,
                    totalDurationSeconds = active.elapsedSeconds,
                    laps = active.lapNumber
                )
            )
        }

        if (wakeLock.isHeld) wakeLock.release()
        // Remove foreground + OngoingActivity indicator from watch face
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            // 1. Pobieramy obecny stan (ile już sekund upłynęło, ważne w przypadku powrotu z pauzy)
            val active = _state.value as? WorkoutViewState.Active ?: return@launch

            // 2. Obliczamy teoretyczny czas startu tego segmentu treningu oparty na zegarze urządzenia
            val startRealtime = android.os.SystemClock.elapsedRealtime() - (active.elapsedSeconds * 1000L)

            while (true) {
                delay(1000)

                // 3. Obliczamy całkowity czas w sekundach, jaki minął od naszego startRealtime
                val currentRealtime = android.os.SystemClock.elapsedRealtime()
                val totalElapsedSeconds = (currentRealtime - startRealtime) / 1000L

                // Przekazujemy dokładny czas do tick()
                tick(totalElapsedSeconds)
            }
        }
    }

    private fun tick(exactElapsedSeconds: Long) {
        val active = _state.value as? WorkoutViewState.Active ?: return

        // Sprawdzamy ile faktycznie sekund przybyło od ostatniego wywołania (zazwyczaj 1, ale eliminuje to błędy opóźnień)
        val deltaSeconds = exactElapsedSeconds - active.elapsedSeconds
        if (deltaSeconds <= 0L) return

        // Obliczamy nowy dystans proporcjonalnie do faktycznego upływu czasu
        // (10.0 km/h przeliczone na dystans pokonany w czasie deltaSeconds)
        val addedDistance = deltaSeconds * (10.0 / 3600.0)
        val newDistance = active.distanceKm + addedDistance

        val newIndex = minOf(active.routePointIndex + 1, active.routePoints.lastIndex)

        _state.value = active.copy(
            elapsedSeconds = exactElapsedSeconds,
            distanceKm = newDistance,
            pacePerKm = formatPace(newDistance, exactElapsedSeconds),
            routePointIndex = newIndex,
            lapDistanceKm = active.lapDistanceKm + addedDistance
        )

        // Zgodnie z wcześniejszymi zmianami usunęliśmy stąd updateNotification() !
    }

    private fun formatPace(distanceKm: Double, seconds: Long): String {
        if (distanceKm <= 0.0) return "--:--"
        val paceSeconds = (seconds / distanceKm).toLong()
        return "%d:%02d".format(paceSeconds / 60, paceSeconds % 60)
    }

    private fun startForegroundWithNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Aktywny trening",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )    }


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

        // Wyciągamy dane niezależnie czy to Active, czy zapisany stan w Paused
        val activeData = when (currentState) {
            is WorkoutViewState.Active -> currentState
            is WorkoutViewState.Paused -> currentState.snapshot
            else -> null
        }

        val elapsedSeconds = activeData?.elapsedSeconds ?: 0L
        val elapsedMillis = elapsedSeconds * 1000L

        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_run)
            // Zmieniamy tytuł dla lepszego UX
            .setContentTitle(if (isPaused) "Trening wstrzymany" else "Trening w toku")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)

        // Pobranie aktualnego czasu trwania z StateFlow, aby stoper wiedział od kiedy liczyć
//        val elapsedMillis = (activeState?.elapsedSeconds ?: 0L) * 1000L
//        val startTimeMillis = SystemClock.elapsedRealtime() - elapsedMillis

        // Tworzenie statusu widocznego na tarczy zegarka (np. 00:15)
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

            notificationBuilder.setContentText("Czas: $staticTimeText")

            // 3. Status na tarczy zegarka to teraz zwykły tekst, a nie stoper
            statusBuilder.addTemplate("#time#")
                .addPart("time", Status.TextPart(staticTimeText))

        } else {
            val notificationBaseTime = System.currentTimeMillis() - elapsedMillis
            val ongoingActivityBaseTime = android.os.SystemClock.elapsedRealtime() - elapsedMillis

            // 1. Włączamy systemowy stoper
            notificationBuilder.setUsesChronometer(true)
            notificationBuilder.setWhen(notificationBaseTime)

            // 2. Status na tarczy zegarka to automatyczny stoper
            statusBuilder.addTemplate("#time#")
                .addPart("time", Status.StopwatchPart(ongoingActivityBaseTime))

        }

        val ongoingActivity =
            OngoingActivity.Builder(applicationContext, NOTIFICATION_ID, notificationBuilder)
                .setStaticIcon(R.drawable.ic_run)
                .setTouchIntent(pendingIntent)
                .setStatus(statusBuilder.build()) // Wpięcie statusu!
                .build()

        ongoingActivity.apply(applicationContext)

        return notificationBuilder.build()
    }

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope.cancel()
        if (wakeLock.isHeld) wakeLock.release()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "WorkoutService"
        const val ACTION_START = "com.miquido.stravapoc.WORKOUT_START"
        const val EXTRA_ROUTE_ID = "extra_route_id"
        private const val CHANNEL_ID = "workout_channel_v2"
        private const val NOTIFICATION_ID = 2001
    }
}
