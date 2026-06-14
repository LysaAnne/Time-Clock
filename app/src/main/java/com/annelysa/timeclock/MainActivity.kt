package com.annelysa.timeclock

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TextButton
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
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
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
                    onProfileSelect = viewModel::selectProfile,
                    onProfileNameChange = viewModel::updateActiveProfileName,
                    onProfileStartDateChange = viewModel::updateActiveProfileStartDate,
                    onNewProfileNameChange = viewModel::updateNewProfileName,
                    onNewProfileStartDateChange = viewModel::updateNewProfileStartDate,
                    onProfileCreate = viewModel::createProfile,
                    onHistoryDayToggle = viewModel::toggleHistoryDay,
                    onManualDateChange = viewModel::updateManualDate,
                    onManualClockInChange = viewModel::updateManualClockIn,
                    onManualClockOutChange = viewModel::updateManualClockOut,
                    onManualSessionSave = viewModel::saveManualSession,
                    onManualSessionCancel = viewModel::cancelManualEdit,
                    onSessionEdit = viewModel::startEditingSession,
                    onSessionDelete = viewModel::deleteSession,
                    onSettingsExpandedToggle = viewModel::toggleSettingsExpanded,
                    onExpectedDailyHoursChange = viewModel::updateExpectedDailyHours,
                    onExpectedWeeklyHoursChange = viewModel::updateExpectedWeeklyHours,
                    onWorkdayToggle = viewModel::toggleWorkday,
                    onUnpaidLunchBreakToggle = viewModel::toggleUnpaidLunchBreak,
                    onLunchBreakMinutesChange = viewModel::updateLunchBreakMinutes,
                    onOvertimeStartDateChange = viewModel::updateOvertimeStartDate,
                    onStartingOvertimeBalanceChange = viewModel::updateStartingOvertimeBalance,
                    onOvertimeRangeChange = viewModel::updateOvertimeRange,
                )
            }
        }
    }
}

data class TimeClockUiState(
    val workProfiles: List<WorkProfile> = listOf(DEFAULT_WORK_PROFILE),
    val activeProfileId: String = DEFAULT_WORK_PROFILE.id,
    val activeProfileNameInput: String = DEFAULT_WORK_PROFILE.name,
    val activeProfileStartDateInput: String = formatDateInput(DEFAULT_WORK_PROFILE.trackingStartDate),
    val newProfileNameInput: String = "",
    val newProfileStartDateInput: String = formatDateInput(LocalDate.now()),
    val profileError: String? = null,
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
    val manualDateInput: String = formatDateInput(LocalDate.now()),
    val manualClockInInput: String = "",
    val manualClockOutInput: String = "",
    val manualEntryError: String? = null,
    val editingSessionClockInMillis: Long? = null,
    val editingSessionClockOutMillis: Long? = null,
    val overtimeStartDateInput: String = formatDateInput(LocalDate.now()),
    val startingOvertimeBalanceInput: String = "0:00",
    val selectedOvertimeRange: OvertimeRange = OvertimeRange.ALL_TIME,
    val overtimeSettingsError: String? = null,
)

data class WorkProfile(
    val id: String,
    val name: String,
    val trackingStartDate: LocalDate,
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

data class WorkReport(
    val label: String,
    val actualDuration: Duration,
    val expectedDuration: Duration,
) {
    val balanceDuration: Duration = actualDuration.minus(expectedDuration)
}

data class OvertimeBalance(
    val range: OvertimeRange,
    val startDate: LocalDate,
    val actualDuration: Duration,
    val expectedDuration: Duration,
    val startingBalance: Duration,
) {
    val periodBalance: Duration = actualDuration.minus(expectedDuration)
    val totalBalance: Duration = startingBalance.plus(periodBalance)
}

enum class OvertimeRange(val label: String) {
    TODAY("Today"),
    ONE_WEEK("1 week"),
    FOUR_WEEKS("4 weeks"),
    ONE_MONTH("1 month"),
    SIX_MONTHS("6 months"),
    TWELVE_MONTHS("12 months"),
    ALL_TIME("All time"),
}

data class DailyChartEntry(
    val date: LocalDate,
    val actualDuration: Duration,
    val expectedDuration: Duration,
) {
    val balanceDuration: Duration = actualDuration.minus(expectedDuration)
}

data class MonthlyTrendEntry(
    val label: String,
    val balanceDuration: Duration,
)

data class CalendarDayVisual(
    val date: LocalDate,
    val actualDuration: Duration,
    val expectedDuration: Duration,
) {
    val status: DayVisualStatus = when {
        expectedDuration == Duration.ZERO && actualDuration == Duration.ZERO -> DayVisualStatus.NO_TARGET
        expectedDuration == Duration.ZERO -> DayVisualStatus.OVERTIME
        actualDuration < expectedDuration -> DayVisualStatus.MISSING
        actualDuration == expectedDuration -> DayVisualStatus.ON_TARGET
        else -> DayVisualStatus.OVERTIME
    }
}

enum class DayVisualStatus {
    MISSING,
    ON_TARGET,
    OVERTIME,
    NO_TARGET,
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

    fun selectProfile(profileId: String) {
        if (profileId == _uiState.value.activeProfileId) return
        val profiles = _uiState.value.workProfiles
        val selectedProfile = profiles.firstOrNull { it.id == profileId } ?: return

        preferences.edit()
            .putString(KEY_ACTIVE_PROFILE_ID, profileId)
            .apply()

        _uiState.value = buildStateForProfile(
            profiles = profiles,
            activeProfile = selectedProfile,
            newProfileNameInput = _uiState.value.newProfileNameInput,
            newProfileStartDateInput = _uiState.value.newProfileStartDateInput,
        )
    }

    fun updateActiveProfileName(input: String) {
        val sanitizedInput = sanitizeProfileName(input)
        val updatedProfile = _uiState.value.activeProfile.copy(
            name = sanitizedInput.ifBlank { DEFAULT_WORK_PROFILE_NAME },
        )
        val updatedProfiles = _uiState.value.workProfiles.replaceProfile(updatedProfile)

        saveProfiles(updatedProfiles)
        _uiState.value = _uiState.value.copy(
            workProfiles = updatedProfiles,
            activeProfileNameInput = sanitizedInput,
            profileError = null,
        )
    }

    fun updateActiveProfileStartDate(input: String) {
        val sanitizedInput = input.take(10)
        val parsedDate = runCatching { LocalDate.parse(sanitizedInput) }.getOrNull()
        val error = if (sanitizedInput.length == 10 && parsedDate == null) {
            "Use date format YYYY-MM-DD."
        } else {
            null
        }
        val updatedProfiles = parsedDate?.let { date ->
            val updatedProfile = _uiState.value.activeProfile.copy(trackingStartDate = date)
            _uiState.value.workProfiles.replaceProfile(updatedProfile)
        } ?: _uiState.value.workProfiles

        if (parsedDate != null) {
            saveProfiles(updatedProfiles)
        }

        _uiState.value = withDailySummary(
            _uiState.value.copy(
                workProfiles = updatedProfiles,
                activeProfileStartDateInput = sanitizedInput,
                profileError = error,
            ),
        )
    }

    fun updateNewProfileName(input: String) {
        _uiState.value = _uiState.value.copy(
            newProfileNameInput = sanitizeProfileName(input),
            profileError = null,
        )
    }

    fun updateNewProfileStartDate(input: String) {
        _uiState.value = _uiState.value.copy(
            newProfileStartDateInput = input.take(10),
            profileError = null,
        )
    }

    fun createProfile() {
        val name = _uiState.value.newProfileNameInput.trim()
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(profileError = "Add a workplace name first.")
            return
        }

        val startDate = runCatching { LocalDate.parse(_uiState.value.newProfileStartDateInput) }.getOrNull()
        if (startDate == null) {
            _uiState.value = _uiState.value.copy(profileError = "Use date format YYYY-MM-DD.")
            return
        }

        val newProfile = WorkProfile(
            id = "profile_${System.currentTimeMillis()}",
            name = name,
            trackingStartDate = startDate,
        )
        val updatedProfiles = _uiState.value.workProfiles + newProfile

        saveProfiles(updatedProfiles)
        preferences.edit()
            .putString(KEY_ACTIVE_PROFILE_ID, newProfile.id)
            .apply()

        _uiState.value = buildStateForProfile(
            profiles = updatedProfiles,
            activeProfile = newProfile,
            newProfileNameInput = "",
            newProfileStartDateInput = formatDateInput(LocalDate.now()),
        )
    }

    fun clockIn() {
        if (_uiState.value.isClockedIn) return

        val now = Instant.now()
        preferences.edit()
            .putLong(profileKey(KEY_ACTIVE_CLOCK_IN), now.toEpochMilli())
            .remove(KEY_ACTIVE_CLOCK_IN)
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

        saveCompletedSessions(completedSessions)
        preferences.edit()
            .remove(profileKey(KEY_ACTIVE_CLOCK_IN))
            .remove(KEY_ACTIVE_CLOCK_IN)
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

    fun updateManualDate(input: String) {
        _uiState.value = _uiState.value.copy(
            manualDateInput = input.take(10),
            manualEntryError = null,
        )
    }

    fun updateManualClockIn(input: String) {
        _uiState.value = _uiState.value.copy(
            manualClockInInput = sanitizeTimeInput(input),
            manualEntryError = null,
        )
    }

    fun updateManualClockOut(input: String) {
        _uiState.value = _uiState.value.copy(
            manualClockOutInput = sanitizeTimeInput(input),
            manualEntryError = null,
        )
    }

    fun startEditingSession(session: WorkSession) {
        val zoneId = ZoneId.systemDefault()
        val clockIn = session.clockIn.atZone(zoneId)
        val clockOut = session.clockOut.atZone(zoneId)

        _uiState.value = _uiState.value.copy(
            manualDateInput = formatDateInput(clockIn.toLocalDate()),
            manualClockInInput = TIME_INPUT_FORMATTER.format(clockIn.toLocalTime()),
            manualClockOutInput = TIME_INPUT_FORMATTER.format(clockOut.toLocalTime()),
            manualEntryError = null,
            editingSessionClockInMillis = session.clockIn.toEpochMilli(),
            editingSessionClockOutMillis = session.clockOut.toEpochMilli(),
        )
    }

    fun cancelManualEdit() {
        _uiState.value = _uiState.value.copy(
            manualDateInput = formatDateInput(LocalDate.now()),
            manualClockInInput = "",
            manualClockOutInput = "",
            manualEntryError = null,
            editingSessionClockInMillis = null,
            editingSessionClockOutMillis = null,
        )
    }

    fun saveManualSession() {
        val parsedSession = parseManualSession()
        if (parsedSession == null) {
            _uiState.value = _uiState.value.copy(
                manualEntryError = "Use date YYYY-MM-DD and times like 09:00 and 17:00.",
            )
            return
        }

        if (parsedSession.clockOut <= parsedSession.clockIn) {
            _uiState.value = _uiState.value.copy(
                manualEntryError = "Clock out must be after clock in.",
            )
            return
        }

        val editingClockInMillis = _uiState.value.editingSessionClockInMillis
        val editingClockOutMillis = _uiState.value.editingSessionClockOutMillis
        val sessionsWithoutEditedItem = _uiState.value.completedSessions.filterNot { session ->
            editingClockInMillis != null &&
                editingClockOutMillis != null &&
                session.clockIn.toEpochMilli() == editingClockInMillis &&
                session.clockOut.toEpochMilli() == editingClockOutMillis
        }
        val completedSessions = (sessionsWithoutEditedItem + parsedSession).sortedBy { it.clockIn }

        saveCompletedSessions(completedSessions)
        _uiState.value = withDailySummary(
            _uiState.value.copy(
                completedSessions = completedSessions,
                lastCompletedSession = completedSessions.maxByOrNull { it.clockOut },
                expandedHistoryDates = _uiState.value.expandedHistoryDates + parsedSession.clockIn
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(),
                manualDateInput = formatDateInput(LocalDate.now()),
                manualClockInInput = "",
                manualClockOutInput = "",
                manualEntryError = null,
                editingSessionClockInMillis = null,
                editingSessionClockOutMillis = null,
            ),
        )
    }

    fun deleteSession(session: WorkSession) {
        val completedSessions = _uiState.value.completedSessions.filterNot {
            it.clockIn == session.clockIn && it.clockOut == session.clockOut
        }

        saveCompletedSessions(completedSessions)
        _uiState.value = withDailySummary(
            _uiState.value.copy(
                completedSessions = completedSessions,
                lastCompletedSession = completedSessions.maxByOrNull { it.clockOut },
            ),
        )
    }

    fun updateExpectedDailyHours(input: String) {
        val sanitizedInput = sanitizeDurationInput(input)
        val dailyDuration = sanitizedInput.toDurationOrNull()
        val weeklyDuration = dailyDuration?.multipliedBy(_uiState.value.workDays.size.toLong())

        preferences.edit()
            .putString(profileKey(KEY_EXPECTED_DAILY_HOURS), sanitizedInput)
            .putString(profileKey(KEY_EXPECTED_WEEKLY_HOURS), weeklyDuration?.let(::formatDurationInput) ?: _uiState.value.expectedWeeklyHoursInput)
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
            .putString(profileKey(KEY_EXPECTED_WEEKLY_HOURS), sanitizedInput)
            .putString(profileKey(KEY_EXPECTED_DAILY_HOURS), dailyDuration?.let(::formatDurationInput) ?: _uiState.value.expectedDailyHoursInput)
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
            .putString(profileKey(KEY_WORK_DAYS), encodeWorkDays(updatedDays))
            .putString(
                profileKey(KEY_EXPECTED_DAILY_HOURS),
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
            .putBoolean(profileKey(KEY_DEDUCT_UNPAID_LUNCH_BREAK), enabled)
            .apply()

        _uiState.value = withDailySummary(
            _uiState.value.copy(deductUnpaidLunchBreak = enabled),
        )
    }

    fun updateLunchBreakMinutes(input: String) {
        val sanitizedInput = sanitizeWholeNumberInput(input).take(3)
        val minutes = sanitizedInput.toLongOrNull()?.coerceIn(0L, 240L) ?: 0L

        preferences.edit()
            .putString(profileKey(KEY_LUNCH_BREAK_MINUTES), sanitizedInput)
            .apply()

        _uiState.value = withDailySummary(
            _uiState.value.copy(
                lunchBreakMinutesInput = sanitizedInput,
                lunchBreakDuration = Duration.ofMinutes(minutes),
            ),
        )
    }

    fun updateOvertimeStartDate(input: String) {
        val sanitizedInput = input.take(10)
        val error = if (sanitizedInput.length == 10 && runCatching { LocalDate.parse(sanitizedInput) }.isFailure) {
            "Use date format YYYY-MM-DD."
        } else {
            null
        }

        preferences.edit()
            .putString(profileKey(KEY_OVERTIME_START_DATE), sanitizedInput)
            .apply()

        _uiState.value = _uiState.value.copy(
            overtimeStartDateInput = sanitizedInput,
            overtimeSettingsError = error,
        )
    }

    fun updateStartingOvertimeBalance(input: String) {
        val sanitizedInput = sanitizeSignedDurationInput(input)
        val error = if (sanitizedInput.isNotBlank() && sanitizedInput.toSignedDurationOrNull() == null) {
            "Use balance like 2:30 or -1:15."
        } else {
            null
        }

        preferences.edit()
            .putString(profileKey(KEY_STARTING_OVERTIME_BALANCE), sanitizedInput)
            .apply()

        _uiState.value = _uiState.value.copy(
            startingOvertimeBalanceInput = sanitizedInput,
            overtimeSettingsError = error,
        )
    }

    fun updateOvertimeRange(range: OvertimeRange) {
        preferences.edit()
            .putString(profileKey(KEY_OVERTIME_RANGE), range.name)
            .apply()

        _uiState.value = _uiState.value.copy(selectedOvertimeRange = range)
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
        val profiles = loadProfiles()
        val savedActiveProfileId = preferences.getString(KEY_ACTIVE_PROFILE_ID, DEFAULT_WORK_PROFILE_ID)
        val activeProfile = profiles.firstOrNull { it.id == savedActiveProfileId } ?: profiles.first()

        if (savedActiveProfileId != activeProfile.id) {
            preferences.edit()
                .putString(KEY_ACTIVE_PROFILE_ID, activeProfile.id)
                .apply()
        }

        return buildStateForProfile(
            profiles = profiles,
            activeProfile = activeProfile,
            newProfileNameInput = "",
            newProfileStartDateInput = formatDateInput(LocalDate.now()),
        )
    }

    private fun buildStateForProfile(
        profiles: List<WorkProfile>,
        activeProfile: WorkProfile,
        newProfileNameInput: String,
        newProfileStartDateInput: String,
    ): TimeClockUiState {
        val activeClockIn = preferences.getLongOrNull(profileKey(activeProfile.id, KEY_ACTIVE_CLOCK_IN))
            .let { activeProfileClockIn ->
                activeProfileClockIn ?: if (activeProfile.id == DEFAULT_WORK_PROFILE_ID) {
                    preferences.getLongOrNull(KEY_ACTIVE_CLOCK_IN)
                } else {
                    null
                }
            }
            ?.let(Instant::ofEpochMilli)
        val completedSessions = loadCompletedSessions(activeProfile.id)
        val expectedDailyHours = getProfileString(activeProfile.id, KEY_EXPECTED_DAILY_HOURS, DEFAULT_DAILY_HOURS_INPUT)
            ?: DEFAULT_DAILY_HOURS_INPUT
        val expectedDailyDuration = expectedDailyHours.toDurationOrNull() ?: DEFAULT_DAILY_DURATION
        val expectedWeeklyHours = getProfileString(activeProfile.id, KEY_EXPECTED_WEEKLY_HOURS, DEFAULT_WEEKLY_HOURS_INPUT)
            ?: DEFAULT_WEEKLY_HOURS_INPUT
        val expectedWeeklyDuration = expectedWeeklyHours.toDurationOrNull()
            ?: expectedDailyDuration.multipliedBy(DEFAULT_WORK_DAYS.size.toLong())
        val workDays = getProfileString(activeProfile.id, KEY_WORK_DAYS, null)
            ?.let(::decodeWorkDays)
            ?: DEFAULT_WORK_DAYS
        val deductUnpaidLunchBreak = getProfileBoolean(
            profileId = activeProfile.id,
            key = KEY_DEDUCT_UNPAID_LUNCH_BREAK,
            defaultValue = false,
        )
        val lunchBreakMinutesInput = getProfileString(
            profileId = activeProfile.id,
            key = KEY_LUNCH_BREAK_MINUTES,
            defaultValue = DEFAULT_LUNCH_BREAK_MINUTES_INPUT,
        ) ?: DEFAULT_LUNCH_BREAK_MINUTES_INPUT
        val lunchBreakDuration = Duration.ofMinutes(
            lunchBreakMinutesInput.toLongOrNull()?.coerceIn(0L, 240L) ?: DEFAULT_LUNCH_BREAK_MINUTES,
        )
        val overtimeStartDateInput = getProfileString(
            profileId = activeProfile.id,
            key = KEY_OVERTIME_START_DATE,
            defaultValue = formatDateInput(activeProfile.trackingStartDate),
        ) ?: formatDateInput(activeProfile.trackingStartDate)
        val startingOvertimeBalanceInput = getProfileString(
            profileId = activeProfile.id,
            key = KEY_STARTING_OVERTIME_BALANCE,
            defaultValue = DEFAULT_STARTING_OVERTIME_BALANCE_INPUT,
        ) ?: DEFAULT_STARTING_OVERTIME_BALANCE_INPUT
        val selectedOvertimeRange = getProfileString(
            profileId = activeProfile.id,
            key = KEY_OVERTIME_RANGE,
            defaultValue = OvertimeRange.ALL_TIME.name,
        )
            ?.let { value -> runCatching { OvertimeRange.valueOf(value) }.getOrNull() }
            ?: OvertimeRange.ALL_TIME

        return withDailySummary(
            TimeClockUiState(
                workProfiles = profiles,
                activeProfileId = activeProfile.id,
                activeProfileNameInput = activeProfile.name,
                activeProfileStartDateInput = formatDateInput(activeProfile.trackingStartDate),
                newProfileNameInput = newProfileNameInput,
                newProfileStartDateInput = newProfileStartDateInput,
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
                overtimeStartDateInput = overtimeStartDateInput,
                startingOvertimeBalanceInput = startingOvertimeBalanceInput,
                selectedOvertimeRange = selectedOvertimeRange,
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

    private fun loadCompletedSessions(profileId: String = _uiState.value.activeProfileId): List<WorkSession> {
        val sessionsKey = profileKey(profileId, KEY_COMPLETED_SESSIONS)
        val savedSessions = preferences.getString(sessionsKey, null)
            ?.let(::decodeSessions)
            .orEmpty()

        if (savedSessions.isNotEmpty()) return savedSessions

        val legacySessions = if (profileId == DEFAULT_WORK_PROFILE_ID) {
            preferences.getString(KEY_COMPLETED_SESSIONS, null)
                ?.let(::decodeSessions)
                .orEmpty()
        } else {
            emptyList()
        }

        if (legacySessions.isNotEmpty()) {
            preferences.edit()
                .putString(sessionsKey, encodeSessions(legacySessions))
                .apply()
            return legacySessions
        }

        val lastClockIn = if (profileId == DEFAULT_WORK_PROFILE_ID) {
            preferences.getLongOrNull(KEY_LAST_CLOCK_IN)?.let(Instant::ofEpochMilli)
        } else {
            null
        }
        val lastClockOut = if (profileId == DEFAULT_WORK_PROFILE_ID) {
            preferences.getLongOrNull(KEY_LAST_CLOCK_OUT)?.let(Instant::ofEpochMilli)
        } else {
            null
        }
        val migratedSession = if (lastClockIn != null && lastClockOut != null) {
            listOf(WorkSession(lastClockIn, lastClockOut))
        } else {
            emptyList()
        }

        if (migratedSession.isNotEmpty()) {
            preferences.edit()
                .putString(sessionsKey, encodeSessions(migratedSession))
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

    private fun saveCompletedSessions(sessions: List<WorkSession>) {
        preferences.edit()
            .putString(profileKey(KEY_COMPLETED_SESSIONS), encodeSessions(sessions))
            .remove(KEY_LAST_CLOCK_IN)
            .remove(KEY_LAST_CLOCK_OUT)
            .apply()
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

    private fun sanitizeSignedDurationInput(input: String): String {
        val sanitized = input
            .filter { it.isDigit() || it == ':' || it == '.' || it == ',' || it == 'h' || it == 'H' || it == 'm' || it == 'M' || it == '-' || it == '+' || it.isWhitespace() }
            .take(13)

        val sign = sanitized.firstOrNull { it == '-' || it == '+' }?.toString().orEmpty()
        val unsigned = sanitized.filterNot { it == '-' || it == '+' }
        return sign + unsigned
    }

    private fun sanitizeWholeNumberInput(input: String): String {
        return input.filter { it.isDigit() }
    }

    private fun sanitizeTimeInput(input: String): String {
        return input.filter { it.isDigit() || it == ':' }.take(5)
    }

    private fun parseManualSession(): WorkSession? {
        val date = runCatching { LocalDate.parse(_uiState.value.manualDateInput) }.getOrNull()
            ?: return null
        val clockIn = parseTimeInput(_uiState.value.manualClockInInput) ?: return null
        val clockOut = parseTimeInput(_uiState.value.manualClockOutInput) ?: return null
        val zoneId = ZoneId.systemDefault()

        return WorkSession(
            clockIn = date.atTime(clockIn).atZone(zoneId).toInstant(),
            clockOut = date.atTime(clockOut).atZone(zoneId).toInstant(),
        )
    }

    private fun parseTimeInput(input: String): LocalTime? {
        return runCatching {
            LocalTime.parse(input, TIME_INPUT_FORMATTER)
        }.getOrNull()
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

    private fun loadProfiles(): List<WorkProfile> {
        val savedProfiles = preferences.getString(KEY_WORK_PROFILES, null)
            ?.let(::decodeProfiles)
            .orEmpty()

        if (savedProfiles.isNotEmpty()) return savedProfiles

        val legacySessions = preferences.getString(KEY_COMPLETED_SESSIONS, null)
            ?.let(::decodeSessions)
            .orEmpty()
        val earliestSessionDate = legacySessions.minByOrNull { it.clockIn }
            ?.clockIn
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDate()
        val legacyOvertimeStartDate = preferences.getString(KEY_OVERTIME_START_DATE, null)
            ?.let { savedDate -> runCatching { LocalDate.parse(savedDate) }.getOrNull() }
        val defaultProfile = DEFAULT_WORK_PROFILE.copy(
            trackingStartDate = earliestSessionDate
                ?: legacyOvertimeStartDate
                ?: LocalDate.now(),
        )

        saveProfiles(listOf(defaultProfile))
        preferences.edit()
            .putString(KEY_ACTIVE_PROFILE_ID, defaultProfile.id)
            .apply()

        return listOf(defaultProfile)
    }

    private fun saveProfiles(profiles: List<WorkProfile>) {
        preferences.edit()
            .putString(KEY_WORK_PROFILES, encodeProfiles(profiles))
            .apply()
    }

    private fun encodeProfiles(profiles: List<WorkProfile>): String {
        return profiles.joinToString(separator = "\n") { profile ->
            "${profile.id}|${sanitizeProfileName(profile.name).ifBlank { DEFAULT_WORK_PROFILE_NAME }}|${formatDateInput(profile.trackingStartDate)}"
        }
    }

    private fun decodeProfiles(encoded: String): List<WorkProfile> {
        return encoded.lineSequence()
            .mapNotNull { line ->
                val parts = line.split("|")
                val id = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val name = parts.getOrNull(1)?.takeIf { it.isNotBlank() } ?: DEFAULT_WORK_PROFILE_NAME
                val trackingStartDate = parts.getOrNull(2)
                    ?.let { value -> runCatching { LocalDate.parse(value) }.getOrNull() }
                    ?: LocalDate.now()

                WorkProfile(
                    id = id,
                    name = name,
                    trackingStartDate = trackingStartDate,
                )
            }
            .toList()
            .ifEmpty { listOf(DEFAULT_WORK_PROFILE) }
    }

    private fun getProfileString(
        profileId: String,
        key: String,
        defaultValue: String?,
    ): String? {
        val namespacedKey = profileKey(profileId, key)
        return preferences.getString(
            namespacedKey,
            if (profileId == DEFAULT_WORK_PROFILE_ID) preferences.getString(key, defaultValue) else defaultValue,
        )
    }

    private fun getProfileBoolean(
        profileId: String,
        key: String,
        defaultValue: Boolean,
    ): Boolean {
        val namespacedKey = profileKey(profileId, key)
        return if (preferences.contains(namespacedKey)) {
            preferences.getBoolean(namespacedKey, defaultValue)
        } else if (profileId == DEFAULT_WORK_PROFILE_ID && preferences.contains(key)) {
            preferences.getBoolean(key, defaultValue)
        } else {
            defaultValue
        }
    }

    private fun profileKey(key: String): String {
        return profileKey(_uiState.value.activeProfileId, key)
    }

    private fun profileKey(profileId: String, key: String): String {
        return "profile_${profileId}_$key"
    }

    private fun sanitizeProfileName(input: String): String {
        return input
            .filterNot { it == '\n' || it == '\r' || it == '|' }
            .take(40)
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
        const val KEY_WORK_PROFILES = "work_profiles"
        const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
        const val KEY_ACTIVE_CLOCK_IN = "active_clock_in"
        const val KEY_COMPLETED_SESSIONS = "completed_sessions"
        const val KEY_EXPECTED_DAILY_HOURS = "expected_daily_hours"
        const val KEY_EXPECTED_WEEKLY_HOURS = "expected_weekly_hours"
        const val KEY_WORK_DAYS = "work_days"
        const val KEY_DEDUCT_UNPAID_LUNCH_BREAK = "deduct_unpaid_lunch_break"
        const val KEY_LUNCH_BREAK_MINUTES = "lunch_break_minutes"
        const val KEY_OVERTIME_START_DATE = "overtime_start_date"
        const val KEY_STARTING_OVERTIME_BALANCE = "starting_overtime_balance"
        const val KEY_OVERTIME_RANGE = "overtime_range"
        const val KEY_LAST_CLOCK_IN = "last_clock_in"
        const val KEY_LAST_CLOCK_OUT = "last_clock_out"
    }
}

@Composable
fun TimeClockScreen(
    state: TimeClockUiState,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit,
    onProfileSelect: (String) -> Unit,
    onProfileNameChange: (String) -> Unit,
    onProfileStartDateChange: (String) -> Unit,
    onNewProfileNameChange: (String) -> Unit,
    onNewProfileStartDateChange: (String) -> Unit,
    onProfileCreate: () -> Unit,
    onHistoryDayToggle: (LocalDate) -> Unit,
    onManualDateChange: (String) -> Unit,
    onManualClockInChange: (String) -> Unit,
    onManualClockOutChange: (String) -> Unit,
    onManualSessionSave: () -> Unit,
    onManualSessionCancel: () -> Unit,
    onSessionEdit: (WorkSession) -> Unit,
    onSessionDelete: (WorkSession) -> Unit,
    onSettingsExpandedToggle: () -> Unit,
    onExpectedDailyHoursChange: (String) -> Unit,
    onExpectedWeeklyHoursChange: (String) -> Unit,
    onWorkdayToggle: (DayOfWeek) -> Unit,
    onUnpaidLunchBreakToggle: (Boolean) -> Unit,
    onLunchBreakMinutesChange: (String) -> Unit,
    onOvertimeStartDateChange: (String) -> Unit,
    onStartingOvertimeBalanceChange: (String) -> Unit,
    onOvertimeRangeChange: (OvertimeRange) -> Unit,
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

            WorkProfileCard(
                state = state,
                onProfileSelect = onProfileSelect,
                onProfileNameChange = onProfileNameChange,
                onProfileStartDateChange = onProfileStartDateChange,
                onNewProfileNameChange = onNewProfileNameChange,
                onNewProfileStartDateChange = onNewProfileStartDateChange,
                onProfileCreate = onProfileCreate,
            )

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
                    onOvertimeStartDateChange = onOvertimeStartDateChange,
                    onStartingOvertimeBalanceChange = onStartingOvertimeBalanceChange,
                )
                LastSessionCard(session = state.lastCompletedSession)
                ManualEntryCard(
                    state = state,
                    onDateChange = onManualDateChange,
                    onClockInChange = onManualClockInChange,
                    onClockOutChange = onManualClockOutChange,
                    onSave = onManualSessionSave,
                    onCancel = onManualSessionCancel,
                )
                ReportsCard(state = state)
                ChartsCard(state = state)
                OvertimeBalanceCard(
                    state = state,
                    onOvertimeRangeChange = onOvertimeRangeChange,
                )
                HistoryCard(
                    state = state,
                    onHistoryDayToggle = onHistoryDayToggle,
                    onSessionEdit = onSessionEdit,
                    onSessionDelete = onSessionDelete,
                )
            }
        }
    }
}

@Composable
private fun WorkProfileCard(
    state: TimeClockUiState,
    onProfileSelect: (String) -> Unit,
    onProfileNameChange: (String) -> Unit,
    onProfileStartDateChange: (String) -> Unit,
    onNewProfileNameChange: (String) -> Unit,
    onNewProfileStartDateChange: (String) -> Unit,
    onProfileCreate: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Work profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            WorkProfileSelector(
                profiles = state.workProfiles,
                activeProfileId = state.activeProfileId,
                onProfileSelect = onProfileSelect,
            )
            OutlinedTextField(
                value = state.activeProfileNameInput,
                onValueChange = onProfileNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Workplace name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            OutlinedTextField(
                value = state.activeProfileStartDateInput,
                onValueChange = onProfileStartDateChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tracking start date") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            state.profileError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB42318),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Add another workplace",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = state.newProfileNameInput,
                    onValueChange = onNewProfileNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("New workplace name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                )
                OutlinedTextField(
                    value = state.newProfileStartDateInput,
                    onValueChange = onNewProfileStartDateChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("New workplace start date") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(onClick = onProfileCreate) {
                        Text(text = "Add workplace")
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkProfileSelector(
    profiles: List<WorkProfile>,
    activeProfileId: String,
    onProfileSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        profiles.chunked(2).forEach { rowProfiles ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowProfiles.forEach { profile ->
                    FilterChip(
                        selected = profile.id == activeProfileId,
                        onClick = { onProfileSelect(profile.id) },
                        label = {
                            Text(
                                text = profile.name,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(2 - rowProfiles.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
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
    onOvertimeStartDateChange: (String) -> Unit,
    onStartingOvertimeBalanceChange: (String) -> Unit,
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
            OutlinedTextField(
                value = state.overtimeStartDateInput,
                onValueChange = onOvertimeStartDateChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Overtime balance start date") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            OutlinedTextField(
                value = state.startingOvertimeBalanceInput,
                onValueChange = onStartingOvertimeBalanceChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Starting overtime balance") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            state.overtimeSettingsError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB42318),
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
private fun ManualEntryCard(
    state: TimeClockUiState,
    onDateChange: (String) -> Unit,
    onClockInChange: (String) -> Unit,
    onClockOutChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val isEditing = state.editingSessionClockInMillis != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7FF)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (isEditing) "Edit session" else "Manual entry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = state.manualDateInput,
                onValueChange = onDateChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.manualClockInInput,
                    onValueChange = onClockInChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Clock in") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                )
                OutlinedTextField(
                    value = state.manualClockOutInput,
                    onValueChange = onClockOutChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Clock out") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                )
            }
            state.manualEntryError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB42318),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (isEditing) {
                    TextButton(onClick = onCancel) {
                        Text(text = "Cancel")
                    }
                }
                Button(onClick = onSave) {
                    Text(text = if (isEditing) "Save changes" else "Add session")
                }
            }
        }
    }
}

@Composable
private fun ReportsCard(state: TimeClockUiState) {
    val reports = buildReports(state)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6FBF9)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Reports",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            reports.forEach { report ->
                ReportRow(report = report)
            }
        }
    }
}

@Composable
private fun ReportRow(report: WorkReport) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = report.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = formatReportBalance(report),
                style = MaterialTheme.typography.bodyMedium,
                color = if (report.balanceDuration.isNegative) {
                    Color(0xFFB42318)
                } else {
                    Color(0xFF0F766E)
                },
                fontWeight = FontWeight.Medium,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Actual ${formatHoursAndMinutes(report.actualDuration)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Expected ${formatHoursAndMinutes(report.expectedDuration)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChartsCard(state: TimeClockUiState) {
    val dailyEntries = buildDailyChartEntries(state)
    val weekReport = buildCurrentWeekReport(state)
    val monthlyTrend = buildMonthlyTrendEntries(state)
    val calendarDays = buildCalendarVisualDays(state)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F8FF)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Charts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            DailyHoursChart(entries = dailyEntries)
            WeeklyProgressChart(report = weekReport)
            MonthlyTrendChart(entries = monthlyTrend)
            CalendarVisualGrid(days = calendarDays)
        }
    }
}

@Composable
private fun DailyHoursChart(entries: List<DailyChartEntry>) {
    val maxMinutes = entries
        .flatMap { listOf(it.actualDuration.toMinutes(), it.expectedDuration.toMinutes()) }
        .maxOrNull()
        ?.coerceAtLeast(60L)
        ?: 60L

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Last 7 days",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            entries.forEach { entry ->
                DailyBar(
                    entry = entry,
                    maxMinutes = maxMinutes,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DailyBar(
    entry: DailyChartEntry,
    maxMinutes: Long,
    modifier: Modifier = Modifier,
) {
    val actualFraction = (entry.actualDuration.toMinutes().toFloat() / maxMinutes).coerceIn(0.02f, 1f)
    val expectedFraction = (entry.expectedDuration.toMinutes().toFloat() / maxMinutes).coerceIn(0f, 1f)
    val barColor = colorForBalance(entry.balanceDuration, entry.expectedDuration)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(94.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(expectedFraction)
                    .background(Color(0xFFE5E7EB), RoundedCornerShape(6.dp)),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(actualFraction)
                    .background(barColor, RoundedCornerShape(6.dp)),
            )
        }
        Text(
            text = entry.date.dayOfWeek.shortLabel(),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = formatDurationShort(entry.actualDuration),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WeeklyProgressChart(report: WorkReport) {
    val expectedMinutes = report.expectedDuration.toMinutes().coerceAtLeast(1L)
    val progress = (report.actualDuration.toMinutes().toFloat() / expectedMinutes).coerceIn(0f, 1f)
    val progressColor = colorForBalance(report.balanceDuration, report.expectedDuration)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "This week",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = formatSignedBalance(report.balanceDuration),
                style = MaterialTheme.typography.bodyMedium,
                color = progressColor,
                fontWeight = FontWeight.Medium,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(progressColor, RoundedCornerShape(8.dp)),
            )
        }
        Text(
            text = "${formatHoursAndMinutes(report.actualDuration)} of ${formatHoursAndMinutes(report.expectedDuration)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MonthlyTrendChart(entries: List<MonthlyTrendEntry>) {
    val maxMagnitude = entries
        .maxOfOrNull { it.balanceDuration.abs().toMinutes() }
        ?.coerceAtLeast(60L)
        ?: 60L

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Monthly overtime trend",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        entries.forEach { entry ->
            MonthlyTrendRow(entry = entry, maxMagnitude = maxMagnitude)
        }
    }
}

@Composable
private fun MonthlyTrendRow(
    entry: MonthlyTrendEntry,
    maxMagnitude: Long,
) {
    val magnitude = (entry.balanceDuration.abs().toMinutes().toFloat() / maxMagnitude).coerceIn(0.04f, 1f)
    val barColor = colorForBalance(entry.balanceDuration, Duration.ZERO)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = entry.label,
            modifier = Modifier.width(48.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .background(Color(0xFFE5E7EB), RoundedCornerShape(5.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(magnitude)
                    .background(barColor, RoundedCornerShape(5.dp)),
            )
        }
        Text(
            text = formatSignedBalance(entry.balanceDuration),
            modifier = Modifier.width(72.dp),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
            color = barColor,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun CalendarVisualGrid(days: List<CalendarDayVisual>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "This month",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            CalendarLegend()
        }
        days.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(colorForDayStatus(day.status), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorForDayText(day.status),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                repeat(7 - week.size) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LegendDot(color = Color(0xFFB42318), label = "Missing")
        LegendDot(color = Color(0xFF0F766E), label = "OK")
        LegendDot(color = Color(0xFF2563EB), label = "Over")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(8.dp)
                .background(color, CircleShape),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OvertimeBalanceCard(
    state: TimeClockUiState,
    onOvertimeRangeChange: (OvertimeRange) -> Unit,
) {
    val balance = buildOvertimeBalance(state)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F4FF)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Overtime balance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OvertimeRangeRow(
                    ranges = OVERTIME_RANGE_ROW_ONE,
                    selectedRange = state.selectedOvertimeRange,
                    onOvertimeRangeChange = onOvertimeRangeChange,
                )
                OvertimeRangeRow(
                    ranges = OVERTIME_RANGE_ROW_TWO,
                    selectedRange = state.selectedOvertimeRange,
                    onOvertimeRangeChange = onOvertimeRangeChange,
                )
            }
            Text(
                text = formatSignedBalance(balance.totalBalance),
                fontSize = 34.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                color = if (balance.totalBalance.isNegative) {
                    Color(0xFFB42318)
                } else {
                    Color(0xFF0F766E)
                },
            )
            Text(
                text = "${balance.range.label} since ${formatDateInput(balance.startDate)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TimeStamp(label = "Actual", value = formatHoursAndMinutes(balance.actualDuration))
                TimeStamp(label = "Expected", value = formatHoursAndMinutes(balance.expectedDuration))
                TimeStamp(label = "Start", value = formatSignedBalance(balance.startingBalance))
            }
            Text(
                text = "Period balance ${formatSignedBalance(balance.periodBalance)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OvertimeRangeRow(
    ranges: List<OvertimeRange>,
    selectedRange: OvertimeRange,
    onOvertimeRangeChange: (OvertimeRange) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ranges.forEach { range ->
            FilterChip(
                selected = range == selectedRange,
                onClick = { onOvertimeRangeChange(range) },
                label = {
                    Text(
                        text = range.label,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HistoryCard(
    state: TimeClockUiState,
    onHistoryDayToggle: (LocalDate) -> Unit,
    onSessionEdit: (WorkSession) -> Unit,
    onSessionDelete: (WorkSession) -> Unit,
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
                        onSessionEdit = onSessionEdit,
                        onSessionDelete = onSessionDelete,
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
    onSessionEdit: (WorkSession) -> Unit,
    onSessionDelete: (WorkSession) -> Unit,
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
                HistorySessionRow(
                    session = session,
                    onEdit = { onSessionEdit(session) },
                    onDelete = { onSessionDelete(session) },
                )
            }
        }
    }
}

@Composable
private fun HistorySessionRow(
    session: WorkSession,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(start = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onEdit) {
                Text(text = "Edit")
            }
            TextButton(onClick = onDelete) {
                Text(text = "Delete")
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
            val expectedDuration = expectedDurationForRange(date, date, state)

            WorkDayHistory(
                date = date,
                sessions = sortedSessions,
                totalDuration = totalDuration,
                expectedDuration = expectedDuration,
            )
        }
        .sortedByDescending { it.date }
}

private fun buildReports(state: TimeClockUiState): List<WorkReport> {
    val today = LocalDate.now()
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)
    val monthStart = today.withDayOfMonth(1)
    val monthEnd = today.withDayOfMonth(today.lengthOfMonth())
    val halfYearStart = if (today.monthValue <= 6) {
        LocalDate.of(today.year, 1, 1)
    } else {
        LocalDate.of(today.year, 7, 1)
    }
    val halfYearEnd = if (today.monthValue <= 6) {
        LocalDate.of(today.year, 6, 30)
    } else {
        LocalDate.of(today.year, 12, 31)
    }
    val yearStart = LocalDate.of(today.year, 1, 1)
    val yearEnd = LocalDate.of(today.year, 12, 31)

    return listOf(
        buildReport("Today", today, today, state),
        buildReport("This week", weekStart, weekEnd, state),
        buildReport("This month", monthStart, monthEnd, state),
        buildReport("Half year", halfYearStart, halfYearEnd, state),
        buildReport("This year", yearStart, yearEnd, state),
    )
}

private fun buildOvertimeBalance(state: TimeClockUiState): OvertimeBalance {
    val allTimeStartDate = runCatching { LocalDate.parse(state.overtimeStartDateInput) }
        .getOrNull()
        ?: state.activeProfile.trackingStartDate
    val today = LocalDate.now()
    val startDate = startDateForOvertimeRange(
        range = state.selectedOvertimeRange,
        allTimeStartDate = maxOf(state.activeProfile.trackingStartDate, allTimeStartDate),
        today = today,
    ).coerceAtLeast(state.activeProfile.trackingStartDate)
    val actualDuration = actualDurationForRange(startDate, today, state)
    val expectedDuration = expectedDurationForRange(startDate, today, state)
    val startingBalance = if (state.selectedOvertimeRange == OvertimeRange.ALL_TIME) {
        state.startingOvertimeBalanceInput.toSignedDurationOrNull() ?: Duration.ZERO
    } else {
        Duration.ZERO
    }

    return OvertimeBalance(
        range = state.selectedOvertimeRange,
        startDate = startDate,
        actualDuration = actualDuration,
        expectedDuration = expectedDuration,
        startingBalance = startingBalance,
    )
}

private fun startDateForOvertimeRange(
    range: OvertimeRange,
    allTimeStartDate: LocalDate,
    today: LocalDate,
): LocalDate {
    return when (range) {
        OvertimeRange.TODAY -> today
        OvertimeRange.ONE_WEEK -> today.minusWeeks(1).plusDays(1)
        OvertimeRange.FOUR_WEEKS -> today.minusWeeks(4).plusDays(1)
        OvertimeRange.ONE_MONTH -> today.minusMonths(1).plusDays(1)
        OvertimeRange.SIX_MONTHS -> today.minusMonths(6).plusDays(1)
        OvertimeRange.TWELVE_MONTHS -> today.minusMonths(12).plusDays(1)
        OvertimeRange.ALL_TIME -> allTimeStartDate
    }
}

private fun buildDailyChartEntries(state: TimeClockUiState): List<DailyChartEntry> {
    val today = LocalDate.now()
    return (6L downTo 0L).map { daysAgo ->
        val date = today.minusDays(daysAgo)
        DailyChartEntry(
            date = date,
            actualDuration = actualDurationForRange(date, date, state),
            expectedDuration = expectedDurationForRange(date, date, state),
        )
    }
}

private fun buildCurrentWeekReport(state: TimeClockUiState): WorkReport {
    val today = LocalDate.now()
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)
    return buildReport("This week", weekStart, weekEnd, state)
}

private fun buildMonthlyTrendEntries(state: TimeClockUiState): List<MonthlyTrendEntry> {
    val today = LocalDate.now()
    val monthStart = today.withDayOfMonth(1)
    val monthEnd = today.withDayOfMonth(today.lengthOfMonth())
    val entries = mutableListOf<MonthlyTrendEntry>()
    var weekStart = monthStart
    var weekNumber = 1

    while (!weekStart.isAfter(monthEnd)) {
        val weekEnd = minOf(weekStart.plusDays(6), monthEnd)
        val actualDuration = actualDurationForRange(weekStart, weekEnd, state)
        val expectedDuration = expectedDurationForRange(weekStart, weekEnd, state)
        entries.add(
            MonthlyTrendEntry(
                label = "W$weekNumber",
                balanceDuration = actualDuration.minus(expectedDuration),
            ),
        )
        weekStart = weekEnd.plusDays(1)
        weekNumber += 1
    }

    return entries
}

private fun buildCalendarVisualDays(state: TimeClockUiState): List<CalendarDayVisual> {
    val today = LocalDate.now()
    val monthStart = today.withDayOfMonth(1)
    val monthEnd = today.withDayOfMonth(today.lengthOfMonth())
    val days = mutableListOf<CalendarDayVisual>()
    var date = monthStart

    while (!date.isAfter(monthEnd)) {
        days.add(
            CalendarDayVisual(
                date = date,
                actualDuration = actualDurationForRange(date, date, state),
                expectedDuration = expectedDurationForRange(date, date, state),
            ),
        )
        date = date.plusDays(1)
    }

    return days
}

private fun buildReport(
    label: String,
    startDate: LocalDate,
    endDate: LocalDate,
    state: TimeClockUiState,
): WorkReport {
    val actualDuration = actualDurationForRange(startDate, endDate, state)
    val expectedDuration = expectedDurationForRange(startDate, endDate, state)

    return WorkReport(
        label = label,
        actualDuration = actualDuration,
        expectedDuration = expectedDuration,
    )
}

private fun actualDurationForRange(
    startDate: LocalDate,
    endDate: LocalDate,
    state: TimeClockUiState,
): Duration {
    val zoneId = ZoneId.systemDefault()
    val effectiveStartDate = startDate.coerceAtLeast(state.activeProfile.trackingStartDate)
    if (effectiveStartDate.isAfter(endDate)) return Duration.ZERO

    val rangeStart = effectiveStartDate.atStartOfDay(zoneId).toInstant()
    val rangeEnd = endDate.plusDays(1).atStartOfDay(zoneId).toInstant()
    val activeSession = state.clockInTime?.let { WorkSession(it, Instant.now()) }
    val sessions = state.completedSessions + listOfNotNull(activeSession)

    return sessions.fold(Duration.ZERO) { total, session ->
        val overlapStart = maxOf(session.clockIn, rangeStart)
        val overlapEnd = minOf(session.clockOut, rangeEnd)
        if (overlapEnd > overlapStart) {
            total.plus(Duration.between(overlapStart, overlapEnd))
        } else {
            total
        }
    }
}

private fun expectedDurationForRange(
    startDate: LocalDate,
    endDate: LocalDate,
    state: TimeClockUiState,
): Duration {
    var date = startDate.coerceAtLeast(state.activeProfile.trackingStartDate)
    if (date.isAfter(endDate)) return Duration.ZERO

    val dailyExpected = state.expectedDailyDuration.plus(
        if (state.deductUnpaidLunchBreak) state.lunchBreakDuration else Duration.ZERO,
    )
    var expected = Duration.ZERO

    while (!date.isAfter(endDate)) {
        if (date.dayOfWeek in state.workDays) {
            expected = expected.plus(dailyExpected)
        }
        date = date.plusDays(1)
    }

    return expected
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

private fun formatReportBalance(report: WorkReport): String {
    if (report.expectedDuration == Duration.ZERO) {
        return "No target"
    }

    return when {
        report.balanceDuration.isNegative -> "${formatHoursAndMinutes(report.balanceDuration.abs())} missing"
        report.balanceDuration == Duration.ZERO -> "On target"
        else -> "${formatHoursAndMinutes(report.balanceDuration)} ahead"
    }
}

private fun colorForBalance(
    balanceDuration: Duration,
    expectedDuration: Duration,
): Color {
    return when {
        expectedDuration == Duration.ZERO && balanceDuration > Duration.ZERO -> Color(0xFF2563EB)
        balanceDuration.isNegative -> Color(0xFFB42318)
        balanceDuration == Duration.ZERO -> Color(0xFF0F766E)
        else -> Color(0xFF2563EB)
    }
}

private fun colorForDayStatus(status: DayVisualStatus): Color {
    return when (status) {
        DayVisualStatus.MISSING -> Color(0xFFFEE4E2)
        DayVisualStatus.ON_TARGET -> Color(0xFFD1FAE5)
        DayVisualStatus.OVERTIME -> Color(0xFFDBEAFE)
        DayVisualStatus.NO_TARGET -> Color(0xFFF3F4F6)
    }
}

private fun colorForDayText(status: DayVisualStatus): Color {
    return when (status) {
        DayVisualStatus.MISSING -> Color(0xFFB42318)
        DayVisualStatus.ON_TARGET -> Color(0xFF047857)
        DayVisualStatus.OVERTIME -> Color(0xFF1D4ED8)
        DayVisualStatus.NO_TARGET -> Color(0xFF4B5563)
    }
}

private fun formatSignedBalance(duration: Duration): String {
    return when {
        duration.isNegative -> "-${formatHoursAndMinutes(duration.abs())}"
        duration == Duration.ZERO -> "0h 00m"
        else -> "+${formatHoursAndMinutes(duration)}"
    }
}

private fun String.toSignedDurationOrNull(): Duration? {
    val trimmed = trim()
    if (trimmed.isBlank()) return Duration.ZERO

    val isNegative = trimmed.startsWith("-")
    val unsigned = trimmed.removePrefix("-").removePrefix("+")
    val duration = parseDurationInput(unsigned) ?: return null
    return if (isNegative) duration.negated() else duration
}

private fun parseDurationInput(input: String): Duration? {
    val normalized = input.trim().lowercase().replace(',', '.')
    if (normalized.isBlank()) return null

    val minutes = when {
        ":" in normalized -> parseColonDurationInput(normalized)
        "h" in normalized || "m" in normalized -> parseLabeledDurationInput(normalized)
        normalized.contains(" ") -> parseSpacedDurationInput(normalized)
        else -> normalized.toDoubleOrNull()?.let { (it * 60).toLong() }
    } ?: return null

    if (minutes < 0L || minutes > MAX_EXPECTED_MINUTES) return null
    return Duration.ofMinutes(minutes)
}

private fun parseColonDurationInput(input: String): Long? {
    val parts = input.split(":")
    val hours = parts.getOrNull(0)?.trim()?.toLongOrNull() ?: return null
    val minutes = parts.getOrNull(1)?.trim()?.toLongOrNull() ?: 0L
    if (parts.size > 2 || minutes !in 0L..59L) return null
    return hours * 60 + minutes
}

private fun parseLabeledDurationInput(input: String): Long? {
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

private fun parseSpacedDurationInput(input: String): Long? {
    val parts = input.split(Regex("""\s+""")).filter { it.isNotBlank() }
    val hours = parts.getOrNull(0)?.toLongOrNull() ?: return null
    val minutes = parts.getOrNull(1)?.toLongOrNull() ?: 0L
    if (parts.size > 2 || minutes !in 0L..59L) return null
    return hours * 60 + minutes
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

private fun formatDateInput(date: LocalDate): String {
    return DATE_INPUT_FORMATTER.format(date)
}

private val TimeClockUiState.activeProfile: WorkProfile
    get() = workProfiles.firstOrNull { it.id == activeProfileId } ?: workProfiles.first()

private fun List<WorkProfile>.replaceProfile(updatedProfile: WorkProfile): List<WorkProfile> {
    return map { profile ->
        if (profile.id == updatedProfile.id) updatedProfile else profile
    }
}

private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val TIME_INPUT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
private val DATE_INPUT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val HISTORY_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")
private const val DEFAULT_WORK_PROFILE_ID = "default_profile"
private const val DEFAULT_WORK_PROFILE_NAME = "My workplace"
private val DEFAULT_WORK_PROFILE = WorkProfile(
    id = DEFAULT_WORK_PROFILE_ID,
    name = DEFAULT_WORK_PROFILE_NAME,
    trackingStartDate = LocalDate.now(),
)
private val DEFAULT_DAILY_HOURS_INPUT = "7:30"
private val DEFAULT_WEEKLY_HOURS_INPUT = "37:30"
private val DEFAULT_DAILY_DURATION = Duration.ofHours(7).plusMinutes(30)
private val MAX_EXPECTED_MINUTES = 7L * 24L * 60L
private val DEFAULT_LUNCH_BREAK_MINUTES = 30L
private val DEFAULT_LUNCH_BREAK_MINUTES_INPUT = "30"
private val DEFAULT_STARTING_OVERTIME_BALANCE_INPUT = "0:00"
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
private val OVERTIME_RANGE_ROW_ONE = listOf(
    OvertimeRange.TODAY,
    OvertimeRange.ONE_WEEK,
    OvertimeRange.FOUR_WEEKS,
    OvertimeRange.ONE_MONTH,
)
private val OVERTIME_RANGE_ROW_TWO = listOf(
    OvertimeRange.SIX_MONTHS,
    OvertimeRange.TWELVE_MONTHS,
    OvertimeRange.ALL_TIME,
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
            onProfileSelect = {},
            onProfileNameChange = {},
            onProfileStartDateChange = {},
            onNewProfileNameChange = {},
            onNewProfileStartDateChange = {},
            onProfileCreate = {},
            onHistoryDayToggle = {},
            onManualDateChange = {},
            onManualClockInChange = {},
            onManualClockOutChange = {},
            onManualSessionSave = {},
            onManualSessionCancel = {},
            onSessionEdit = {},
            onSessionDelete = {},
            onSettingsExpandedToggle = {},
            onExpectedDailyHoursChange = {},
            onExpectedWeeklyHoursChange = {},
            onWorkdayToggle = {},
            onUnpaidLunchBreakToggle = {},
            onLunchBreakMinutesChange = {},
            onOvertimeStartDateChange = {},
            onStartingOvertimeBalanceChange = {},
            onOvertimeRangeChange = {},
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
            onProfileSelect = {},
            onProfileNameChange = {},
            onProfileStartDateChange = {},
            onNewProfileNameChange = {},
            onNewProfileStartDateChange = {},
            onProfileCreate = {},
            onHistoryDayToggle = {},
            onManualDateChange = {},
            onManualClockInChange = {},
            onManualClockOutChange = {},
            onManualSessionSave = {},
            onManualSessionCancel = {},
            onSessionEdit = {},
            onSessionDelete = {},
            onSettingsExpandedToggle = {},
            onExpectedDailyHoursChange = {},
            onExpectedWeeklyHoursChange = {},
            onWorkdayToggle = {},
            onUnpaidLunchBreakToggle = {},
            onLunchBreakMinutesChange = {},
            onOvertimeStartDateChange = {},
            onStartingOvertimeBalanceChange = {},
            onOvertimeRangeChange = {},
        )
    }
}
