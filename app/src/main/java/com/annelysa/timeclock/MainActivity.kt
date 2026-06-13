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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
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
                    onHistoryDayToggle = viewModel::toggleHistoryDay,
                    onSettingsExpandedToggle = viewModel::toggleSettingsExpanded,
                    onExpectedDailyHoursChange = viewModel::updateExpectedDailyHours,
                    onExpectedWeeklyHoursChange = viewModel::updateExpectedWeeklyHours,
                    onWorkdayToggle = viewModel::toggleWorkday,
                    onUnpaidLunchBreakToggle = viewModel::toggleUnpaidLunchBreak,
                    onLunchBreakMinutesChange = viewModel::updateLunchBreakMinutes,
                )
            }
        }
    }
}

data class TimeClockUiState(
    val isClockedIn: Boolean = false,
    val clockInTime: Instant? = null,
    val activeDuration: Duration = Duration.ZERO,
    val todayTotalDuration: Duration = Duration.ZERO,
    val todayCreditedDuration: Duration = Duration.ZERO,
    val todayBreakDeduction: Duration = Duration.ZERO,
    val todaySessionCount: Int = 0,
    val todayFirstClockIn: Instant? = null,
    val todayLastClockOut: Instant? = null,
    val isSettingsExpanded: Boolean = false,
    val expectedDailyHoursInput: String = "7:30",
    val expectedDailyDuration: Duration = Duration.ofHours(7).plusMinutes(30),
    val expectedWeeklyHoursInput: String = "37:30",
    val expectedWeeklyDuration: Duration = Duration.ofHours(37).plusMinutes(30),
    val workDays: Set<DayOfWeek> = DEFAULT_WORK_DAYS,
    val deductUnpaidLunchBreak: Boolean = false,
    val lunchBreakMinutesInput: String = "30",
    val lunchBreakDuration: Duration = Duration.ofMinutes(30),
    val isTodayWorkday: Boolean = true,
    val todayBalanceDuration: Duration = Duration.ZERO,
    val todayProgressMessage: String = "",
    val lastCompletedSession: WorkSession? = null,
    val completedSessions: List<WorkSession> = emptyList(),
    val expandedHistoryDates: Set<LocalDate> = emptySet(),
)

data class WorkSession(
    val clockIn: Instant,
    val clockOut: Instant,
) {
    val duration: Duration = Duration.between(clockIn, clockOut).coerceAtLeast(Duration.ZERO)
}

data class WorkDayHistory(
    val date: LocalDate,
    val sessions: List<WorkSession>,
    val totalDuration: Duration,
    val expectedDuration: Duration,
) {
    val balanceDuration: Duration = totalDuration.minus(expectedDuration)
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

        _uiState.value = withDailySummary(
            _uiState.value.copy(
                isClockedIn = true,
                clockInTime = now,
                activeDuration = Duration.ZERO,
            ),
        )
    }

    fun clockOut() {
        val startedAt = _uiState.value.clockInTime ?: return
        val endedAt = Instant.now()
        val session = WorkSession(startedAt, endedAt)
        val completedSessions = (_uiState.value.completedSessions + session).sortedBy { it.clockIn }

        preferences.edit()
            .remove(KEY_ACTIVE_CLOCK_IN)
            .putString(KEY_COMPLETED_SESSIONS, encodeSessions(completedSessions))
            .remove(KEY_LAST_CLOCK_IN)
            .remove(KEY_LAST_CLOCK_OUT)
            .apply()

        _uiState.value = withDailySummary(
            _uiState.value.copy(
                isClockedIn = false,
                clockInTime = null,
                activeDuration = Duration.ZERO,
                todayLastClockOut = endedAt,
                lastCompletedSession = session,
                completedSessions = completedSessions,
            ),
        )
    }

    fun updateExpectedDailyHours(input: String) {
        val sanitizedInput = sanitizeDurationInput(input)
        val dailyDuration = sanitizedInput.toDurationOrNull()
        val weeklyDuration = dailyDuration?.multipliedBy(_uiState.value.workDays.size.toLong())

        preferences.edit()
            .putString(KEY_EXPECTED_DAILY_HOURS, sanitizedInput)
            .putString(KEY_EXPECTED_WEEKLY_HOURS, weeklyDuration?.let(::formatDurationInput) ?: _uiState.value.expectedWeeklyHoursInput)
            .apply()

        _uiState.value = withDailySummary(
            _uiState.value.copy(
                expectedDailyHoursInput = sanitizedInput,
                expectedDailyDuration = dailyDuration ?: _uiState.value.expectedDailyDuration,
                expectedWeeklyHoursInput = weeklyDuration?.let(::formatDurationInput)
                    ?: _uiState.value.expectedWeeklyHoursInput,
                expectedWeeklyDuration = weeklyDuration ?: _uiState.value.expectedWeeklyDuration,
            ),
        )
    }

    fun updateExpectedWeeklyHours(input: String) {
        val sanitizedInput = sanitizeDurationInput(input)
        val weeklyDuration = sanitizedInput.toDurationOrNull()
        val dailyDuration = weeklyDuration?.dividedByWorkdays(_uiState.value.workDays.size)

        preferences.edit()
            .putString(KEY_EXPECTED_WEEKLY_HOURS, sanitizedInput)
            .putString(KEY_EXPECTED_DAILY_HOURS, dailyDuration?.let(::formatDurationInput) ?: _uiState.value.expectedDailyHoursInput)
            .apply()

        _uiState.value = withDailySummary(
            _uiState.value.copy(
                expectedWeeklyHoursInput = sanitizedInput,
                expectedWeeklyDuration = weeklyDuration ?: _uiState.value.expectedWeeklyDuration,
                expectedDailyHoursInput = dailyDuration?.let(::formatDurationInput)
                    ?: _uiState.value.expectedDailyHoursInput,
                expectedDailyDuration = dailyDuration ?: _uiState.value.expectedDailyDuration,
            ),
        )
    }

    fun toggleWorkday(day: DayOfWeek) {
        val currentDays = _uiState.value.workDays
        val updatedDays = if (day in currentDays) {
            currentDays - day
        } else {
            currentDays + day
        }

        preferences.edit()
            .putString(KEY_WORK_DAYS, encodeWorkDays(updatedDays))
            .putString(
                KEY_EXPECTED_DAILY_HOURS,
                _uiState.value.expectedWeeklyDuration.dividedByWorkdays(updatedDays.size)
                    .let(::formatDurationInput),
            )
            .apply()

        _uiState.value = withDailySummary(
            _uiState.value.copy(
                workDays = updatedDays,
                expectedDailyDuration = _uiState.value.expectedWeeklyDuration.dividedByWorkdays(updatedDays.size),
                expectedDailyHoursInput = _uiState.value.expectedWeeklyDuration
                    .dividedByWorkdays(updatedDays.size)
                    .let(::formatDurationInput),
            ),
        )
    }

    fun toggleSettingsExpanded() {
        _uiState.value = _uiState.value.copy(
            isSettingsExpanded = !_uiState.value.isSettingsExpanded,
        )
    }

    fun toggleHistoryDay(date: LocalDate) {
        val expandedDates = _uiState.value.expandedHistoryDates
        _uiState.value = _uiState.value.copy(
            expandedHistoryDates = if (date in expandedDates) {
                expandedDates - date
            } else {
                expandedDates + date
            },
        )
    }

    fun toggleUnpaidLunchBreak(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_DEDUCT_UNPAID_LUNCH_BREAK, enabled)
            .apply()

        _uiState.value = withDailySummary(
            _uiState.value.copy(deductUnpaidLunchBreak = enabled),
        )
    }

    fun updateLunchBreakMinutes(input: String) {
        val sanitizedInput = sanitizeWholeNumberInput(input).take(3)
        val minutes = sanitizedInput.toLongOrNull()?.coerceIn(0L, 240L) ?: 0L

        preferences.edit()
            .putString(KEY_LUNCH_BREAK_MINUTES, sanitizedInput)
            .apply()

        _uiState.value = withDailySummary(
            _uiState.value.copy(
                lunchBreakMinutesInput = sanitizedInput,
                lunchBreakDuration = Duration.ofMinutes(minutes),
            ),
        )
    }

    private fun refreshActiveDuration() {
        val startedAt = _uiState.value.clockInTime
        _uiState.value = withDailySummary(
            _uiState.value.copy(
                activeDuration = startedAt?.let {
                    Duration.between(it, Instant.now()).coerceAtLeast(Duration.ZERO)
                } ?: Duration.ZERO,
            ),
        )
    }

    private fun loadInitialState(): TimeClockUiState {
        val activeClockIn = preferences.getLongOrNull(KEY_ACTIVE_CLOCK_IN)?.let(Instant::ofEpochMilli)
        val completedSessions = loadCompletedSessions()
        val expectedDailyHours = preferences.getString(KEY_EXPECTED_DAILY_HOURS, DEFAULT_DAILY_HOURS_INPUT)
            ?: DEFAULT_DAILY_HOURS_INPUT
        val expectedDailyDuration = expectedDailyHours.toDurationOrNull() ?: DEFAULT_DAILY_DURATION
        val expectedWeeklyHours = preferences.getString(KEY_EXPECTED_WEEKLY_HOURS, DEFAULT_WEEKLY_HOURS_INPUT)
            ?: DEFAULT_WEEKLY_HOURS_INPUT
        val expectedWeeklyDuration = expectedWeeklyHours.toDurationOrNull()
            ?: expectedDailyDuration.multipliedBy(DEFAULT_WORK_DAYS.size.toLong())
        val workDays = preferences.getString(KEY_WORK_DAYS, null)
            ?.let(::decodeWorkDays)
            ?: DEFAULT_WORK_DAYS
        val deductUnpaidLunchBreak = preferences.getBoolean(KEY_DEDUCT_UNPAID_LUNCH_BREAK, false)
        val lunchBreakMinutesInput = preferences.getString(
            KEY_LUNCH_BREAK_MINUTES,
            DEFAULT_LUNCH_BREAK_MINUTES_INPUT,
        ) ?: DEFAULT_LUNCH_BREAK_MINUTES_INPUT
        val lunchBreakDuration = Duration.ofMinutes(
            lunchBreakMinutesInput.toLongOrNull()?.coerceIn(0L, 240L) ?: DEFAULT_LUNCH_BREAK_MINUTES,
        )

        return withDailySummary(
            TimeClockUiState(
                isClockedIn = activeClockIn != null,
                clockInTime = activeClockIn,
                activeDuration = activeClockIn?.let {
                    Duration.between(it, Instant.now()).coerceAtLeast(Duration.ZERO)
                } ?: Duration.ZERO,
                expectedDailyHoursInput = formatDurationInput(expectedWeeklyDuration.dividedByWorkdays(workDays.size)),
                expectedDailyDuration = expectedWeeklyDuration.dividedByWorkdays(workDays.size),
                expectedWeeklyHoursInput = expectedWeeklyHours,
                expectedWeeklyDuration = expectedWeeklyDuration,
                workDays = workDays,
                deductUnpaidLunchBreak = deductUnpaidLunchBreak,
                lunchBreakMinutesInput = lunchBreakMinutesInput,
                lunchBreakDuration = lunchBreakDuration,
                lastCompletedSession = completedSessions.maxByOrNull { it.clockOut },
                completedSessions = completedSessions,
            ),
        )
    }

    private fun withDailySummary(state: TimeClockUiState): TimeClockUiState {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val todayCompletedSessions = state.completedSessions.filter { it.overlapsDate(today, zoneId) }
        val activeSession = state.clockInTime?.let { WorkSession(it, Instant.now()) }
        val todaySessions = todayCompletedSessions + listOfNotNull(activeSession)
            .filter { it.overlapsDate(today, zoneId) }

        val completedDuration = todayCompletedSessions.fold(Duration.ZERO) { total, session ->
            total.plus(session.durationOnDate(today, zoneId))
        }
        val activeDuration = activeSession?.durationOnDate(today, zoneId) ?: Duration.ZERO
        val todayTotalDuration = completedDuration.plus(activeDuration)
        val todayBreakDeduction = if (state.deductUnpaidLunchBreak) {
            state.lunchBreakDuration
        } else {
            Duration.ZERO
        }
        val todayCreditedDuration = todayTotalDuration
        val isTodayWorkday = LocalDate.now(zoneId).dayOfWeek in state.workDays
        val expectedTodayDuration = if (isTodayWorkday) {
            state.expectedDailyDuration.plus(todayBreakDeduction)
        } else {
            Duration.ZERO
        }
        val balance = todayCreditedDuration.minus(expectedTodayDuration)

        return state.copy(
            todayTotalDuration = todayTotalDuration,
            todayCreditedDuration = todayCreditedDuration,
            todayBreakDeduction = todayBreakDeduction,
            todaySessionCount = todaySessions.size,
            todayFirstClockIn = todaySessions.minByOrNull { it.clockIn }?.clockIn,
            todayLastClockOut = todayCompletedSessions.maxByOrNull { it.clockOut }?.clockOut,
            expectedWeeklyDuration = state.expectedWeeklyDuration,
            isTodayWorkday = isTodayWorkday,
            todayBalanceDuration = balance,
            todayProgressMessage = formatProgressMessage(
                isTodayWorkday = isTodayWorkday,
                worked = todayCreditedDuration,
                expected = expectedTodayDuration,
            ),
        )
    }

    private fun loadCompletedSessions(): List<WorkSession> {
        val savedSessions = preferences.getString(KEY_COMPLETED_SESSIONS, null)
            ?.let(::decodeSessions)
            .orEmpty()

        if (savedSessions.isNotEmpty()) return savedSessions

        val lastClockIn = preferences.getLongOrNull(KEY_LAST_CLOCK_IN)?.let(Instant::ofEpochMilli)
        val lastClockOut = preferences.getLongOrNull(KEY_LAST_CLOCK_OUT)?.let(Instant::ofEpochMilli)
        val migratedSession = if (lastClockIn != null && lastClockOut != null) {
            listOf(WorkSession(lastClockIn, lastClockOut))
        } else {
            emptyList()
        }

        if (migratedSession.isNotEmpty()) {
            preferences.edit()
                .putString(KEY_COMPLETED_SESSIONS, encodeSessions(migratedSession))
                .remove(KEY_LAST_CLOCK_IN)
                .remove(KEY_LAST_CLOCK_OUT)
                .apply()
        }

        return migratedSession
    }

    private fun encodeSessions(sessions: List<WorkSession>): String {
        return sessions.joinToString(separator = "\n") {
            "${it.clockIn.toEpochMilli()},${it.clockOut.toEpochMilli()}"
        }
    }

    private fun decodeSessions(encoded: String): List<WorkSession> {
        return encoded.lineSequence()
            .mapNotNull { line ->
                val parts = line.split(",")
                val clockIn = parts.getOrNull(0)?.toLongOrNull()
                val clockOut = parts.getOrNull(1)?.toLongOrNull()

                if (clockIn != null && clockOut != null && clockOut >= clockIn) {
                    WorkSession(Instant.ofEpochMilli(clockIn), Instant.ofEpochMilli(clockOut))
                } else {
                    null
                }
            }
            .sortedBy { it.clockIn }
            .toList()
    }

    private fun sanitizeDurationInput(input: String): String {
        return input
            .filter { it.isDigit() || it == ':' || it == '.' || it == ',' || it == 'h' || it == 'H' || it == 'm' || it == 'M' || it.isWhitespace() }
            .take(12)
    }

    private fun sanitizeWholeNumberInput(input: String): String {
        return input.filter { it.isDigit() }
    }

    private fun String.toDurationOrNull(): Duration? {
        val normalized = trim().lowercase().replace(',', '.')
        if (normalized.isBlank()) return null

        val minutes = when {
            ":" in normalized -> parseColonDuration(normalized)
            "h" in normalized || "m" in normalized -> parseLabeledDuration(normalized)
            normalized.contains(" ") -> parseSpacedDuration(normalized)
            else -> parseDecimalHours(normalized)
        } ?: return null

        if (minutes < 0L || minutes > MAX_EXPECTED_MINUTES) return null
        return Duration.ofMinutes(minutes)
    }

    private fun parseColonDuration(input: String): Long? {
        val parts = input.split(":")
        val hours = parts.getOrNull(0)?.trim()?.toLongOrNull() ?: return null
        val minutes = parts.getOrNull(1)?.trim()?.toLongOrNull() ?: 0L
        if (parts.size > 2 || minutes !in 0L..59L) return null
        return hours * 60 + minutes
    }

    private fun parseLabeledDuration(input: String): Long? {
        val hours = Regex("""(\d+(?:\.\d+)?)\s*h""").find(input)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()
            ?: 0.0
        val minutes = Regex("""(\d+)\s*m""").find(input)
            ?.groupValues
            ?.getOrNull(1)
            ?.toLongOrNull()
            ?: 0L

        if ("h" !in input && "m" !in input) return null
        return (hours * 60).toLong() + minutes
    }

    private fun parseSpacedDuration(input: String): Long? {
        val parts = input.split(Regex("""\s+""")).filter { it.isNotBlank() }
        val hours = parts.getOrNull(0)?.toLongOrNull() ?: return null
        val minutes = parts.getOrNull(1)?.toLongOrNull() ?: 0L
        if (parts.size > 2 || minutes !in 0L..59L) return null
        return hours * 60 + minutes
    }

    private fun parseDecimalHours(input: String): Long? {
        val hours = input.toDoubleOrNull() ?: return null
        return (hours * 60).toLong()
    }

    private fun Duration.dividedByWorkdays(workdayCount: Int): Duration {
        if (workdayCount <= 0) return Duration.ZERO
        return Duration.ofMinutes(toMinutes() / workdayCount)
    }

    private fun encodeWorkDays(days: Set<DayOfWeek>): String {
        return days.sortedBy { it.value }.joinToString(separator = ",") { it.name }
    }

    private fun decodeWorkDays(encoded: String): Set<DayOfWeek> {
        val days = encoded.split(",")
            .mapNotNull { value ->
                runCatching { DayOfWeek.valueOf(value) }.getOrNull()
            }
            .toSet()

        return days.ifEmpty { DEFAULT_WORK_DAYS }
    }

    private fun WorkSession.overlapsDate(date: LocalDate, zoneId: ZoneId): Boolean {
        return durationOnDate(date, zoneId) > Duration.ZERO
    }

    private fun WorkSession.durationOnDate(date: LocalDate, zoneId: ZoneId): Duration {
        val dayStart = date.atStartOfDay(zoneId).toInstant()
        val dayEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant()
        val overlapStart = maxOf(clockIn, dayStart)
        val overlapEnd = minOf(clockOut, dayEnd)

        return if (overlapEnd > overlapStart) {
            Duration.between(overlapStart, overlapEnd)
        } else {
            Duration.ZERO
        }
    }

    private fun android.content.SharedPreferences.getLongOrNull(key: String): Long? {
        return if (contains(key)) getLong(key, 0L) else null
    }

    private companion object {
        const val PREFS_NAME = "time_clock_preferences"
        const val KEY_ACTIVE_CLOCK_IN = "active_clock_in"
        const val KEY_COMPLETED_SESSIONS = "completed_sessions"
        const val KEY_EXPECTED_DAILY_HOURS = "expected_daily_hours"
        const val KEY_EXPECTED_WEEKLY_HOURS = "expected_weekly_hours"
        const val KEY_WORK_DAYS = "work_days"
        const val KEY_DEDUCT_UNPAID_LUNCH_BREAK = "deduct_unpaid_lunch_break"
        const val KEY_LUNCH_BREAK_MINUTES = "lunch_break_minutes"
        const val KEY_LAST_CLOCK_IN = "last_clock_in"
        const val KEY_LAST_CLOCK_OUT = "last_clock_out"
    }
}

@Composable
fun TimeClockScreen(
    state: TimeClockUiState,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit,
    onHistoryDayToggle: (LocalDate) -> Unit,
    onSettingsExpandedToggle: () -> Unit,
    onExpectedDailyHoursChange: (String) -> Unit,
    onExpectedWeeklyHoursChange: (String) -> Unit,
    onWorkdayToggle: (DayOfWeek) -> Unit,
    onUnpaidLunchBreakToggle: (Boolean) -> Unit,
    onLunchBreakMinutesChange: (String) -> Unit,
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(28.dp),
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

            ClockActionButton(
                isClockedIn = state.isClockedIn,
                onClockIn = onClockIn,
                onClockOut = onClockOut,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 4.dp),
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
                DailySummaryCard(state = state)
                WorkHoursSettingsCard(
                    state = state,
                    onSettingsExpandedToggle = onSettingsExpandedToggle,
                    onExpectedDailyHoursChange = onExpectedDailyHoursChange,
                    onExpectedWeeklyHoursChange = onExpectedWeeklyHoursChange,
                    onWorkdayToggle = onWorkdayToggle,
                    onUnpaidLunchBreakToggle = onUnpaidLunchBreakToggle,
                    onLunchBreakMinutesChange = onLunchBreakMinutesChange,
                )
                LastSessionCard(session = state.lastCompletedSession)
                HistoryCard(
                    state = state,
                    onHistoryDayToggle = onHistoryDayToggle,
                )
            }
        }
    }
}

@Composable
private fun ClockActionButton(
    isClockedIn: Boolean,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit,
) {
    Button(
        onClick = if (isClockedIn) onClockOut else onClockIn,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isClockedIn) {
                Color(0xFFB42318)
            } else {
                MaterialTheme.colorScheme.primary
            },
        ),
    ) {
        Text(
            text = if (isClockedIn) "Clock Out" else "Clock In",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DailySummaryCard(state: TimeClockUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = formatDuration(state.todayCreditedDuration),
                fontSize = 34.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (state.todayBreakDeduction > Duration.ZERO) {
                Text(
                    text = "${formatDurationShort(state.todayBreakDeduction)} unpaid lunch added to today's clocked target",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = state.todayProgressMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = if (state.todayBalanceDuration.isNegative) {
                    Color(0xFFB42318)
                } else {
                    Color(0xFF0F766E)
                },
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TimeStamp(label = "Sessions", value = state.todaySessionCount.toString())
                TimeStamp(label = "First in", value = state.todayFirstClockIn?.let(::formatTime) ?: "-")
                TimeStamp(
                    label = "Last out",
                    value = if (state.isClockedIn) {
                        "Active"
                    } else {
                        state.todayLastClockOut?.let(::formatTime) ?: "-"
                    },
                )
            }
        }
    }
}

@Composable
private fun WorkHoursSettingsCard(
    state: TimeClockUiState,
    onSettingsExpandedToggle: () -> Unit,
    onExpectedDailyHoursChange: (String) -> Unit,
    onExpectedWeeklyHoursChange: (String) -> Unit,
    onWorkdayToggle: (DayOfWeek) -> Unit,
    onUnpaidLunchBreakToggle: (Boolean) -> Unit,
    onLunchBreakMinutesChange: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF5)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Work hours and breaks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onSettingsExpandedToggle) {
                    Icon(
                        imageVector = if (state.isSettingsExpanded) {
                            Icons.Default.ExpandLess
                        } else {
                            Icons.Default.ExpandMore
                        },
                        contentDescription = if (state.isSettingsExpanded) {
                            "Collapse settings"
                        } else {
                            "Expand settings"
                        },
                    )
                }
            }

            if (!state.isSettingsExpanded) return@Column

            OutlinedTextField(
                value = state.expectedDailyHoursInput,
                onValueChange = onExpectedDailyHoursChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Expected per workday") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            OutlinedTextField(
                value = state.expectedWeeklyHoursInput,
                onValueChange = onExpectedWeeklyHoursChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Expected per week") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            SettingSwitchRow(
                title = "Deduct unpaid lunch",
                subtitle = "Subtract lunch from today's credited time",
                checked = state.deductUnpaidLunchBreak,
                onCheckedChange = onUnpaidLunchBreakToggle,
            )
            if (state.deductUnpaidLunchBreak) {
                OutlinedTextField(
                    value = state.lunchBreakMinutesInput,
                    onValueChange = onLunchBreakMinutesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lunch break minutes") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    WORK_DAYS_ROW_ONE.forEach { day ->
                        WorkdayChip(
                            day = day,
                            selected = day in state.workDays,
                            onClick = { onWorkdayToggle(day) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    WORK_DAYS_ROW_TWO.forEach { day ->
                        WorkdayChip(
                            day = day,
                            selected = day in state.workDays,
                            onClick = { onWorkdayToggle(day) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun WorkdayChip(
    day: DayOfWeek,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = day.shortLabel(),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun LastSessionCard(session: WorkSession?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6F4)),
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
private fun HistoryCard(
    state: TimeClockUiState,
    onHistoryDayToggle: (LocalDate) -> Unit,
) {
    val historyDays = buildHistoryDays(state)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (historyDays.isEmpty()) {
                Text(
                    text = "No completed work sessions yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                historyDays.forEach { day ->
                    HistoryDayRow(
                        day = day,
                        expanded = day.date in state.expandedHistoryDates,
                        onToggle = { onHistoryDayToggle(day.date) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryDayRow(
    day: WorkDayHistory,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = HISTORY_DATE_FORMATTER.format(day.date),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${formatHoursAndMinutes(day.totalDuration)} worked",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatHistoryBalance(day),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (day.balanceDuration.isNegative) {
                        Color(0xFFB42318)
                    } else {
                        Color(0xFF0F766E)
                    },
                    fontWeight = FontWeight.Medium,
                )
            }
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse day" else "Expand day",
                )
            }
        }

        if (expanded) {
            day.sessions.forEach { session ->
                HistorySessionRow(session = session)
            }
        }
    }
}

@Composable
private fun HistorySessionRow(session: WorkSession) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "${formatTime(session.clockIn)} - ${formatTime(session.clockOut)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatHoursAndMinutes(session.duration),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
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

private fun formatDurationShort(duration: Duration): String {
    val seconds = duration.seconds.coerceAtLeast(0)
    val hours = seconds / 3_600
    val minutes = (seconds % 3_600) / 60
    return if (minutes == 0L) {
        "${hours}h"
    } else {
        "${hours}h ${minutes}m"
    }
}

private fun formatHoursAndMinutes(duration: Duration): String {
    val seconds = duration.seconds.coerceAtLeast(0)
    val hours = seconds / 3_600
    val minutes = (seconds % 3_600) / 60
    return "${hours}h ${"%02d".format(minutes)}m"
}

private fun formatDurationInput(duration: Duration): String {
    val totalMinutes = duration.toMinutes().coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "%d:%02d".format(hours, minutes)
}

private fun formatProgressMessage(
    isTodayWorkday: Boolean,
    worked: Duration,
    expected: Duration,
): String {
    if (!isTodayWorkday) return "No hours expected today"

    val balance = worked.minus(expected)
    return when {
        balance.isNegative -> "You need ${formatDurationShort(balance.abs())} more today"
        balance == Duration.ZERO -> "You are exactly on target today"
        else -> "You are ${formatDurationShort(balance)} ahead today"
    }
}

private fun buildHistoryDays(state: TimeClockUiState): List<WorkDayHistory> {
    val zoneId = ZoneId.systemDefault()

    return state.completedSessions
        .groupBy { it.clockIn.atZone(zoneId).toLocalDate() }
        .map { (date, sessions) ->
            val sortedSessions = sessions.sortedBy { it.clockIn }
            val totalDuration = sortedSessions.fold(Duration.ZERO) { total, session ->
                total.plus(session.duration)
            }
            val unpaidLunch = if (state.deductUnpaidLunchBreak && date.dayOfWeek in state.workDays) {
                state.lunchBreakDuration
            } else {
                Duration.ZERO
            }
            val expectedDuration = if (date.dayOfWeek in state.workDays) {
                state.expectedDailyDuration.plus(unpaidLunch)
            } else {
                Duration.ZERO
            }

            WorkDayHistory(
                date = date,
                sessions = sortedSessions,
                totalDuration = totalDuration,
                expectedDuration = expectedDuration,
            )
        }
        .sortedByDescending { it.date }
}

private fun formatHistoryBalance(day: WorkDayHistory): String {
    if (day.expectedDuration == Duration.ZERO) {
        return "No hours expected"
    }

    return when {
        day.balanceDuration.isNegative -> "${formatHoursAndMinutes(day.balanceDuration.abs())} missing"
        day.balanceDuration == Duration.ZERO -> "On target"
        else -> "${formatHoursAndMinutes(day.balanceDuration)} ahead"
    }
}

private fun DayOfWeek.shortLabel(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }
}

private fun formatTime(instant: Instant): String {
    return TIME_FORMATTER.format(instant.atZone(ZoneId.systemDefault()))
}

private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val HISTORY_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")
private val DEFAULT_DAILY_HOURS_INPUT = "7:30"
private val DEFAULT_WEEKLY_HOURS_INPUT = "37:30"
private val DEFAULT_DAILY_DURATION = Duration.ofHours(7).plusMinutes(30)
private val MAX_EXPECTED_MINUTES = 7L * 24L * 60L
private val DEFAULT_LUNCH_BREAK_MINUTES = 30L
private val DEFAULT_LUNCH_BREAK_MINUTES_INPUT = "30"
private val DEFAULT_WORK_DAYS = setOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
)
private val WORK_DAYS_ROW_ONE = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
)
private val WORK_DAYS_ROW_TWO = listOf(
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY,
)

@Preview(showBackground = true)
@Composable
private fun ClockedOutPreview() {
    TimeClockTheme {
        TimeClockScreen(
            state = TimeClockUiState(
                todayTotalDuration = Duration.ofHours(6).plusMinutes(25),
                todaySessionCount = 2,
                todayFirstClockIn = Instant.now().minus(Duration.ofHours(7)),
                todayLastClockOut = Instant.now().minus(Duration.ofMinutes(20)),
                todayProgressMessage = "You need 1h 5m more today",
                todayBalanceDuration = Duration.ofMinutes(-65),
                isSettingsExpanded = true,
                deductUnpaidLunchBreak = true,
                todayCreditedDuration = Duration.ofHours(5).plusMinutes(55),
                todayBreakDeduction = Duration.ofMinutes(30),
                lastCompletedSession = WorkSession(
                    clockIn = Instant.now().minus(Duration.ofHours(3)),
                    clockOut = Instant.now().minus(Duration.ofMinutes(20)),
                ),
                completedSessions = listOf(
                    WorkSession(
                        clockIn = Instant.now().minus(Duration.ofDays(1)).minus(Duration.ofHours(8)),
                        clockOut = Instant.now().minus(Duration.ofDays(1)),
                    ),
                    WorkSession(
                        clockIn = Instant.now().minus(Duration.ofHours(3)),
                        clockOut = Instant.now().minus(Duration.ofMinutes(20)),
                    ),
                ),
                expandedHistoryDates = setOf(LocalDate.now()),
            ),
            onClockIn = {},
            onClockOut = {},
            onHistoryDayToggle = {},
            onSettingsExpandedToggle = {},
            onExpectedDailyHoursChange = {},
            onExpectedWeeklyHoursChange = {},
            onWorkdayToggle = {},
            onUnpaidLunchBreakToggle = {},
            onLunchBreakMinutesChange = {},
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
                todayTotalDuration = Duration.ofHours(4).plusMinutes(42),
                todaySessionCount = 2,
                todayFirstClockIn = Instant.now().minus(Duration.ofHours(5)),
                todayProgressMessage = "You need 2h 48m more today",
                todayBalanceDuration = Duration.ofMinutes(-168),
                todayCreditedDuration = Duration.ofHours(4).plusMinutes(42),
                lastCompletedSession = WorkSession(
                    clockIn = Instant.now().minus(Duration.ofHours(5)),
                    clockOut = Instant.now().minus(Duration.ofHours(1)),
                ),
                completedSessions = listOf(
                    WorkSession(
                        clockIn = Instant.now().minus(Duration.ofHours(5)),
                        clockOut = Instant.now().minus(Duration.ofHours(1)),
                    ),
                ),
            ),
            onClockIn = {},
            onClockOut = {},
            onHistoryDayToggle = {},
            onSettingsExpandedToggle = {},
            onExpectedDailyHoursChange = {},
            onExpectedWeeklyHoursChange = {},
            onWorkdayToggle = {},
            onUnpaidLunchBreakToggle = {},
            onLunchBreakMinutesChange = {},
        )
    }
}
