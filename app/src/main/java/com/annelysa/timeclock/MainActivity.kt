package com.annelysa.timeclock

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TimeClockTheme {
                val viewModel: TimeClockViewModel = viewModel()
                val state by viewModel.uiState.collectAsState()

                TimeClockScreen(
                    state = state,
                    onClockIn = viewModel::clockIn,
                    onClockOut = viewModel::clockOut,
                )
            }
        }
    }
}

data class TimeClockUiState(
    val isClockedIn: Boolean = false,
    val clockInTime: Instant? = null,
    val activeDuration: Duration = Duration.ZERO,
    val lastCompletedSession: WorkSession? = null,
)

data class WorkSession(
    val clockIn: Instant,
    val clockOut: Instant,
) {
    val duration: Duration = Duration.between(clockIn, clockOut).coerceAtLeast(Duration.ZERO)
}

class TimeClockViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(loadInitialState())

    val uiState: StateFlow<TimeClockUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                refreshActiveDuration()
                delay(1_000)
            }
        }
    }

    fun clockIn() {
        if (_uiState.value.isClockedIn) return

        val now = Instant.now()
        preferences.edit()
            .putLong(KEY_ACTIVE_CLOCK_IN, now.toEpochMilli())
            .apply()

        _uiState.value = _uiState.value.copy(
            isClockedIn = true,
            clockInTime = now,
            activeDuration = Duration.ZERO,
        )
    }

    fun clockOut() {
        val startedAt = _uiState.value.clockInTime ?: return
        val endedAt = Instant.now()
        val session = WorkSession(startedAt, endedAt)

        preferences.edit()
            .remove(KEY_ACTIVE_CLOCK_IN)
            .putLong(KEY_LAST_CLOCK_IN, startedAt.toEpochMilli())
            .putLong(KEY_LAST_CLOCK_OUT, endedAt.toEpochMilli())
            .apply()

        _uiState.value = _uiState.value.copy(
            isClockedIn = false,
            clockInTime = null,
            activeDuration = Duration.ZERO,
            lastCompletedSession = session,
        )
    }

    private fun refreshActiveDuration() {
        val startedAt = _uiState.value.clockInTime ?: return
        _uiState.value = _uiState.value.copy(
            activeDuration = Duration.between(startedAt, Instant.now()).coerceAtLeast(Duration.ZERO),
        )
    }

    private fun loadInitialState(): TimeClockUiState {
        val activeClockIn = preferences.getLongOrNull(KEY_ACTIVE_CLOCK_IN)?.let(Instant::ofEpochMilli)
        val lastClockIn = preferences.getLongOrNull(KEY_LAST_CLOCK_IN)?.let(Instant::ofEpochMilli)
        val lastClockOut = preferences.getLongOrNull(KEY_LAST_CLOCK_OUT)?.let(Instant::ofEpochMilli)
        val lastSession = if (lastClockIn != null && lastClockOut != null) {
            WorkSession(lastClockIn, lastClockOut)
        } else {
            null
        }

        return TimeClockUiState(
            isClockedIn = activeClockIn != null,
            clockInTime = activeClockIn,
            activeDuration = activeClockIn?.let {
                Duration.between(it, Instant.now()).coerceAtLeast(Duration.ZERO)
            } ?: Duration.ZERO,
            lastCompletedSession = lastSession,
        )
    }

    private fun android.content.SharedPreferences.getLongOrNull(key: String): Long? {
        return if (contains(key)) getLong(key, 0L) else null
    }

    private companion object {
        const val PREFS_NAME = "time_clock_preferences"
        const val KEY_ACTIVE_CLOCK_IN = "active_clock_in"
        const val KEY_LAST_CLOCK_IN = "last_clock_in"
        const val KEY_LAST_CLOCK_OUT = "last_clock_out"
    }
}

@Composable
fun TimeClockScreen(
    state: TimeClockUiState,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Time Clock",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = if (state.isClockedIn) "You are currently clocked in" else "Ready for your next shift",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (state.isClockedIn) formatDuration(state.activeDuration) else "00:00:00",
                    fontSize = 56.sp,
                    lineHeight = 62.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.clockInTime?.let { "Clocked in at ${formatTime(it)}" }
                        ?: "No active work session",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LastSessionCard(session = state.lastCompletedSession)

                Button(
                    onClick = if (state.isClockedIn) onClockOut else onClockIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isClockedIn) {
                            Color(0xFFB42318)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    ),
                ) {
                    Text(
                        text = if (state.isClockedIn) "Clock Out" else "Clock In",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun LastSessionCard(session: WorkSession?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFF6F4),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Last completed session",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (session == null) {
                Text(
                    text = "No completed sessions yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TimeStamp(label = "In", value = formatTime(session.clockIn))
                    Spacer(modifier = Modifier.weight(0.15f))
                    TimeStamp(label = "Out", value = formatTime(session.clockOut))
                    Spacer(modifier = Modifier.weight(0.15f))
                    TimeStamp(label = "Total", value = formatDuration(session.duration))
                }
            }
        }
    }
}

@Composable
private fun TimeStamp(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun TimeClockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(0xFF0F766E),
            background = Color(0xFFF8FAFC),
            surface = Color.White,
            onBackground = Color(0xFF111827),
            onSurfaceVariant = Color(0xFF4B5563),
        ),
        content = content,
    )
}

private fun formatDuration(duration: Duration): String {
    val seconds = duration.seconds.coerceAtLeast(0)
    val hours = seconds / 3_600
    val minutes = (seconds % 3_600) / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, remainingSeconds)
}

private fun formatTime(instant: Instant): String {
    return TIME_FORMATTER.format(instant.atZone(ZoneId.systemDefault()))
}

private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

@Preview(showBackground = true)
@Composable
private fun ClockedOutPreview() {
    TimeClockTheme {
        TimeClockScreen(
            state = TimeClockUiState(),
            onClockIn = {},
            onClockOut = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ClockedInPreview() {
    TimeClockTheme {
        TimeClockScreen(
            state = TimeClockUiState(
                isClockedIn = true,
                clockInTime = Instant.now().minus(Duration.ofMinutes(42)),
                activeDuration = Duration.ofMinutes(42),
            ),
            onClockIn = {},
            onClockOut = {},
        )
    }
}
