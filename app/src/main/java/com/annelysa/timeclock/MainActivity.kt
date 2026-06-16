package com.annelysa.timeclock

import android.Manifest
import android.app.Application
import android.app.DatePickerDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
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
    private val timeClockViewModel: TimeClockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createReminderNotificationChannel()
        requestNotificationPermissionIfNeeded()

        setContent {
            TimeClockTheme {
                val viewModel = timeClockViewModel
                val state by viewModel.uiState.collectAsState()

                TimeClockScreen(
                    state = state,
                    onTabSelect = viewModel::selectTab,
                    onClockIn = viewModel::clockIn,
                    onClockOut = viewModel::clockOut,
                    onActiveClockInChange = viewModel::updateActiveClockInEditInput,
                    onActiveClockInSave = viewModel::saveActiveClockInTime,
                    onRecentClockOutChange = viewModel::updateRecentClockOutEditInput,
                    onRecentClockOutSave = viewModel::saveRecentClockOutTime,
                    onTodayOvertimeRangeChange = viewModel::updateTodayOvertimeRange,
                    onTodayOvertimeStartDateChange = viewModel::updateTodayOvertimeStartDate,
                    onTodayOvertimeEndDateChange = viewModel::updateTodayOvertimeEndDate,
                    onInsightsSectionSelect = viewModel::selectInsightsSection,
                    onExportExpandToggle = viewModel::toggleExportExpanded,
                    onProfileSelect = viewModel::selectProfile,
                    onProfileNameChange = viewModel::updateActiveProfileName,
                    onProfileStartDateChange = viewModel::updateActiveProfileStartDate,
                    onWorkplaceTypeSelect = viewModel::updateWorkplaceType,
                    onMonthlySalaryChange = viewModel::updateMonthlySalary,
                    onHourlyRateChange = viewModel::updateHourlyRate,
                    onCurrencyChange = viewModel::updateCurrency,
                    onNewProfileNameChange = viewModel::updateNewProfileName,
                    onNewProfileStartDateChange = viewModel::updateNewProfileStartDate,
                    onProfileCreate = viewModel::createProfile,
                    onProfileDelete = viewModel::deleteActiveProfile,
                    onProfileStopTracking = viewModel::stopTrackingActiveProfile,
                    onProfileReactivate = viewModel::reactivateActiveProfile,
                    onHistoryDayToggle = viewModel::toggleHistoryDay,
                    onManualDateChange = viewModel::updateManualDate,
                    onManualClockInChange = viewModel::updateManualClockIn,
                    onManualClockOutChange = viewModel::updateManualClockOut,
                    onManualNoteChange = viewModel::updateManualNote,
                    onManualSessionSave = viewModel::saveManualSession,
                    onManualSessionCancel = viewModel::cancelManualEdit,
                    onAbsenceDateChange = viewModel::updateAbsenceDate,
                    onAbsenceEndDateChange = viewModel::updateAbsenceEndDate,
                    onAbsenceTypeSelect = viewModel::selectAbsenceType,
                    onAbsenceHoursChange = viewModel::updateAbsenceHours,
                    onAbsenceNoteChange = viewModel::updateAbsenceNote,
                    onAbsenceSave = viewModel::saveAbsence,
                    onAbsenceDelete = viewModel::deleteAbsence,
                    onSessionEdit = viewModel::startEditingSession,
                    onSessionDelete = viewModel::deleteSession,
                    onExpectedDailyHoursChange = viewModel::updateExpectedDailyHours,
                    onExpectedWeeklyHoursChange = viewModel::updateExpectedWeeklyHours,
                    onWorkdayToggle = viewModel::toggleWorkday,
                    onUnpaidLunchBreakToggle = viewModel::toggleUnpaidLunchBreak,
                    onLunchBreakMinutesChange = viewModel::updateLunchBreakMinutes,
                    onOvertimeStartDateChange = viewModel::updateOvertimeStartDate,
                    onStartingOvertimeBalanceChange = viewModel::updateStartingOvertimeBalance,
                    onOvertimeRangeChange = viewModel::updateOvertimeRange,
                    onShareCsvText = { shareCsvTextExport(state) },
                    onShareCsvFile = { shareCsvFileExport(state) },
                    onSaveCsv = { saveCsvExport(state) },
                    onSharePdf = { sharePdfExport(state) },
                    onSavePdf = { savePdfExport(state) },
                    onExportOptionToggle = viewModel::toggleExportOption,
                    onExportRangeModeSelect = viewModel::selectExportRangeMode,
                    onExportStartDateChange = viewModel::updateExportStartDate,
                    onExportEndDateChange = viewModel::updateExportEndDate,
                    onClockInReminderToggle = viewModel::toggleClockInReminder,
                    onClockInReminderTimeChange = viewModel::updateClockInReminderTime,
                    onClockOutReminderToggle = viewModel::toggleClockOutReminder,
                    onActiveSessionNoteChange = viewModel::updateActiveSessionNote,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        timeClockViewModel.refreshFromStorage()
    }

    private fun shareCsvTextExport(state: TimeClockUiState) {
        val csv = buildExportCsv(state)
        val fileName = "time-clock-${state.activeProfile.name.toFileNamePart()}-${formatDateInput(LocalDate.now())}.csv"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            putExtra(Intent.EXTRA_TITLE, fileName)
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        startActivity(Intent.createChooser(shareIntent, "Share CSV text"))
    }

    private fun shareCsvFileExport(state: TimeClockUiState) {
        val fileName = exportFileName(state, "csv")
        val exportDir = File(cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)
        file.writeText(buildExportCsv(state))
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Time report: ${state.activeProfile.name}")
            putExtra(Intent.EXTRA_TITLE, fileName)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share CSV file"))
    }

    private fun saveCsvExport(state: TimeClockUiState) {
        val fileName = exportFileName(state, "csv")
        val bytes = buildExportCsv(state).toByteArray()
        saveBytesToDownloads(
            fileName = fileName,
            mimeType = "text/csv",
            bytes = bytes,
            successMessage = "CSV saved to Downloads",
            fallback = { shareCsvFileExport(state) },
        )
    }

    private fun sharePdfExport(state: TimeClockUiState) {
        val fileName = exportFileName(state, "pdf")
        val exportDir = File(cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)
        file.writeBytes(buildExportPdfBytes(state))
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "Time report: ${state.activeProfile.name}")
            putExtra(Intent.EXTRA_TITLE, fileName)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share PDF report"))
    }

    private fun savePdfExport(state: TimeClockUiState) {
        saveBytesToDownloads(
            fileName = exportFileName(state, "pdf"),
            mimeType = "application/pdf",
            bytes = buildExportPdfBytes(state),
            successMessage = "PDF saved to Downloads",
            fallback = { sharePdfExport(state) },
        )
    }

    private fun saveBytesToDownloads(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        successMessage: String,
        fallback: () -> Unit,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri: Uri? = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(bytes)
                }
                Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Could not save file", Toast.LENGTH_LONG).show()
            }
        } else {
            fallback()
        }
    }

    private fun exportFileName(state: TimeClockUiState, extension: String): String {
        return "time-clock-${state.activeProfile.name.toFileNamePart()}-${formatDateInput(LocalDate.now())}.$extension"
    }

    private fun createReminderNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            REMINDER_NOTIFICATION_CHANNEL_ID,
            "Time Clock reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Clock-out reminders and active session alerts"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE)
        }
    }
}

private const val DEFAULT_CURRENCY_INPUT = "DKK"

data class TimeClockUiState(
    val selectedTab: AppTab = AppTab.TODAY,
    val workProfiles: List<WorkProfile> = listOf(DEFAULT_WORK_PROFILE),
    val activeProfileId: String = DEFAULT_WORK_PROFILE.id,
    val activeProfileNameInput: String = DEFAULT_WORK_PROFILE.name,
    val activeProfileStartDateInput: String = formatDateInput(DEFAULT_WORK_PROFILE.trackingStartDate),
    val workplaceType: WorkplaceType = WorkplaceType.FIXED_HOURS_FIXED_PAY,
    val monthlySalaryInput: String = "",
    val hourlyRateInput: String = "",
    val currencyInput: String = DEFAULT_CURRENCY_INPUT,
    val paySettingsError: String? = null,
    val newProfileNameInput: String = "",
    val newProfileStartDateInput: String = formatDateInput(LocalDate.now()),
    val profileError: String? = null,
    val isClockedIn: Boolean = false,
    val clockInTime: Instant? = null,
    val activeDuration: Duration = Duration.ZERO,
    val activeClockInEditInput: String = "",
    val activeClockInEditError: String? = null,
    val activeSessionNoteInput: String = "",
    val recentClockOutEditInput: String = "",
    val recentClockOutEditError: String? = null,
    val todayTotalDuration: Duration = Duration.ZERO,
    val todayCreditedDuration: Duration = Duration.ZERO,
    val todayBreakDeduction: Duration = Duration.ZERO,
    val todaySessionCount: Int = 0,
    val todayFirstClockIn: Instant? = null,
    val todayLastClockOut: Instant? = null,
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
    val selectedTodayOvertimeRange: TodayOvertimeRange = TodayOvertimeRange.ONE_WEEK,
    val todayOvertimeStartDateInput: String = formatDateInput(LocalDate.now().minusWeeks(1).plusDays(1)),
    val todayOvertimeEndDateInput: String = formatDateInput(LocalDate.now()),
    val todayOvertimeRangeError: String? = null,
    val manualDateInput: String = formatDateInput(LocalDate.now()),
    val manualClockInInput: String = "",
    val manualClockOutInput: String = "",
    val manualNoteInput: String = "",
    val manualEntryError: String? = null,
    val editingSessionClockInMillis: Long? = null,
    val editingSessionClockOutMillis: Long? = null,
    val absences: List<AbsenceEntry> = emptyList(),
    val absenceDateInput: String = formatDateInput(LocalDate.now()),
    val absenceEndDateInput: String = formatDateInput(LocalDate.now()),
    val selectedAbsenceType: AbsenceType = AbsenceType.VACATION,
    val absenceHoursInput: String = "1:00",
    val absenceNoteInput: String = "",
    val absenceEntryError: String? = null,
    val overtimeStartDateInput: String = formatDateInput(LocalDate.now()),
    val startingOvertimeBalanceInput: String = "0:00",
    val selectedOvertimeRange: OvertimeRange = OvertimeRange.ALL_TIME,
    val overtimeSettingsError: String? = null,
    val clockInReminderEnabled: Boolean = false,
    val clockInReminderTimeInput: String = DEFAULT_CLOCK_IN_REMINDER_INPUT,
    val clockOutReminderEnabled: Boolean = false,
    val clockOutReminderSentMask: Int = 0,
    val reminderSettingsError: String? = null,
    val selectedInsightsSection: InsightsSection = InsightsSection.SUMMARY,
    val isExportExpanded: Boolean = false,
    val exportOptions: ExportOptions = ExportOptions(),
    val exportRangeMode: ExportRangeMode = ExportRangeMode.ALL_REGISTERED,
    val exportStartDateInput: String = formatDateInput(LocalDate.now()),
    val exportEndDateInput: String = formatDateInput(LocalDate.now()),
    val exportPeriodError: String? = null,
)

data class WorkProfile(
    val id: String,
    val name: String,
    val trackingStartDate: LocalDate,
    val trackingEndDate: LocalDate? = null,
)

data class WorkSession(
    val clockIn: Instant,
    val clockOut: Instant,
    val note: String = "",
) {
    val duration: Duration = Duration.between(clockIn, clockOut).coerceAtLeast(Duration.ZERO)
}

data class WorkDayHistory(
    val date: LocalDate,
    val sessions: List<WorkSession>,
    val absences: List<AbsenceEntry>,
    val totalDuration: Duration,
    val expectedDuration: Duration,
) {
    val balanceDuration: Duration = totalDuration.minus(expectedDuration)
}

data class AbsenceEntry(
    val date: LocalDate,
    val type: AbsenceType,
    val duration: Duration = Duration.ZERO,
    val note: String = "",
)

enum class AbsenceType(
    val label: String,
    val coversExpectedHours: Boolean,
) {
    VACATION("Holiday", true),
    SICK_DAY("Sick day", true),
    NO_WORK("No work", true),
}

data class WorkReport(
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val actualDuration: Duration,
    val expectedDuration: Duration,
) {
    val balanceDuration: Duration = actualDuration.minus(expectedDuration)
}

data class EarningsRow(
    val label: String,
    val amount: Double,
    val basis: String,
)

data class ExportOptions(
    val includeWorkplaceSettings: Boolean = true,
    val includeReportSummaries: Boolean = true,
    val includeOvertimeBalance: Boolean = true,
    val includeEarnings: Boolean = true,
    val includeSessions: Boolean = true,
    val includeAbsences: Boolean = true,
    val includeNotes: Boolean = true,
)

enum class ExportOption(
    val label: String,
) {
    WORKPLACE_SETTINGS("Workplace settings"),
    REPORT_SUMMARIES("Report summaries"),
    OVERTIME_BALANCE("Overtime balance"),
    EARNINGS("Earnings"),
    SESSIONS("Work sessions"),
    ABSENCES("Absences"),
    NOTES("Notes"),
}

enum class ExportRangeMode(val label: String) {
    ALL_REGISTERED("All registered time"),
    CUSTOM("Specific period"),
}

data class ExportPeriod(
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

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

data class TodayOvertimeBalance(
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
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

enum class TodayOvertimeRange(val label: String) {
    ONE_WEEK("1 week"),
    FOUR_WEEKS("4 weeks"),
    SIX_MONTHS("6 months"),
    TWELVE_MONTHS("1 year"),
    ALL_TIME("All time"),
    CUSTOM("Custom"),
}

enum class WorkplaceType(
    val label: String,
    val description: String,
) {
    FIXED_HOURS_FIXED_PAY(
        "Fixed hours + fixed pay",
        "Regular job with expected hours, overtime balance, and optional salary",
    ),
    HOURLY_PAID(
        "Hourly paid",
        "Consultant, shift, or hourly work where earnings follow worked hours",
    ),
    TIME_TRACKING_ONLY(
        "Time tracking only",
        "Own business, projects, or unpaid work where you only track time",
    ),
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

data class LongSessionAlert(
    val index: Int,
    val overtimeDuration: Duration,
)

enum class AppTab(
    val label: String,
    val icon: ImageVector,
) {
    TODAY("Today", Icons.Default.Today),
    HISTORY("History", Icons.Default.History),
    INSIGHTS("Insights", Icons.Default.BarChart),
    SETTINGS("Settings", Icons.Default.Settings),
}

enum class InsightsSection(val label: String) {
    SUMMARY("Summary"),
    CHARTS("Charts"),
    EXPORT("Export"),
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

    fun selectTab(tab: AppTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectInsightsSection(section: InsightsSection) {
        _uiState.value = _uiState.value.copy(selectedInsightsSection = section)
    }

    fun toggleExportExpanded() {
        _uiState.value = _uiState.value.copy(isExportExpanded = !_uiState.value.isExportExpanded)
    }

    fun updateTodayOvertimeRange(range: TodayOvertimeRange) {
        _uiState.value = _uiState.value.copy(
            selectedTodayOvertimeRange = range,
            todayOvertimeRangeError = todayOvertimeRangeError(
                range = range,
                startInput = _uiState.value.todayOvertimeStartDateInput,
                endInput = _uiState.value.todayOvertimeEndDateInput,
            ),
        )
    }

    fun updateTodayOvertimeStartDate(input: String) {
        val sanitizedInput = input.take(10)
        _uiState.value = _uiState.value.copy(
            todayOvertimeStartDateInput = sanitizedInput,
            todayOvertimeRangeError = todayOvertimeRangeError(
                range = _uiState.value.selectedTodayOvertimeRange,
                startInput = sanitizedInput,
                endInput = _uiState.value.todayOvertimeEndDateInput,
            ),
        )
    }

    fun updateTodayOvertimeEndDate(input: String) {
        val sanitizedInput = input.take(10)
        _uiState.value = _uiState.value.copy(
            todayOvertimeEndDateInput = sanitizedInput,
            todayOvertimeRangeError = todayOvertimeRangeError(
                range = _uiState.value.selectedTodayOvertimeRange,
                startInput = _uiState.value.todayOvertimeStartDateInput,
                endInput = sanitizedInput,
            ),
        )
    }

    fun refreshFromStorage() {
        val state = _uiState.value
        val activeClockIn = preferences.getLongOrNull(profileKey(state.activeProfileId, KEY_ACTIVE_CLOCK_IN))
            .let { activeProfileClockIn ->
                activeProfileClockIn ?: if (state.activeProfileId == DEFAULT_WORK_PROFILE_ID) {
                    preferences.getLongOrNull(KEY_ACTIVE_CLOCK_IN)
                } else {
                    null
                }
            }
            ?.let(Instant::ofEpochMilli)
        val completedSessions = loadCompletedSessions(state.activeProfileId)
        val lastCompletedSession = completedSessions.maxByOrNull { it.clockOut }
        val refreshedState = withDailySummary(
            state.copy(
                isClockedIn = activeClockIn != null,
                clockInTime = activeClockIn,
                activeDuration = activeClockIn?.let {
                    Duration.between(it, Instant.now()).coerceAtLeast(Duration.ZERO)
                } ?: Duration.ZERO,
                activeClockInEditInput = activeClockIn?.let(::formatTimeInput).orEmpty(),
                activeClockInEditError = null,
                recentClockOutEditInput = if (activeClockIn == null) {
                    lastCompletedSession?.clockOut?.let(::formatTimeInput).orEmpty()
                } else {
                    state.recentClockOutEditInput
                },
                recentClockOutEditError = null,
                completedSessions = completedSessions,
                lastCompletedSession = lastCompletedSession,
                clockOutReminderSentMask = preferences.getInt(
                    profileKey(state.activeProfileId, KEY_CLOCK_OUT_REMINDER_SENT_MASK),
                    0,
                ),
            ),
        )
        _uiState.value = refreshedState
    }

    fun selectProfile(profileId: String) {
        if (profileId == _uiState.value.activeProfileId) return
        val profiles = _uiState.value.workProfiles
        val selectedProfile = profiles.firstOrNull { it.id == profileId } ?: return

        preferences.edit()
            .putString(KEY_ACTIVE_PROFILE_ID, profileId)
            .apply()

        val selectedState = buildStateForProfile(
            profiles = profiles,
            activeProfile = selectedProfile,
            newProfileNameInput = _uiState.value.newProfileNameInput,
            newProfileStartDateInput = _uiState.value.newProfileStartDateInput,
        )
        _uiState.value = selectedState
        rescheduleClockInReminder(selectedState)
        refreshHomeScreenWidgets()
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
        refreshHomeScreenWidgets()
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
        refreshHomeScreenWidgets()
    }

    fun updateWorkplaceType(type: WorkplaceType) {
        preferences.edit()
            .putString(profileKey(KEY_WORKPLACE_TYPE), type.name)
            .apply()

        _uiState.value = _uiState.value.copy(
            workplaceType = type,
            paySettingsError = null,
        )
        refreshHomeScreenWidgets()
    }

    fun updateMonthlySalary(input: String) {
        val sanitizedInput = sanitizeMoneyInput(input)
        val error = if (sanitizedInput.isNotBlank() && sanitizedInput.toMoneyOrNull() == null) {
            "Use an amount like 45000 or 45000.50."
        } else {
            null
        }

        preferences.edit()
            .putString(profileKey(KEY_MONTHLY_SALARY), sanitizedInput)
            .apply()

        _uiState.value = _uiState.value.copy(
            monthlySalaryInput = sanitizedInput,
            paySettingsError = error,
        )
        refreshHomeScreenWidgets()
    }

    fun updateHourlyRate(input: String) {
        val sanitizedInput = sanitizeMoneyInput(input)
        val error = if (sanitizedInput.isNotBlank() && sanitizedInput.toMoneyOrNull() == null) {
            "Use an amount like 650 or 650.50."
        } else {
            null
        }

        preferences.edit()
            .putString(profileKey(KEY_HOURLY_RATE), sanitizedInput)
            .apply()

        _uiState.value = _uiState.value.copy(
            hourlyRateInput = sanitizedInput,
            paySettingsError = error,
        )
        refreshHomeScreenWidgets()
    }

    fun updateCurrency(input: String) {
        val sanitizedInput = input
            .filter { it.isLetter() }
            .uppercase()
            .take(3)

        preferences.edit()
            .putString(profileKey(KEY_CURRENCY), sanitizedInput)
            .apply()

        _uiState.value = _uiState.value.copy(
            currencyInput = sanitizedInput,
            paySettingsError = null,
        )
        refreshHomeScreenWidgets()
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

        val newProfileState = buildStateForProfile(
            profiles = updatedProfiles,
            activeProfile = newProfile,
            newProfileNameInput = "",
            newProfileStartDateInput = formatDateInput(LocalDate.now()),
        )
        _uiState.value = newProfileState
        rescheduleClockInReminder(newProfileState)
    }

    fun deleteActiveProfile() {
        val state = _uiState.value
        if (state.workProfiles.size <= 1) {
            _uiState.value = state.copy(profileError = "You need at least one workplace.")
            return
        }

        val deletedProfileId = state.activeProfileId
        val updatedProfiles = state.workProfiles.filterNot { it.id == deletedProfileId }
        val nextProfile = updatedProfiles.first()

        saveProfiles(updatedProfiles)
        preferences.edit()
            .putString(KEY_ACTIVE_PROFILE_ID, nextProfile.id)
            .removeProfileData(deletedProfileId)
            .apply()

        val nextProfileState = buildStateForProfile(
            profiles = updatedProfiles,
            activeProfile = nextProfile,
            newProfileNameInput = state.newProfileNameInput,
            newProfileStartDateInput = state.newProfileStartDateInput,
        )
        _uiState.value = nextProfileState
        rescheduleClockInReminder(nextProfileState)
    }

    fun stopTrackingActiveProfile(endDate: LocalDate) {
        val state = _uiState.value
        if (endDate.isBefore(state.activeProfile.trackingStartDate)) {
            _uiState.value = state.copy(profileError = "End date cannot be before the workplace start date.")
            return
        }
        val updatedProfile = state.activeProfile.copy(trackingEndDate = endDate)
        val updatedProfiles = state.workProfiles.replaceProfile(updatedProfile)
        val activeClockIn = state.clockInTime
        val now = Instant.now()
        val completedSessions = if (activeClockIn != null && now.isAfter(activeClockIn)) {
            (state.completedSessions + WorkSession(activeClockIn, now, note = state.activeSessionNoteInput.trim())).sortedBy { it.clockIn }
        } else {
            state.completedSessions
        }

        saveProfiles(updatedProfiles)
        saveCompletedSessions(completedSessions)
        preferences.edit()
            .remove(profileKey(KEY_ACTIVE_CLOCK_IN))
            .remove(profileKey(KEY_CLOCK_OUT_REMINDER_SENT_MASK))
            .remove(KEY_ACTIVE_CLOCK_IN)
            .apply()
        activeClockIn?.let {
            cancelLongSessionReminders(
                context = getApplication<Application>(),
                profileId = state.activeProfileId,
                clockInMillis = it.toEpochMilli(),
            )
        }

        _uiState.value = withDailySummary(
            state.copy(
                workProfiles = updatedProfiles,
                activeProfileStartDateInput = formatDateInput(updatedProfile.trackingStartDate),
                isClockedIn = false,
                clockInTime = null,
                activeDuration = Duration.ZERO,
                activeClockInEditInput = "",
                activeClockInEditError = null,
                activeSessionNoteInput = "",
                completedSessions = completedSessions,
                lastCompletedSession = completedSessions.maxByOrNull { it.clockOut },
                profileError = null,
            ),
        )
        refreshHomeScreenWidgets()
    }

    fun reactivateActiveProfile() {
        val state = _uiState.value
        val updatedProfile = state.activeProfile.copy(trackingEndDate = null)
        val updatedProfiles = state.workProfiles.replaceProfile(updatedProfile)

        saveProfiles(updatedProfiles)
        _uiState.value = withDailySummary(
            state.copy(
                workProfiles = updatedProfiles,
                profileError = null,
            ),
        )
        refreshHomeScreenWidgets()
    }

    fun clockIn() {
        if (_uiState.value.isClockedIn || _uiState.value.activeProfile.trackingEndDate != null) return

        val now = Instant.now()
        preferences.edit()
            .putLong(profileKey(KEY_ACTIVE_CLOCK_IN), now.toEpochMilli())
            .remove(profileKey(KEY_CLOCK_OUT_REMINDER_SENT_MASK))
            .remove(KEY_ACTIVE_CLOCK_IN)
            .apply()

        val clockedInState = withDailySummary(
            _uiState.value.copy(
                isClockedIn = true,
                clockInTime = now,
                activeDuration = Duration.ZERO,
                activeClockInEditInput = formatTimeInput(now),
                activeClockInEditError = null,
                activeSessionNoteInput = "",
                recentClockOutEditError = null,
            ),
        )
        _uiState.value = clockedInState
        rescheduleLongSessionReminders(clockedInState)
        refreshHomeScreenWidgets()
    }

    fun clockOut() {
        val startedAt = _uiState.value.clockInTime ?: return
        val endedAt = Instant.now()
        val session = WorkSession(startedAt, endedAt, note = _uiState.value.activeSessionNoteInput.trim())
        val completedSessions = (_uiState.value.completedSessions + session).sortedBy { it.clockIn }

        saveCompletedSessions(completedSessions)
        preferences.edit()
            .remove(profileKey(KEY_ACTIVE_CLOCK_IN))
            .remove(profileKey(KEY_CLOCK_OUT_REMINDER_SENT_MASK))
            .remove(KEY_ACTIVE_CLOCK_IN)
            .apply()
        cancelLongSessionReminders(
            context = getApplication<Application>(),
            profileId = _uiState.value.activeProfileId,
            clockInMillis = startedAt.toEpochMilli(),
        )
        refreshHomeScreenWidgets()

        _uiState.value = withDailySummary(
            _uiState.value.copy(
                isClockedIn = false,
                clockInTime = null,
                activeDuration = Duration.ZERO,
                activeClockInEditInput = "",
                activeClockInEditError = null,
                activeSessionNoteInput = "",
                recentClockOutEditInput = formatTimeInput(endedAt),
                recentClockOutEditError = null,
                todayLastClockOut = endedAt,
                lastCompletedSession = session,
                completedSessions = completedSessions,
            ),
        )
    }

    fun updateActiveClockInEditInput(input: String) {
        _uiState.value = _uiState.value.copy(
            activeClockInEditInput = sanitizeTimeInput(input),
            activeClockInEditError = null,
        )
    }

    fun saveActiveClockInTime(): Boolean {
        val state = _uiState.value
        val currentClockIn = state.clockInTime ?: return false
        val clockInTime = parseTimeInput(state.activeClockInEditInput)
        if (clockInTime == null) {
            _uiState.value = state.copy(activeClockInEditError = "Use a time like 09:00.")
            return false
        }

        val zoneId = ZoneId.systemDefault()
        val newClockIn = LocalDate.now(zoneId).atTime(clockInTime).atZone(zoneId).toInstant()
        val now = Instant.now()
        if (!newClockIn.isBefore(now)) {
            _uiState.value = state.copy(activeClockInEditError = "Clock in must be before now.")
            return false
        }

        preferences.edit()
            .putLong(profileKey(KEY_ACTIVE_CLOCK_IN), newClockIn.toEpochMilli())
            .remove(profileKey(KEY_CLOCK_OUT_REMINDER_SENT_MASK))
            .apply()
        cancelLongSessionReminders(
            context = getApplication<Application>(),
            profileId = state.activeProfileId,
            clockInMillis = currentClockIn.toEpochMilli(),
        )

        val updatedState = withDailySummary(
            state.copy(
                clockInTime = newClockIn,
                activeDuration = Duration.between(newClockIn, now).coerceAtLeast(Duration.ZERO),
                activeClockInEditInput = formatTimeInput(newClockIn),
                activeClockInEditError = null,
                clockOutReminderSentMask = 0,
            ),
        )
        _uiState.value = updatedState
        rescheduleLongSessionReminders(updatedState)
        refreshHomeScreenWidgets()
        return true
    }

    fun updateRecentClockOutEditInput(input: String) {
        _uiState.value = _uiState.value.copy(
            recentClockOutEditInput = sanitizeTimeInput(input),
            recentClockOutEditError = null,
        )
    }

    fun updateActiveSessionNote(input: String) {
        _uiState.value = _uiState.value.copy(activeSessionNoteInput = input.take(160))
    }

    fun saveRecentClockOutTime(): Boolean {
        val state = _uiState.value
        val session = state.lastCompletedSession ?: return false
        val clockOutTime = parseTimeInput(state.recentClockOutEditInput)
        if (clockOutTime == null) {
            _uiState.value = state.copy(recentClockOutEditError = "Use a time like 17:00.")
            return false
        }

        val zoneId = ZoneId.systemDefault()
        val clockOutDate = session.clockOut.atZone(zoneId).toLocalDate()
        val newClockOut = clockOutDate.atTime(clockOutTime).atZone(zoneId).toInstant()
        if (!newClockOut.isAfter(session.clockIn)) {
            _uiState.value = state.copy(recentClockOutEditError = "Clock out must be after clock in.")
            return false
        }
        val updatedSession = session.copy(clockOut = newClockOut)
        val completedSessions = state.completedSessions.map { existingSession ->
            if (existingSession.clockIn == session.clockIn && existingSession.clockOut == session.clockOut) {
                updatedSession
            } else {
                existingSession
            }
        }.sortedBy { it.clockIn }

        saveCompletedSessions(completedSessions)
        _uiState.value = withDailySummary(
            state.copy(
                completedSessions = completedSessions,
                lastCompletedSession = updatedSession,
                recentClockOutEditInput = formatTimeInput(newClockOut),
                recentClockOutEditError = null,
            ),
        )
        refreshHomeScreenWidgets()
        return true
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

    fun updateManualNote(input: String) {
        _uiState.value = _uiState.value.copy(
            manualNoteInput = input.take(160),
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
            manualNoteInput = session.note,
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
            manualNoteInput = "",
            manualEntryError = null,
            editingSessionClockInMillis = null,
            editingSessionClockOutMillis = null,
        )
    }

    fun updateAbsenceDate(input: String) {
        _uiState.value = _uiState.value.copy(
            absenceDateInput = input.take(10),
            absenceEntryError = null,
        )
    }

    fun updateAbsenceEndDate(input: String) {
        _uiState.value = _uiState.value.copy(
            absenceEndDateInput = input.take(10),
            absenceEntryError = null,
        )
    }

    fun selectAbsenceType(type: AbsenceType) {
        _uiState.value = _uiState.value.copy(
            selectedAbsenceType = type,
            absenceEntryError = null,
        )
    }

    fun updateAbsenceHours(input: String) {
        _uiState.value = _uiState.value.copy(
            absenceHoursInput = sanitizeDurationInput(input),
            absenceEntryError = null,
        )
    }

    fun updateAbsenceNote(input: String) {
        _uiState.value = _uiState.value.copy(
            absenceNoteInput = input.take(80),
            absenceEntryError = null,
        )
    }

    fun saveAbsence(): Boolean {
        val state = _uiState.value
        val startDate = runCatching { LocalDate.parse(state.absenceDateInput) }.getOrNull()
        if (startDate == null) {
            _uiState.value = state.copy(absenceEntryError = "Use date format YYYY-MM-DD.")
            return false
        }
        val endDate = runCatching { LocalDate.parse(state.absenceEndDateInput) }.getOrNull()
        if (endDate == null) {
            _uiState.value = state.copy(absenceEntryError = "Use end date format YYYY-MM-DD.")
            return false
        }
        if (endDate.isBefore(startDate)) {
            _uiState.value = state.copy(absenceEntryError = "End date must be after start date.")
            return false
        }

        val dates = buildDateRange(startDate, endDate)
        val absenceDates = dates.toSet()
        val newAbsences = dates.map { date ->
            AbsenceEntry(
                date = date,
                type = state.selectedAbsenceType,
                duration = Duration.ZERO,
                note = state.absenceNoteInput.trim(),
            )
        }
        val absences = (state.absences.filterNot { it.date in absenceDates } + newAbsences)
            .sortedBy { it.date }

        saveAbsences(absences)
        _uiState.value = withDailySummary(
            state.copy(
                absences = absences,
                absenceDateInput = formatDateInput(LocalDate.now()),
                absenceEndDateInput = formatDateInput(LocalDate.now()),
                absenceHoursInput = "1:00",
                absenceNoteInput = "",
                absenceEntryError = null,
                expandedHistoryDates = state.expandedHistoryDates + dates,
            ),
        )
        rescheduleLongSessionReminders(_uiState.value)
        refreshHomeScreenWidgets()
        Toast.makeText(getApplication<Application>(), "Absence added", Toast.LENGTH_SHORT).show()
        return true
    }

    fun deleteAbsence(absence: AbsenceEntry) {
        val absences = _uiState.value.absences.filterNot {
            it.date == absence.date && it.type == absence.type && it.note == absence.note
        }

        saveAbsences(absences)
        _uiState.value = withDailySummary(
            _uiState.value.copy(absences = absences),
        )
        rescheduleLongSessionReminders(_uiState.value)
        refreshHomeScreenWidgets()
    }

    fun saveManualSession(): Boolean {
        val parsedSession = parseManualSession()
        if (parsedSession == null) {
            _uiState.value = _uiState.value.copy(
                manualEntryError = "Use date YYYY-MM-DD and times like 09:00 and 17:00.",
            )
            return false
        }

        if (parsedSession.clockOut <= parsedSession.clockIn) {
            _uiState.value = _uiState.value.copy(
                manualEntryError = "Clock out must be after clock in.",
            )
            return false
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
                manualNoteInput = "",
                manualEntryError = null,
                editingSessionClockInMillis = null,
                editingSessionClockOutMillis = null,
            ),
        )
        Toast.makeText(getApplication<Application>(), "Session saved", Toast.LENGTH_SHORT).show()
        return true
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
        rescheduleLongSessionReminders(_uiState.value)
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
        rescheduleLongSessionReminders(_uiState.value)
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
        rescheduleClockInReminder(_uiState.value)
        rescheduleLongSessionReminders(_uiState.value)
        refreshHomeScreenWidgets()
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
        rescheduleLongSessionReminders(_uiState.value)
        refreshHomeScreenWidgets()
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
        rescheduleLongSessionReminders(_uiState.value)
        refreshHomeScreenWidgets()
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
        refreshHomeScreenWidgets()
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
        refreshHomeScreenWidgets()
    }

    fun updateOvertimeRange(range: OvertimeRange) {
        preferences.edit()
            .putString(profileKey(KEY_OVERTIME_RANGE), range.name)
            .apply()

        _uiState.value = _uiState.value.copy(selectedOvertimeRange = range)
        refreshHomeScreenWidgets()
    }

    fun toggleExportOption(option: ExportOption) {
        val currentOptions = _uiState.value.exportOptions
        val updatedOptions = when (option) {
            ExportOption.WORKPLACE_SETTINGS -> currentOptions.copy(
                includeWorkplaceSettings = !currentOptions.includeWorkplaceSettings,
            )
            ExportOption.REPORT_SUMMARIES -> currentOptions.copy(
                includeReportSummaries = !currentOptions.includeReportSummaries,
            )
            ExportOption.OVERTIME_BALANCE -> currentOptions.copy(
                includeOvertimeBalance = !currentOptions.includeOvertimeBalance,
            )
            ExportOption.EARNINGS -> currentOptions.copy(
                includeEarnings = !currentOptions.includeEarnings,
            )
            ExportOption.SESSIONS -> currentOptions.copy(
                includeSessions = !currentOptions.includeSessions,
            )
            ExportOption.ABSENCES -> currentOptions.copy(
                includeAbsences = !currentOptions.includeAbsences,
            )
            ExportOption.NOTES -> currentOptions.copy(
                includeNotes = !currentOptions.includeNotes,
            )
        }

        _uiState.value = _uiState.value.copy(exportOptions = updatedOptions)
    }

    fun selectExportRangeMode(mode: ExportRangeMode) {
        _uiState.value = _uiState.value.copy(
            exportRangeMode = mode,
            exportPeriodError = exportPeriodError(
                mode = mode,
                startInput = _uiState.value.exportStartDateInput,
                endInput = _uiState.value.exportEndDateInput,
            ),
        )
    }

    fun updateExportStartDate(input: String) {
        val sanitizedInput = input.take(10)
        _uiState.value = _uiState.value.copy(
            exportStartDateInput = sanitizedInput,
            exportPeriodError = exportPeriodError(
                mode = _uiState.value.exportRangeMode,
                startInput = sanitizedInput,
                endInput = _uiState.value.exportEndDateInput,
            ),
        )
    }

    fun updateExportEndDate(input: String) {
        val sanitizedInput = input.take(10)
        _uiState.value = _uiState.value.copy(
            exportEndDateInput = sanitizedInput,
            exportPeriodError = exportPeriodError(
                mode = _uiState.value.exportRangeMode,
                startInput = _uiState.value.exportStartDateInput,
                endInput = sanitizedInput,
            ),
        )
    }

    fun toggleClockInReminder(enabled: Boolean) {
        preferences.edit()
            .putBoolean(profileKey(KEY_CLOCK_IN_REMINDER_ENABLED), enabled)
            .apply()

        _uiState.value = _uiState.value.copy(
            clockInReminderEnabled = enabled,
            reminderSettingsError = null,
        )
        rescheduleClockInReminder(_uiState.value)
    }

    fun updateClockInReminderTime(input: String) {
        val sanitizedInput = sanitizeTimeInput(input)
        val parsedTime = parseTimeInput(sanitizedInput)
        val error = if (sanitizedInput.length >= 4 && parsedTime == null) {
            "Use time like 08:00."
        } else {
            null
        }

        preferences.edit()
            .putString(profileKey(KEY_CLOCK_IN_REMINDER_TIME), sanitizedInput)
            .apply()

        _uiState.value = _uiState.value.copy(
            clockInReminderTimeInput = sanitizedInput,
            reminderSettingsError = error,
        )
        if (parsedTime != null) {
            rescheduleClockInReminder(_uiState.value)
        }
    }

    fun toggleClockOutReminder(enabled: Boolean) {
        preferences.edit()
            .putBoolean(profileKey(KEY_CLOCK_OUT_REMINDER_ENABLED), enabled)
            .apply()

        _uiState.value = _uiState.value.copy(
            clockOutReminderEnabled = enabled,
            reminderSettingsError = null,
        )
        if (enabled) {
            rescheduleLongSessionReminders(_uiState.value)
        } else {
            _uiState.value.clockInTime?.let { clockInTime ->
                cancelLongSessionReminders(
                    context = getApplication<Application>(),
                    profileId = _uiState.value.activeProfileId,
                    clockInMillis = clockInTime.toEpochMilli(),
                )
            }
        }
    }

    private fun refreshActiveDuration() {
        val startedAt = _uiState.value.clockInTime
        val updatedState = withDailySummary(
            _uiState.value.copy(
                activeDuration = startedAt?.let {
                    Duration.between(it, Instant.now()).coerceAtLeast(Duration.ZERO)
                } ?: Duration.ZERO,
            ),
        )
        _uiState.value = updatedState
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

        val initialState = buildStateForProfile(
            profiles = profiles,
            activeProfile = activeProfile,
            newProfileNameInput = "",
            newProfileStartDateInput = formatDateInput(LocalDate.now()),
        )
        rescheduleClockInReminder(initialState)
        return initialState
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
        val absences = loadAbsences(activeProfile.id)
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
        val clockOutReminderEnabled = getProfileBoolean(
            profileId = activeProfile.id,
            key = KEY_CLOCK_OUT_REMINDER_ENABLED,
            defaultValue = false,
        )
        val clockInReminderEnabled = getProfileBoolean(
            profileId = activeProfile.id,
            key = KEY_CLOCK_IN_REMINDER_ENABLED,
            defaultValue = false,
        )
        val clockInReminderTimeInput = getProfileString(
            profileId = activeProfile.id,
            key = KEY_CLOCK_IN_REMINDER_TIME,
            defaultValue = DEFAULT_CLOCK_IN_REMINDER_INPUT,
        ) ?: DEFAULT_CLOCK_IN_REMINDER_INPUT
        val clockOutReminderSentMask = preferences.getInt(
            profileKey(activeProfile.id, KEY_CLOCK_OUT_REMINDER_SENT_MASK),
            0,
        )
        val workplaceType = getProfileString(
            profileId = activeProfile.id,
            key = KEY_WORKPLACE_TYPE,
            defaultValue = WorkplaceType.FIXED_HOURS_FIXED_PAY.name,
        )
            ?.let { value -> runCatching { WorkplaceType.valueOf(value) }.getOrNull() }
            ?: WorkplaceType.FIXED_HOURS_FIXED_PAY
        val monthlySalaryInput = getProfileString(
            profileId = activeProfile.id,
            key = KEY_MONTHLY_SALARY,
            defaultValue = "",
        ).orEmpty()
        val hourlyRateInput = getProfileString(
            profileId = activeProfile.id,
            key = KEY_HOURLY_RATE,
            defaultValue = "",
        ).orEmpty()
        val currencyInput = getProfileString(
            profileId = activeProfile.id,
            key = KEY_CURRENCY,
            defaultValue = DEFAULT_CURRENCY_INPUT,
        ) ?: DEFAULT_CURRENCY_INPUT
        val lastCompletedSession = completedSessions.maxByOrNull { it.clockOut }

        return withDailySummary(
            TimeClockUiState(
                workProfiles = profiles,
                activeProfileId = activeProfile.id,
                activeProfileNameInput = activeProfile.name,
                activeProfileStartDateInput = formatDateInput(activeProfile.trackingStartDate),
                workplaceType = workplaceType,
                monthlySalaryInput = monthlySalaryInput,
                hourlyRateInput = hourlyRateInput,
                currencyInput = currencyInput,
                newProfileNameInput = newProfileNameInput,
                newProfileStartDateInput = newProfileStartDateInput,
                isClockedIn = activeClockIn != null,
                clockInTime = activeClockIn,
                activeDuration = activeClockIn?.let {
                    Duration.between(it, Instant.now()).coerceAtLeast(Duration.ZERO)
                } ?: Duration.ZERO,
                activeClockInEditInput = activeClockIn?.let(::formatTimeInput).orEmpty(),
                recentClockOutEditInput = lastCompletedSession?.clockOut?.let(::formatTimeInput).orEmpty(),
                expectedDailyHoursInput = formatDurationInput(expectedWeeklyDuration.dividedByWorkdays(workDays.size)),
                expectedDailyDuration = expectedWeeklyDuration.dividedByWorkdays(workDays.size),
                expectedWeeklyHoursInput = expectedWeeklyHours,
                expectedWeeklyDuration = expectedWeeklyDuration,
                workDays = workDays,
                deductUnpaidLunchBreak = deductUnpaidLunchBreak,
                lunchBreakMinutesInput = lunchBreakMinutesInput,
                lunchBreakDuration = lunchBreakDuration,
                lastCompletedSession = lastCompletedSession,
                completedSessions = completedSessions,
                absences = absences,
                overtimeStartDateInput = overtimeStartDateInput,
                startingOvertimeBalanceInput = startingOvertimeBalanceInput,
                selectedOvertimeRange = selectedOvertimeRange,
                clockInReminderEnabled = clockInReminderEnabled,
                clockInReminderTimeInput = clockInReminderTimeInput,
                clockOutReminderEnabled = clockOutReminderEnabled,
                clockOutReminderSentMask = clockOutReminderSentMask,
            ),
        )
    }

    private fun withDailySummary(state: TimeClockUiState): TimeClockUiState {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val todayCompletedSessions = state.completedSessions.filter { it.overlapsDate(today, zoneId) }
        val activeSession = state.clockInTime?.let { WorkSession(it, Instant.now(), note = state.activeSessionNoteInput.trim()) }
        val todayAbsence = state.absences.firstOrNull { it.date == today }
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
        val isAfterWorkplaceEnd = state.activeProfile.trackingEndDate?.let { today.isAfter(it) } == true
        val isTodayWorkday = !isAfterWorkplaceEnd &&
            LocalDate.now(zoneId).dayOfWeek in state.workDays &&
            todayAbsence?.type?.coversExpectedHours != true
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
                absence = todayAbsence,
                worked = todayCreditedDuration,
                expected = expectedTodayDuration,
            ),
        )
    }

    private fun maybeShowClockOutReminder(state: TimeClockUiState): TimeClockUiState {
        if (state.clockInTime == null) return state
        if (!state.clockOutReminderEnabled) return state
        val expectedDuration = expectedDurationForLongSessionAlert(state)
        if (expectedDuration <= Duration.ZERO) return state
        val eligibleAlert = LONG_SESSION_ALERTS
            .mapIndexedNotNull { index, overtimeDuration ->
                val bit = 1 shl index
                val threshold = expectedDuration.plus(overtimeDuration)
                if (state.activeDuration >= threshold && state.clockOutReminderSentMask and bit == 0) {
                    LongSessionAlert(index = index, overtimeDuration = overtimeDuration)
                } else {
                    null
                }
            }
            .lastOrNull()
            ?: return state

        if (!canPostNotifications()) return state

        showClockOutReminderNotification(
            state = state,
            expectedDuration = expectedDuration,
            overtimeDuration = eligibleAlert.overtimeDuration,
        )
        val updatedMask = (0..eligibleAlert.index).fold(state.clockOutReminderSentMask) { mask, index ->
            mask or (1 shl index)
        }
        preferences.edit()
            .putInt(profileKey(KEY_CLOCK_OUT_REMINDER_SENT_MASK), updatedMask)
            .apply()

        return state.copy(clockOutReminderSentMask = updatedMask)
    }

    private fun expectedDurationForLongSessionAlert(state: TimeClockUiState): Duration {
        if (!state.isTodayWorkday) return Duration.ZERO
        return state.expectedDailyDuration.plus(
            if (state.deductUnpaidLunchBreak) state.lunchBreakDuration else Duration.ZERO,
        )
    }

    private fun rescheduleClockInReminder(state: TimeClockUiState) {
        val application = getApplication<Application>()
        if (!state.clockInReminderEnabled) {
            cancelClockInReminder(application)
            return
        }

        val reminderTime = parseTimeInput(state.clockInReminderTimeInput)
        if (reminderTime == null) {
            cancelClockInReminder(application)
            return
        }

        scheduleClockInReminder(
            context = application,
            profileId = state.activeProfileId,
            reminderTime = reminderTime,
            workDays = state.workDays,
        )
    }

    private fun rescheduleLongSessionReminders(state: TimeClockUiState) {
        val clockInTime = state.clockInTime ?: return
        if (!state.clockOutReminderEnabled) return

        val expectedDuration = expectedDurationForLongSessionAlert(state)
        if (expectedDuration <= Duration.ZERO) return

        scheduleLongSessionReminders(
            context = getApplication<Application>(),
            profileId = state.activeProfileId,
            clockInMillis = clockInTime.toEpochMilli(),
            expectedDuration = expectedDuration,
        )
    }

    private fun canPostNotifications(): Boolean {
        val application = getApplication<Application>()
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            application.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun showClockOutReminderNotification(
        state: TimeClockUiState,
        expectedDuration: Duration,
        overtimeDuration: Duration,
    ) {
        val application = getApplication<Application>()
        val intent = Intent(application, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            application,
            CLOCK_OUT_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(application, REMINDER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(
                if (overtimeDuration == Duration.ZERO) {
                    "Time to clock out"
                } else {
                    "${formatDurationShort(overtimeDuration)} overtime"
                },
            )
            .setContentText("${state.activeProfile.name}: ${formatDurationShort(state.activeDuration)} active, target ${formatDurationShort(expectedDuration)}")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        application.getSystemService(NotificationManager::class.java)
            .notify(CLOCK_OUT_REMINDER_NOTIFICATION_ID, notification)
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
            "${it.clockIn.toEpochMilli()}|${it.clockOut.toEpochMilli()}|${sanitizeSessionNote(it.note)}"
        }
    }

    private fun saveCompletedSessions(sessions: List<WorkSession>) {
        preferences.edit()
            .putString(profileKey(KEY_COMPLETED_SESSIONS), encodeSessions(sessions))
            .remove(KEY_LAST_CLOCK_IN)
            .remove(KEY_LAST_CLOCK_OUT)
            .apply()
        refreshHomeScreenWidgets()
    }

    private fun loadAbsences(profileId: String = _uiState.value.activeProfileId): List<AbsenceEntry> {
        return preferences.getString(profileKey(profileId, KEY_ABSENCES), null)
            ?.let(::decodeAbsences)
            .orEmpty()
    }

    private fun saveAbsences(absences: List<AbsenceEntry>) {
        preferences.edit()
            .putString(profileKey(KEY_ABSENCES), encodeAbsences(absences))
            .apply()
        refreshHomeScreenWidgets()
    }

    private fun encodeAbsences(absences: List<AbsenceEntry>): String {
        return absences.joinToString(separator = "\n") { absence ->
            "${formatDateInput(absence.date)}|${absence.type.name}|${absence.duration.toMinutes()}|${absence.note.replace("|", " ").replace("\n", " ")}"
        }
    }

    private fun decodeAbsences(encoded: String): List<AbsenceEntry> {
        return encoded.lineSequence()
            .mapNotNull { line ->
                val parts = line.split("|")
                val date = parts.getOrNull(0)
                    ?.let { value -> runCatching { LocalDate.parse(value) }.getOrNull() }
                    ?: return@mapNotNull null
                val type = parts.getOrNull(1)
                    ?.let { value ->
                        if (value == "PUBLIC_HOLIDAY") {
                            AbsenceType.VACATION
                        } else if (value == "TIME_OFF") {
                            AbsenceType.NO_WORK
                        } else {
                            runCatching { AbsenceType.valueOf(value) }.getOrNull()
                        }
                    }
                    ?: AbsenceType.VACATION
                val durationMinutes = parts.getOrNull(2)?.toLongOrNull() ?: 0L
                val note = if (parts.getOrNull(2)?.toLongOrNull() == null) {
                    parts.getOrNull(2).orEmpty()
                } else {
                    parts.getOrNull(3).orEmpty()
                }

                AbsenceEntry(
                    date = date,
                    type = type,
                    duration = Duration.ofMinutes(durationMinutes),
                    note = note,
                )
            }
            .sortedBy { it.date }
            .toList()
    }

    private fun decodeSessions(encoded: String): List<WorkSession> {
        return encoded.lineSequence()
            .mapNotNull { line ->
                val parts = if ("|" in line) line.split("|", limit = 3) else line.split(",")
                val clockIn = parts.getOrNull(0)?.toLongOrNull()
                val clockOut = parts.getOrNull(1)?.toLongOrNull()

                if (clockIn != null && clockOut != null && clockOut >= clockIn) {
                    WorkSession(
                        clockIn = Instant.ofEpochMilli(clockIn),
                        clockOut = Instant.ofEpochMilli(clockOut),
                        note = parts.getOrNull(2).orEmpty(),
                    )
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

    private fun sanitizeSessionNote(input: String): String {
        return input.replace("|", " ").replace("\n", " ").take(160)
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
            note = _uiState.value.manualNoteInput.trim(),
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
        refreshHomeScreenWidgets()
    }

    private fun refreshHomeScreenWidgets() {
        updateTimeClockWidgets(getApplication<Application>())
    }

    private fun encodeProfiles(profiles: List<WorkProfile>): String {
        return profiles.joinToString(separator = "\n") { profile ->
            listOf(
                profile.id,
                sanitizeProfileName(profile.name).ifBlank { DEFAULT_WORK_PROFILE_NAME },
                formatDateInput(profile.trackingStartDate),
                profile.trackingEndDate?.let(::formatDateInput).orEmpty(),
            ).joinToString(separator = "|")
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
                val trackingEndDate = parts.getOrNull(3)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { value -> runCatching { LocalDate.parse(value) }.getOrNull() }

                WorkProfile(
                    id = id,
                    name = name,
                    trackingStartDate = trackingStartDate,
                    trackingEndDate = trackingEndDate,
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

    private fun sanitizeMoneyInput(input: String): String {
        return input
            .replace(",", ".")
            .filter { it.isDigit() || it == '.' }
            .take(12)
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

    private fun android.content.SharedPreferences.Editor.removeProfileData(profileId: String): android.content.SharedPreferences.Editor {
        return remove(profileKey(profileId, KEY_ACTIVE_CLOCK_IN))
            .remove(profileKey(profileId, KEY_COMPLETED_SESSIONS))
            .remove(profileKey(profileId, KEY_ABSENCES))
            .remove(profileKey(profileId, KEY_EXPECTED_DAILY_HOURS))
            .remove(profileKey(profileId, KEY_EXPECTED_WEEKLY_HOURS))
            .remove(profileKey(profileId, KEY_WORK_DAYS))
            .remove(profileKey(profileId, KEY_DEDUCT_UNPAID_LUNCH_BREAK))
            .remove(profileKey(profileId, KEY_LUNCH_BREAK_MINUTES))
            .remove(profileKey(profileId, KEY_OVERTIME_START_DATE))
            .remove(profileKey(profileId, KEY_STARTING_OVERTIME_BALANCE))
            .remove(profileKey(profileId, KEY_OVERTIME_RANGE))
            .remove(profileKey(profileId, KEY_WORKPLACE_TYPE))
            .remove(profileKey(profileId, KEY_MONTHLY_SALARY))
            .remove(profileKey(profileId, KEY_HOURLY_RATE))
            .remove(profileKey(profileId, KEY_CURRENCY))
            .remove(profileKey(profileId, KEY_CLOCK_IN_REMINDER_ENABLED))
            .remove(profileKey(profileId, KEY_CLOCK_IN_REMINDER_TIME))
            .remove(profileKey(profileId, KEY_CLOCK_OUT_REMINDER_ENABLED))
            .remove(profileKey(profileId, KEY_CLOCK_OUT_REMINDER_SENT_MASK))
    }

    private companion object {
        const val PREFS_NAME = "time_clock_preferences"
        const val KEY_WORK_PROFILES = "work_profiles"
        const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
        const val KEY_ACTIVE_CLOCK_IN = "active_clock_in"
        const val KEY_COMPLETED_SESSIONS = "completed_sessions"
        const val KEY_ABSENCES = "absences"
        const val KEY_EXPECTED_DAILY_HOURS = "expected_daily_hours"
        const val KEY_EXPECTED_WEEKLY_HOURS = "expected_weekly_hours"
        const val KEY_WORK_DAYS = "work_days"
        const val KEY_DEDUCT_UNPAID_LUNCH_BREAK = "deduct_unpaid_lunch_break"
        const val KEY_LUNCH_BREAK_MINUTES = "lunch_break_minutes"
        const val KEY_OVERTIME_START_DATE = "overtime_start_date"
        const val KEY_STARTING_OVERTIME_BALANCE = "starting_overtime_balance"
        const val KEY_OVERTIME_RANGE = "overtime_range"
        const val KEY_WORKPLACE_TYPE = "workplace_type"
        const val KEY_MONTHLY_SALARY = "monthly_salary"
        const val KEY_HOURLY_RATE = "hourly_rate"
        const val KEY_CURRENCY = "currency"
        const val KEY_CLOCK_IN_REMINDER_ENABLED = "clock_in_reminder_enabled"
        const val KEY_CLOCK_IN_REMINDER_TIME = "clock_in_reminder_time"
        const val KEY_CLOCK_OUT_REMINDER_ENABLED = "clock_out_reminder_enabled"
        const val KEY_CLOCK_OUT_REMINDER_SENT_MASK = "clock_out_reminder_sent_mask"
        const val KEY_LAST_CLOCK_IN = "last_clock_in"
        const val KEY_LAST_CLOCK_OUT = "last_clock_out"
    }
}

@Composable
fun TimeClockScreen(
    state: TimeClockUiState,
    onTabSelect: (AppTab) -> Unit,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit,
    onActiveClockInChange: (String) -> Unit,
    onActiveClockInSave: () -> Boolean,
    onRecentClockOutChange: (String) -> Unit,
    onRecentClockOutSave: () -> Boolean,
    onTodayOvertimeRangeChange: (TodayOvertimeRange) -> Unit,
    onTodayOvertimeStartDateChange: (String) -> Unit,
    onTodayOvertimeEndDateChange: (String) -> Unit,
    onInsightsSectionSelect: (InsightsSection) -> Unit,
    onExportExpandToggle: () -> Unit,
    onProfileSelect: (String) -> Unit,
    onProfileNameChange: (String) -> Unit,
    onProfileStartDateChange: (String) -> Unit,
    onWorkplaceTypeSelect: (WorkplaceType) -> Unit,
    onMonthlySalaryChange: (String) -> Unit,
    onHourlyRateChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onNewProfileNameChange: (String) -> Unit,
    onNewProfileStartDateChange: (String) -> Unit,
    onProfileCreate: () -> Unit,
    onProfileDelete: () -> Unit,
    onProfileStopTracking: (LocalDate) -> Unit,
    onProfileReactivate: () -> Unit,
    onHistoryDayToggle: (LocalDate) -> Unit,
    onManualDateChange: (String) -> Unit,
    onManualClockInChange: (String) -> Unit,
    onManualClockOutChange: (String) -> Unit,
    onManualNoteChange: (String) -> Unit,
    onManualSessionSave: () -> Boolean,
    onManualSessionCancel: () -> Unit,
    onAbsenceDateChange: (String) -> Unit,
    onAbsenceEndDateChange: (String) -> Unit,
    onAbsenceTypeSelect: (AbsenceType) -> Unit,
    onAbsenceHoursChange: (String) -> Unit,
    onAbsenceNoteChange: (String) -> Unit,
    onAbsenceSave: () -> Boolean,
    onAbsenceDelete: (AbsenceEntry) -> Unit,
    onSessionEdit: (WorkSession) -> Unit,
    onSessionDelete: (WorkSession) -> Unit,
    onExpectedDailyHoursChange: (String) -> Unit,
    onExpectedWeeklyHoursChange: (String) -> Unit,
    onWorkdayToggle: (DayOfWeek) -> Unit,
    onUnpaidLunchBreakToggle: (Boolean) -> Unit,
    onLunchBreakMinutesChange: (String) -> Unit,
    onOvertimeStartDateChange: (String) -> Unit,
    onStartingOvertimeBalanceChange: (String) -> Unit,
    onOvertimeRangeChange: (OvertimeRange) -> Unit,
    onShareCsvText: () -> Unit,
    onShareCsvFile: () -> Unit,
    onSaveCsv: () -> Unit,
    onSharePdf: () -> Unit,
    onSavePdf: () -> Unit,
    onExportOptionToggle: (ExportOption) -> Unit,
    onExportRangeModeSelect: (ExportRangeMode) -> Unit,
    onExportStartDateChange: (String) -> Unit,
    onExportEndDateChange: (String) -> Unit,
    onClockInReminderToggle: (Boolean) -> Unit,
    onClockInReminderTimeChange: (String) -> Unit,
    onClockOutReminderToggle: (Boolean) -> Unit,
    onActiveSessionNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isAddMenuExpanded by remember { mutableStateOf(false) }
    var isManualEntryDialogOpen by remember { mutableStateOf(false) }
    var isAbsenceDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF3F4F6),
        bottomBar = {
            TimeClockBottomNavigation(
                selectedTab = state.selectedTab,
                onTabSelect = onTabSelect,
                expanded = isAddMenuExpanded,
                onExpandedChange = { isAddMenuExpanded = it },
                onManualEntryClick = {
                    isAddMenuExpanded = false
                    isManualEntryDialogOpen = true
                },
                onAbsenceClick = {
                    isAddMenuExpanded = false
                    isAbsenceDialogOpen = true
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
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
            }

            ActiveWorkplaceDropdown(
                profiles = state.workProfiles,
                activeProfileId = state.activeProfileId,
                onProfileSelect = onProfileSelect,
            )

            when (state.selectedTab) {
                AppTab.TODAY -> TodayTab(
                    state = state,
                    onClockIn = onClockIn,
                    onClockOut = onClockOut,
                    onActiveClockInChange = onActiveClockInChange,
                    onActiveClockInSave = onActiveClockInSave,
                    onRecentClockOutChange = onRecentClockOutChange,
                    onRecentClockOutSave = onRecentClockOutSave,
                    onTodayOvertimeRangeChange = onTodayOvertimeRangeChange,
                    onTodayOvertimeStartDateChange = onTodayOvertimeStartDateChange,
                    onTodayOvertimeEndDateChange = onTodayOvertimeEndDateChange,
                    onActiveSessionNoteChange = onActiveSessionNoteChange,
                )
                AppTab.HISTORY -> HistoryTab(
                    state = state,
                    onAbsenceDelete = onAbsenceDelete,
                    onHistoryDayToggle = onHistoryDayToggle,
                    onSessionEdit = onSessionEdit,
                    onSessionDelete = onSessionDelete,
                )
                AppTab.INSIGHTS -> InsightsTab(
                    state = state,
                    onInsightsSectionSelect = onInsightsSectionSelect,
                    onOvertimeRangeChange = onOvertimeRangeChange,
                    onShareCsvText = onShareCsvText,
                    onShareCsvFile = onShareCsvFile,
                    onSaveCsv = onSaveCsv,
                    onSharePdf = onSharePdf,
                    onSavePdf = onSavePdf,
                    onExportOptionToggle = onExportOptionToggle,
                    onExportRangeModeSelect = onExportRangeModeSelect,
                    onExportStartDateChange = onExportStartDateChange,
                    onExportEndDateChange = onExportEndDateChange,
                    onExportExpandToggle = onExportExpandToggle,
                )
                AppTab.SETTINGS -> SettingsTab(
                    state = state,
                    onProfileNameChange = onProfileNameChange,
                    onProfileStartDateChange = onProfileStartDateChange,
                    onWorkplaceTypeSelect = onWorkplaceTypeSelect,
                    onMonthlySalaryChange = onMonthlySalaryChange,
                    onHourlyRateChange = onHourlyRateChange,
                    onCurrencyChange = onCurrencyChange,
                    onNewProfileNameChange = onNewProfileNameChange,
                    onNewProfileStartDateChange = onNewProfileStartDateChange,
                    onProfileCreate = onProfileCreate,
                    onProfileDelete = onProfileDelete,
                    onProfileStopTracking = onProfileStopTracking,
                    onProfileReactivate = onProfileReactivate,
                    onExpectedDailyHoursChange = onExpectedDailyHoursChange,
                    onExpectedWeeklyHoursChange = onExpectedWeeklyHoursChange,
                    onWorkdayToggle = onWorkdayToggle,
                    onUnpaidLunchBreakToggle = onUnpaidLunchBreakToggle,
                    onLunchBreakMinutesChange = onLunchBreakMinutesChange,
                    onOvertimeStartDateChange = onOvertimeStartDateChange,
                    onStartingOvertimeBalanceChange = onStartingOvertimeBalanceChange,
                    onClockInReminderToggle = onClockInReminderToggle,
                    onClockInReminderTimeChange = onClockInReminderTimeChange,
                    onClockOutReminderToggle = onClockOutReminderToggle,
                )
            }
        }
    }

    if (isManualEntryDialogOpen || state.editingSessionClockInMillis != null) {
        ManualEntryDialog(
            state = state,
            onDateChange = onManualDateChange,
            onClockInChange = onManualClockInChange,
            onClockOutChange = onManualClockOutChange,
            onNoteChange = onManualNoteChange,
            onSave = {
                val saved = onManualSessionSave()
                if (saved) {
                    isManualEntryDialogOpen = false
                }
                saved
            },
            onCancel = {
                isManualEntryDialogOpen = false
                onManualSessionCancel()
            },
            onDismiss = {
                isManualEntryDialogOpen = false
                if (state.editingSessionClockInMillis != null) {
                    onManualSessionCancel()
                }
            },
        )
    }

    if (isAbsenceDialogOpen) {
        AbsenceEntryDialog(
            state = state,
            onDateChange = onAbsenceDateChange,
            onEndDateChange = onAbsenceEndDateChange,
            onTypeSelect = onAbsenceTypeSelect,
            onHoursChange = onAbsenceHoursChange,
            onNoteChange = onAbsenceNoteChange,
            onSave = {
                val saved = onAbsenceSave()
                if (saved) {
                    isAbsenceDialogOpen = false
                }
                saved
            },
            onDismiss = { isAbsenceDialogOpen = false },
        )
    }
}

@Composable
private fun TimeClockBottomNavigation(
    selectedTab: AppTab,
    onTabSelect: (AppTab) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onManualEntryClick: () -> Unit,
    onAbsenceClick: () -> Unit,
) {
    NavigationBar(containerColor = Color.White) {
        listOf(AppTab.TODAY, AppTab.HISTORY).forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onTabSelect(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                    )
                },
                label = { Text(text = tab.label) },
            )
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AddEntryMenuButton(
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                onManualEntryClick = onManualEntryClick,
                onAbsenceClick = onAbsenceClick,
            )
        }
        listOf(AppTab.INSIGHTS, AppTab.SETTINGS).forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onTabSelect(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                    )
                },
                label = { Text(text = tab.label) },
            )
        }
    }
}

@Composable
private fun AddEntryMenuButton(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onManualEntryClick: () -> Unit,
    onAbsenceClick: () -> Unit,
) {
    Box {
        FloatingActionButton(
            onClick = { onExpandedChange(true) },
            containerColor = Color(0xFF16A34A),
            contentColor = Color.White,
            modifier = Modifier
                .width(56.dp)
                .height(56.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            DropdownMenuItem(
                text = { Text(text = "Manual entry") },
                onClick = onManualEntryClick,
            )
            DropdownMenuItem(
                text = { Text(text = "Absence") },
                onClick = onAbsenceClick,
            )
        }
    }
}

@Composable
private fun ManualEntryDialog(
    state: TimeClockUiState,
    onDateChange: (String) -> Unit,
    onClockInChange: (String) -> Unit,
    onClockOutChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Boolean,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (state.editingSessionClockInMillis != null) "Edit session" else "Manual entry") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                ManualEntryCard(
                    state = state,
                    onDateChange = onDateChange,
                    onClockInChange = onClockInChange,
                    onClockOutChange = onClockOutChange,
                    onNoteChange = onNoteChange,
                    onSave = onSave,
                    onCancel = onCancel,
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close")
            }
        },
    )
}

@Composable
private fun AbsenceEntryDialog(
    state: TimeClockUiState,
    onDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onTypeSelect: (AbsenceType) -> Unit,
    onHoursChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Boolean,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Absence") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                AbsenceEntryCard(
                    state = state,
                    onDateChange = onDateChange,
                    onEndDateChange = onEndDateChange,
                    onTypeSelect = onTypeSelect,
                    onHoursChange = onHoursChange,
                    onNoteChange = onNoteChange,
                    onSave = onSave,
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close")
            }
        },
    )
}

@Composable
private fun TodayTab(
    state: TimeClockUiState,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit,
    onActiveClockInChange: (String) -> Unit,
    onActiveClockInSave: () -> Boolean,
    onRecentClockOutChange: (String) -> Unit,
    onRecentClockOutSave: () -> Boolean,
    onTodayOvertimeRangeChange: (TodayOvertimeRange) -> Unit,
    onTodayOvertimeStartDateChange: (String) -> Unit,
    onTodayOvertimeEndDateChange: (String) -> Unit,
    onActiveSessionNoteChange: (String) -> Unit,
) {
    var showAbsenceClockInDialog by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val todayAbsence = state.absences.firstOrNull { it.date == today }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ClockActionButton(
            isClockedIn = state.isClockedIn,
            isTrackingEnded = state.activeProfile.trackingEndDate != null,
            onClockIn = {
                if (todayAbsence != null) {
                    showAbsenceClockInDialog = true
                } else {
                    onClockIn()
                }
            },
            onClockOut = onClockOut,
        )
        ActiveTimerBlock(state = state)
        QuickTimeCorrectionCard(
            state = state,
            onActiveClockInChange = onActiveClockInChange,
            onActiveClockInSave = onActiveClockInSave,
            onRecentClockOutChange = onRecentClockOutChange,
            onRecentClockOutSave = onRecentClockOutSave,
        )
        if (state.isClockedIn) {
            SessionNoteCard(
                note = state.activeSessionNoteInput,
                onNoteChange = onActiveSessionNoteChange,
            )
        }
        DailySummaryCard(state = state)
        OvertimePreviewCard(
            state = state,
            onRangeChange = onTodayOvertimeRangeChange,
            onStartDateChange = onTodayOvertimeStartDateChange,
            onEndDateChange = onTodayOvertimeEndDateChange,
        )
    }

    if (showAbsenceClockInDialog && todayAbsence != null) {
        AlertDialog(
            onDismissRequest = { showAbsenceClockInDialog = false },
            title = { Text(text = "Clock in on ${todayAbsence.type.label}?") },
            text = {
                Text(
                    text = "You have ${todayAbsence.type.label.lowercase()} registered today. You can still clock in, and the worked time will count as actual work. The absence will keep today's expected hours at 0, so any time you work today will count as positive overtime.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAbsenceClockInDialog = false
                        onClockIn()
                    },
                ) {
                    Text(text = "Clock in anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbsenceClockInDialog = false }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

@Composable
private fun HistoryTab(
    state: TimeClockUiState,
    onAbsenceDelete: (AbsenceEntry) -> Unit,
    onHistoryDayToggle: (LocalDate) -> Unit,
    onSessionEdit: (WorkSession) -> Unit,
    onSessionDelete: (WorkSession) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HistoryCard(
            state = state,
            onHistoryDayToggle = onHistoryDayToggle,
            onSessionEdit = onSessionEdit,
            onSessionDelete = onSessionDelete,
            onAbsenceDelete = onAbsenceDelete,
        )
    }
}

@Composable
private fun InsightsTab(
    state: TimeClockUiState,
    onInsightsSectionSelect: (InsightsSection) -> Unit,
    onOvertimeRangeChange: (OvertimeRange) -> Unit,
    onShareCsvText: () -> Unit,
    onShareCsvFile: () -> Unit,
    onSaveCsv: () -> Unit,
    onSharePdf: () -> Unit,
    onSavePdf: () -> Unit,
    onExportOptionToggle: (ExportOption) -> Unit,
    onExportRangeModeSelect: (ExportRangeMode) -> Unit,
    onExportStartDateChange: (String) -> Unit,
    onExportEndDateChange: (String) -> Unit,
    onExportExpandToggle: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        InsightsSectionSelector(
            selectedSection = state.selectedInsightsSection,
            onSectionSelect = onInsightsSectionSelect,
        )
        when (state.selectedInsightsSection) {
            InsightsSection.SUMMARY -> {
                ReportsCard(state = state)
                EarningsCard(state = state)
                OvertimeBalanceCard(
                    state = state,
                    onOvertimeRangeChange = onOvertimeRangeChange,
                )
            }
            InsightsSection.CHARTS -> ChartsCard(state = state)
            InsightsSection.EXPORT -> ExportCard(
                state = state,
                onShareCsvText = onShareCsvText,
                onShareCsvFile = onShareCsvFile,
                onSaveCsv = onSaveCsv,
                onSharePdf = onSharePdf,
                onSavePdf = onSavePdf,
                onExportOptionToggle = onExportOptionToggle,
                onExportRangeModeSelect = onExportRangeModeSelect,
                onExportStartDateChange = onExportStartDateChange,
                onExportEndDateChange = onExportEndDateChange,
                onExpandToggle = onExportExpandToggle,
            )
        }
    }
}

@Composable
private fun InsightsSectionSelector(
    selectedSection: InsightsSection,
    onSectionSelect: (InsightsSection) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InsightsSection.entries.forEach { section ->
            FilterChip(
                selected = section == selectedSection,
                onClick = { onSectionSelect(section) },
                label = {
                    Text(
                        text = section.label,
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
private fun SettingsTab(
    state: TimeClockUiState,
    onProfileNameChange: (String) -> Unit,
    onProfileStartDateChange: (String) -> Unit,
    onWorkplaceTypeSelect: (WorkplaceType) -> Unit,
    onMonthlySalaryChange: (String) -> Unit,
    onHourlyRateChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onNewProfileNameChange: (String) -> Unit,
    onNewProfileStartDateChange: (String) -> Unit,
    onProfileCreate: () -> Unit,
    onProfileDelete: () -> Unit,
    onProfileStopTracking: (LocalDate) -> Unit,
    onProfileReactivate: () -> Unit,
    onExpectedDailyHoursChange: (String) -> Unit,
    onExpectedWeeklyHoursChange: (String) -> Unit,
    onWorkdayToggle: (DayOfWeek) -> Unit,
    onUnpaidLunchBreakToggle: (Boolean) -> Unit,
    onLunchBreakMinutesChange: (String) -> Unit,
    onOvertimeStartDateChange: (String) -> Unit,
    onStartingOvertimeBalanceChange: (String) -> Unit,
    onClockInReminderToggle: (Boolean) -> Unit,
    onClockInReminderTimeChange: (String) -> Unit,
    onClockOutReminderToggle: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        WorkProfileCard(
            state = state,
            onProfileNameChange = onProfileNameChange,
            onProfileStartDateChange = onProfileStartDateChange,
            onWorkplaceTypeSelect = onWorkplaceTypeSelect,
            onMonthlySalaryChange = onMonthlySalaryChange,
            onHourlyRateChange = onHourlyRateChange,
            onCurrencyChange = onCurrencyChange,
            onNewProfileNameChange = onNewProfileNameChange,
            onNewProfileStartDateChange = onNewProfileStartDateChange,
            onProfileCreate = onProfileCreate,
            onProfileDelete = onProfileDelete,
            onProfileStopTracking = onProfileStopTracking,
            onProfileReactivate = onProfileReactivate,
        )
        WorkHoursSettingsCard(
            state = state,
            onExpectedDailyHoursChange = onExpectedDailyHoursChange,
            onExpectedWeeklyHoursChange = onExpectedWeeklyHoursChange,
            onWorkdayToggle = onWorkdayToggle,
            onUnpaidLunchBreakToggle = onUnpaidLunchBreakToggle,
            onLunchBreakMinutesChange = onLunchBreakMinutesChange,
            onOvertimeStartDateChange = onOvertimeStartDateChange,
            onStartingOvertimeBalanceChange = onStartingOvertimeBalanceChange,
        )
        ReminderSettingsCard(
            state = state,
            onClockInReminderToggle = onClockInReminderToggle,
            onClockInReminderTimeChange = onClockInReminderTimeChange,
            onClockOutReminderToggle = onClockOutReminderToggle,
        )
    }
}

@Composable
private fun ActiveWorkplaceDropdown(
    profiles: List<WorkProfile>,
    activeProfileId: String,
    onProfileSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val activeProfile = profiles.firstOrNull { it.id == activeProfileId } ?: profiles.first()

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF111827),
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = activeProfile.name,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            profiles.forEach { profile ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = profile.name,
                            fontWeight = if (profile.id == activeProfileId) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                        )
                    },
                    onClick = {
                        expanded = false
                        onProfileSelect(profile.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun DatePickerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        trailingIcon = {
            TextButton(
                onClick = {
                    val initialDate = runCatching { LocalDate.parse(value) }.getOrNull() ?: LocalDate.now()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            onValueChange(formatDateInput(LocalDate.of(year, month + 1, day)))
                        },
                        initialDate.year,
                        initialDate.monthValue - 1,
                        initialDate.dayOfMonth,
                    ).show()
                },
            ) {
                Text(text = "Pick")
            }
        },
    )
}

@Composable
private fun TimePickerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        trailingIcon = {
            TextButton(
                onClick = {
                    val initialTime = runCatching { LocalTime.parse(value, TIME_INPUT_FORMATTER) }.getOrNull()
                        ?: LocalTime.now()
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            onValueChange(TIME_INPUT_FORMATTER.format(LocalTime.of(hour, minute)))
                        },
                        initialTime.hour,
                        initialTime.minute,
                        true,
                    ).show()
                },
            ) {
                Text(text = "Pick")
            }
        },
    )
}

@Composable
private fun ActiveTimerBlock(state: TimeClockUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 22.dp),
        ) {
            Text(
                text = if (state.isClockedIn) formatDuration(state.activeDuration) else "00:00:00",
                fontSize = 62.sp,
                lineHeight = 66.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.clockInTime?.let { "Clocked in at ${formatTime(it)}" }
                    ?: "No active work session",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFD1D5DB),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun QuickTimeCorrectionCard(
    state: TimeClockUiState,
    onActiveClockInChange: (String) -> Unit,
    onActiveClockInSave: () -> Boolean,
    onRecentClockOutChange: (String) -> Unit,
    onRecentClockOutSave: () -> Boolean,
) {
    if (!state.isClockedIn && state.lastCompletedSession == null) return
    var showDialog by remember { mutableStateOf(false) }
    val isCorrectingClockIn = state.isClockedIn
    val buttonLabel = if (isCorrectingClockIn) "Correct latest clock in" else "Correct latest clock out"
    val dialogTitle = if (isCorrectingClockIn) "Correct clock-in time" else "Correct clock-out time"
    val fieldValue = if (isCorrectingClockIn) state.activeClockInEditInput else state.recentClockOutEditInput
    val fieldLabel = if (isCorrectingClockIn) "Clock in" else "Clock out"
    val error = if (isCorrectingClockIn) state.activeClockInEditError else state.recentClockOutEditError
    val onValueChange = if (isCorrectingClockIn) onActiveClockInChange else onRecentClockOutChange
    val onSave = if (isCorrectingClockIn) onActiveClockInSave else onRecentClockOutSave
    val futureClockOutWarning = if (isCorrectingClockIn) null else recentClockOutFutureWarning(state)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        OutlinedButton(
            onClick = { showDialog = true },
            shape = RoundedCornerShape(4.dp),
        ) {
            Text(text = buttonLabel)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = dialogTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isCorrectingClockIn) {
                            "Use this if you forgot to clock in at the real start time."
                        } else {
                            "Use this if the latest clock-out time was wrong."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TimePickerTextField(
                        value = fieldValue,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = fieldLabel,
                    )
                    error?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB42318),
                        )
                    }
                    futureClockOutWarning?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF92400E),
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (onSave()) {
                            showDialog = false
                        }
                    },
                ) {
                    Text(text = "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = "Close")
                }
            },
        )
    }
}

@Composable
private fun SessionNoteCard(
    note: String,
    onNoteChange: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Session note",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Note") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
        }
    }
}

@Composable
private fun OvertimePreviewCard(
    state: TimeClockUiState,
    onRangeChange: (TodayOvertimeRange) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
) {
    val balance = buildTodayOvertimeBalance(state)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Overtime Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = balance.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${formatDateInput(balance.startDate)} to ${formatDateInput(balance.endDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = formatSignedBalance(balance.totalBalance),
                    style = MaterialTheme.typography.titleLarge,
                    color = signedDurationColor(balance.totalBalance),
                    fontWeight = FontWeight.Bold,
                )
            }
            TodayOvertimeDropdown(
                ranges = TODAY_OVERTIME_RANGES,
                selectedRange = state.selectedTodayOvertimeRange,
                onRangeChange = onRangeChange,
            )
            if (state.selectedTodayOvertimeRange == TodayOvertimeRange.CUSTOM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DatePickerTextField(
                        value = state.todayOvertimeStartDateInput,
                        onValueChange = onStartDateChange,
                        modifier = Modifier.weight(1f),
                        label = "From",
                    )
                    DatePickerTextField(
                        value = state.todayOvertimeEndDateInput,
                        onValueChange = onEndDateChange,
                        modifier = Modifier.weight(1f),
                        label = "To",
                    )
                }
            }
            state.todayOvertimeRangeError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB42318),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TimeStamp(label = "Actual", value = formatHoursAndMinutes(balance.actualDuration))
                TimeStamp(label = "Expected", value = formatHoursAndMinutes(balance.expectedDuration))
            }
        }
    }
}

@Composable
private fun TodayOvertimeDropdown(
    ranges: List<TodayOvertimeRange>,
    selectedRange: TodayOvertimeRange,
    onRangeChange: (TodayOvertimeRange) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Period")
                Text(
                    text = selectedRange.label,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            ranges.forEach { range ->
                DropdownMenuItem(
                    text = {
                    Text(
                        text = range.label,
                        fontWeight = if (range == selectedRange) FontWeight.SemiBold else FontWeight.Normal,
                    )
                    },
                    onClick = {
                        expanded = false
                        onRangeChange(range)
                    },
                )
            }
        }
    }
}

@Composable
private fun WorkProfileCard(
    state: TimeClockUiState,
    onProfileNameChange: (String) -> Unit,
    onProfileStartDateChange: (String) -> Unit,
    onWorkplaceTypeSelect: (WorkplaceType) -> Unit,
    onMonthlySalaryChange: (String) -> Unit,
    onHourlyRateChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onNewProfileNameChange: (String) -> Unit,
    onNewProfileStartDateChange: (String) -> Unit,
    onProfileCreate: () -> Unit,
    onProfileDelete: () -> Unit,
    onProfileStopTracking: (LocalDate) -> Unit,
    onProfileReactivate: () -> Unit,
) {
    var showStopTrackingDialog by remember { mutableStateOf(false) }
    var stopTrackingDateInput by remember(state.activeProfile.trackingEndDate) {
        mutableStateOf(formatDateInput(state.activeProfile.trackingEndDate ?: LocalDate.now()))
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
            OutlinedTextField(
                value = state.activeProfileNameInput,
                onValueChange = onProfileNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Workplace name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            DatePickerTextField(
                value = state.activeProfileStartDateInput,
                onValueChange = onProfileStartDateChange,
                modifier = Modifier.fillMaxWidth(),
                label = "Tracking start date",
            )
            state.activeProfile.trackingEndDate?.let { endDate ->
                Text(
                    text = "Stopped tracking on ${formatDateInput(endDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            WorkplaceTypeSelector(
                selectedType = state.workplaceType,
                onTypeSelect = onWorkplaceTypeSelect,
            )
            if (state.workplaceType == WorkplaceType.FIXED_HOURS_FIXED_PAY) {
                OutlinedTextField(
                    value = state.monthlySalaryInput,
                    onValueChange = onMonthlySalaryChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Monthly salary") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            if (state.workplaceType == WorkplaceType.HOURLY_PAID) {
                OutlinedTextField(
                    value = state.hourlyRateInput,
                    onValueChange = onHourlyRateChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Hourly rate") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            if (state.workplaceType != WorkplaceType.TIME_TRACKING_ONLY) {
                OutlinedTextField(
                    value = state.currencyInput,
                    onValueChange = onCurrencyChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Currency") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                )
            }
            state.paySettingsError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB42318),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (state.activeProfile.trackingEndDate == null) {
                    TextButton(onClick = { showStopTrackingDialog = true }) {
                        Text(text = "I no longer work here")
                    }
                } else {
                    TextButton(onClick = onProfileReactivate) {
                        Text(text = "Reactivate workplace")
                    }
                }
                TextButton(onClick = onProfileDelete) {
                    Text(
                        text = "Delete workplace",
                        color = Color(0xFFB42318),
                    )
                }
            }
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
                DatePickerTextField(
                    value = state.newProfileStartDateInput,
                    onValueChange = onNewProfileStartDateChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = "New workplace start date",
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

    if (showStopTrackingDialog) {
        AlertDialog(
            onDismissRequest = { showStopTrackingDialog = false },
            title = { Text(text = "Stop tracking this workplace?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "This keeps all existing data, but stops expected hours and overtime from counting after the date you choose.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    DatePickerTextField(
                        value = stopTrackingDateInput,
                        onValueChange = { stopTrackingDateInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = "Last work date",
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val endDate = runCatching { LocalDate.parse(stopTrackingDateInput) }.getOrNull()
                        if (endDate != null) {
                            onProfileStopTracking(endDate)
                            showStopTrackingDialog = false
                        }
                    },
                ) {
                    Text(text = "Stop tracking")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopTrackingDialog = false }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

@Composable
private fun WorkplaceTypeSelector(
    selectedType: WorkplaceType,
    onTypeSelect: (WorkplaceType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WorkplaceType.entries.forEach { type ->
            FilterChip(
                selected = type == selectedType,
                onClick = { onTypeSelect(type) },
                label = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = type.label,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = type.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ClockActionButton(
    isClockedIn: Boolean,
    isTrackingEnded: Boolean,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit,
) {
    Button(
        onClick = if (isClockedIn) onClockOut else onClockIn,
        enabled = isClockedIn || !isTrackingEnded,
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
            text = when {
                isClockedIn -> "Clock Out"
                isTrackingEnded -> "Workplace ended"
                else -> "Clock In"
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DailySummaryCard(state: TimeClockUiState) {
    val targetDuration = state.todayCreditedDuration.minus(state.todayBalanceDuration)
    val progress = if (targetDuration > Duration.ZERO) {
        (state.todayCreditedDuration.toMillis().toFloat() / targetDuration.toMillis().coerceAtLeast(1L))
            .coerceIn(0f, 1f)
    } else {
        0f
    }
    val remainingDuration = if (state.todayBalanceDuration.isNegative) state.todayBalanceDuration.abs() else Duration.ZERO

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                color = signedDurationColor(state.todayBalanceDuration),
                fontWeight = FontWeight.SemiBold,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(Color(0xFFE5E7EB), RoundedCornerShape(999.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(signedDurationColor(state.todayBalanceDuration), RoundedCornerShape(999.dp)),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${formatHoursAndMinutes(state.todayCreditedDuration)} worked",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (remainingDuration > Duration.ZERO) {
                        "${formatHoursAndMinutes(remainingDuration)} left"
                    } else {
                        "Target reached"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = signedDurationColor(state.todayBalanceDuration),
                    fontWeight = FontWeight.SemiBold,
                )
            }
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Work hours and breaks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

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
            DatePickerTextField(
                value = state.overtimeStartDateInput,
                onValueChange = onOvertimeStartDateChange,
                modifier = Modifier.fillMaxWidth(),
                label = "Overtime balance start date",
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
private fun ReminderSettingsCard(
    state: TimeClockUiState,
    onClockInReminderToggle: (Boolean) -> Unit,
    onClockInReminderTimeChange: (String) -> Unit,
    onClockOutReminderToggle: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            SettingSwitchRow(
                title = "Clock-in reminder",
                subtitle = "Notify at your start time on selected workdays",
                checked = state.clockInReminderEnabled,
                onCheckedChange = onClockInReminderToggle,
            )
            if (state.clockInReminderEnabled) {
                TimePickerTextField(
                    value = state.clockInReminderTimeInput,
                    onValueChange = onClockInReminderTimeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = "Start time",
                )
            }
            SettingSwitchRow(
                title = "Long-session alert",
                subtitle = "Notify at expected clock-out, then 1h, 2h, and 5h overtime",
                checked = state.clockOutReminderEnabled,
                onCheckedChange = onClockOutReminderToggle,
            )
            state.reminderSettingsError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB42318),
                )
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
    onNoteChange: (String) -> Unit,
    onSave: () -> Boolean,
    onCancel: () -> Unit,
) {
    val isEditing = state.editingSessionClockInMillis != null
    val futureClockOutWarning = manualClockOutFutureWarning(state)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
            DatePickerTextField(
                value = state.manualDateInput,
                onValueChange = onDateChange,
                modifier = Modifier.fillMaxWidth(),
                label = "Date",
            )
            TimePickerTextField(
                value = state.manualClockInInput,
                onValueChange = onClockInChange,
                modifier = Modifier.fillMaxWidth(),
                label = "Clock in",
            )
            TimePickerTextField(
                value = state.manualClockOutInput,
                onValueChange = onClockOutChange,
                modifier = Modifier.fillMaxWidth(),
                label = "Clock out",
            )
            OutlinedTextField(
                value = state.manualNoteInput,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Note") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            state.manualEntryError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB42318),
                )
            }
            futureClockOutWarning?.let { warning ->
                Text(
                    text = warning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF92400E),
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
                Button(onClick = { onSave() }) {
                    Text(text = if (isEditing) "Save changes" else "Add session")
                }
            }
        }
    }
}

@Composable
private fun AbsenceEntryCard(
    state: TimeClockUiState,
    onDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onTypeSelect: (AbsenceType) -> Unit,
    onHoursChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Boolean,
) {
    var showWorkedDayWarning by remember { mutableStateOf(false) }
    val overlappingSessionCount = absenceOverlappingSessionCount(state)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Absence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            AbsenceTypeSelector(
                selectedType = state.selectedAbsenceType,
                onTypeSelect = onTypeSelect,
            )
            DatePickerTextField(
                value = state.absenceDateInput,
                onValueChange = onDateChange,
                modifier = Modifier.fillMaxWidth(),
                label = "Start date",
            )
            DatePickerTextField(
                value = state.absenceEndDateInput,
                onValueChange = onEndDateChange,
                modifier = Modifier.fillMaxWidth(),
                label = "End date",
            )
            OutlinedTextField(
                value = state.absenceNoteInput,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            state.absenceEntryError?.let { error ->
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
                Button(
                    onClick = {
                        if (overlappingSessionCount > 0) {
                            showWorkedDayWarning = true
                        } else {
                            onSave()
                        }
                    },
                ) {
                    Text(text = "Add absence")
                }
            }
        }
    }

    if (showWorkedDayWarning) {
        AlertDialog(
            onDismissRequest = { showWorkedDayWarning = false },
            title = { Text(text = "Work already registered") },
            text = {
                Text(
                    text = "There ${if (overlappingSessionCount == 1) "is" else "are"} already $overlappingSessionCount work ${if (overlappingSessionCount == 1) "session" else "sessions"} in this absence period. If you save the absence, those worked hours will still count as actual work, while the absence removes expected hours for those days.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWorkedDayWarning = false
                        onSave()
                    },
                ) {
                    Text(text = "Save anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWorkedDayWarning = false }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

@Composable
private fun AbsenceTypeSelector(
    selectedType: AbsenceType,
    onTypeSelect: (AbsenceType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AbsenceType.entries.chunked(2).forEach { rowTypes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowTypes.forEach { type ->
                    FilterChip(
                        selected = type == selectedType,
                        onClick = { onTypeSelect(type) },
                        label = {
                            Text(
                                text = type.label,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(2 - rowTypes.size) {
                    Spacer(modifier = Modifier.weight(1f))
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
private fun EarningsCard(state: TimeClockUiState) {
    val earningsRows = buildEarningsRows(state)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Earnings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = state.workplaceType.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (earningsRows.isEmpty()) {
                Text(
                    text = when (state.workplaceType) {
                        WorkplaceType.TIME_TRACKING_ONLY -> "This workplace tracks time only."
                        WorkplaceType.FIXED_HOURS_FIXED_PAY -> "Add monthly salary in Settings to estimate earnings."
                        WorkplaceType.HOURLY_PAID -> "Add hourly rate in Settings to estimate earnings."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                earningsRows.forEach { row ->
                    EarningsSummaryRow(
                        row = row,
                        currency = state.currencyInput.ifBlank { DEFAULT_CURRENCY_INPUT },
                    )
                }
            }
        }
    }
}

@Composable
private fun EarningsSummaryRow(
    row: EarningsRow,
    currency: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = row.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = formatMoney(row.amount, currency),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF0F766E),
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = row.basis,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExportCard(
    state: TimeClockUiState,
    onShareCsvText: () -> Unit,
    onShareCsvFile: () -> Unit,
    onSaveCsv: () -> Unit,
    onSharePdf: () -> Unit,
    onSavePdf: () -> Unit,
    onExportOptionToggle: (ExportOption) -> Unit,
    onExportRangeModeSelect: (ExportRangeMode) -> Unit,
    onExportStartDateChange: (String) -> Unit,
    onExportEndDateChange: (String) -> Unit,
    onExpandToggle: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Export report",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Choose period, data, and format",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onExpandToggle) {
                    Icon(
                        imageVector = if (state.isExportExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (state.isExportExpanded) "Collapse export" else "Expand export",
                    )
                }
            }
            if (!state.isExportExpanded) return@Column
            Text(
                text = "Period",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ExportRangeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = mode == state.exportRangeMode,
                        onClick = { onExportRangeModeSelect(mode) },
                        label = {
                            Text(
                                text = mode.label,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (state.exportRangeMode == ExportRangeMode.CUSTOM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DatePickerTextField(
                        value = state.exportStartDateInput,
                        onValueChange = onExportStartDateChange,
                        modifier = Modifier.weight(1f),
                        label = "Start date",
                    )
                    DatePickerTextField(
                        value = state.exportEndDateInput,
                        onValueChange = onExportEndDateChange,
                        modifier = Modifier.weight(1f),
                        label = "End date",
                    )
                }
            }
            state.exportPeriodError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB42318),
                )
            }
            Text(
                text = "Include in export",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            ExportOption.entries.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowOptions.forEach { option ->
                        ExportOptionRow(
                            option = option,
                            checked = state.exportOptions.isEnabled(option),
                            onToggle = { onExportOptionToggle(option) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(2 - rowOptions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Text(
                text = "PDF",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onSharePdf,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = "Share PDF")
                }
                Button(
                    onClick = onSavePdf,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = "Save PDF")
                }
            }
            Text(
                text = "CSV",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onShareCsvFile,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = "Share CSV")
                }
                Button(
                    onClick = onSaveCsv,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = "Save CSV")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onShareCsvText) {
                    Text(text = "Copy/share CSV text")
                }
            }
        }
    }
}

@Composable
private fun ExportOptionRow(
    option: ExportOption,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
        )
        Text(
            text = option.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
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
        Text(
            text = "${formatDateInput(report.startDate)} to ${formatDateInput(report.endDate)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                color = signedDurationColor(balance.totalBalance),
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
                color = signedDurationColor(balance.periodBalance),
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
    onAbsenceDelete: (AbsenceEntry) -> Unit,
) {
    val historyDays = buildHistoryDays(state)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                        onAbsenceDelete = onAbsenceDelete,
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
    onAbsenceDelete: (AbsenceEntry) -> Unit,
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
                day.absences.firstOrNull()?.let { absence ->
                    Text(
                        text = formatAbsenceLabel(absence),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Medium,
                    )
                }
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
            day.absences.forEach { absence ->
                HistoryAbsenceRow(
                    absence = absence,
                    onDelete = { onAbsenceDelete(absence) },
                )
            }
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
private fun HistoryAbsenceRow(
    absence: AbsenceEntry,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp)
            .background(Color(0xFFF9FAFB), RoundedCornerShape(6.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = formatAbsenceLabel(absence),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF92400E),
            fontWeight = FontWeight.Medium,
        )
        if (absence.note.isNotBlank()) {
            Text(
                text = absence.note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onDelete) {
            Text(text = "Delete absence")
        }
    }
}

@Composable
private fun HistorySessionRow(
    session: WorkSession,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val zoneId = ZoneId.systemDefault()
    val clockIn = session.clockIn.atZone(zoneId)
    val clockOut = session.clockOut.atZone(zoneId)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp)
            .background(Color(0xFFF9FAFB), RoundedCornerShape(6.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${TIME_FORMATTER.format(clockIn)} - ${TIME_FORMATTER.format(clockOut)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = formatDateInput(clockIn.toLocalDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = formatHoursAndMinutes(session.duration),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
        if (session.note.isNotBlank()) {
            Text(
                text = session.note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun TimeStamp(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
) {
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
            color = valueColor,
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

private fun formatMoney(amount: Double, currency: String): String {
    val safeCurrency = currency.ifBlank { DEFAULT_CURRENCY_INPUT }
    return "$safeCurrency ${"%,.2f".format(amount.coerceAtLeast(0.0))}"
}

private fun manualClockOutFutureWarning(state: TimeClockUiState): String? {
    val date = runCatching { LocalDate.parse(state.manualDateInput) }.getOrNull() ?: return null
    val clockOut = runCatching { LocalTime.parse(state.manualClockOutInput, TIME_INPUT_FORMATTER) }.getOrNull()
        ?: return null
    val instant = date.atTime(clockOut).atZone(ZoneId.systemDefault()).toInstant()
    return if (instant.isAfter(Instant.now())) "Clock out is in the future." else null
}

private fun recentClockOutFutureWarning(state: TimeClockUiState): String? {
    val session = state.lastCompletedSession ?: return null
    val clockOut = runCatching { LocalTime.parse(state.recentClockOutEditInput, TIME_INPUT_FORMATTER) }.getOrNull()
        ?: return null
    val date = session.clockOut.atZone(ZoneId.systemDefault()).toLocalDate()
    val instant = date.atTime(clockOut).atZone(ZoneId.systemDefault()).toInstant()
    return if (instant.isAfter(Instant.now())) "Clock out is in the future." else null
}

private fun absenceOverlappingSessionCount(state: TimeClockUiState): Int {
    val startDate = runCatching { LocalDate.parse(state.absenceDateInput) }.getOrNull() ?: return 0
    val endDate = runCatching { LocalDate.parse(state.absenceEndDateInput) }.getOrNull() ?: return 0
    if (endDate.isBefore(startDate)) return 0

    val zoneId = ZoneId.systemDefault()
    val dates = buildDateRange(startDate, endDate)
    val activeSession = state.clockInTime?.let { WorkSession(it, Instant.now(), note = state.activeSessionNoteInput.trim()) }
    val sessions = state.completedSessions + listOfNotNull(activeSession)

    return sessions.count { session ->
        dates.any { date -> sessionOverlapsDate(session, date, zoneId) }
    }
}

private fun sessionOverlapsDate(
    session: WorkSession,
    date: LocalDate,
    zoneId: ZoneId,
): Boolean {
    val dayStart = date.atStartOfDay(zoneId).toInstant()
    val dayEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant()
    val overlapStart = maxOf(session.clockIn, dayStart)
    val overlapEnd = minOf(session.clockOut, dayEnd)
    return overlapEnd > overlapStart
}

private fun buildExportCsv(state: TimeClockUiState): String {
    val zoneId = ZoneId.systemDefault()
    val period = resolveExportPeriod(state)
    val reports = listOf(buildReport(period.label, period.startDate, period.endDate, state))
    val earningsRows = buildEarningsRowsForReports(state, reports)
    val overtimeActual = actualDurationForRange(period.startDate, period.endDate, state)
    val overtimeExpected = expectedDurationForRange(period.startDate, period.endDate, state)
    val exportBalance = overtimeActual.minus(overtimeExpected)
    val currency = state.currencyInput.ifBlank { DEFAULT_CURRENCY_INPUT }
    val activeSession = state.clockInTime?.let { WorkSession(it, Instant.now(), note = state.activeSessionNoteInput.trim()) }
    val sessions = (state.completedSessions + listOfNotNull(activeSession))
        .filter { it.overlapsExportPeriod(period, zoneId) }
        .sortedBy { it.clockIn }
    val absences = state.absences
        .filter { it.date.isInExportPeriod(period) }
        .sortedBy { it.date }
    val csv = StringBuilder()

    fun row(vararg cells: String) {
        csv.append(cells.joinToString(separator = ",") { it.toCsvCell() }).append('\n')
    }

    row("Time Clock Export")
    row("Exported", LocalDate.now().toString())
    row("Period", period.label)
    row("Start date", formatDateInput(period.startDate))
    row("End date", formatDateInput(period.endDate))
    row()

    if (state.exportOptions.includeWorkplaceSettings) {
        row("Workplace")
        row("Name", state.activeProfile.name)
        row("Tracking start date", formatDateInput(state.activeProfile.trackingStartDate))
        row("Workplace type", state.workplaceType.label)
        row("Expected per workday", formatHoursAndMinutes(state.expectedDailyDuration))
        row("Expected per week", formatHoursAndMinutes(state.expectedWeeklyDuration))
        row("Workdays", state.workDays.sortedBy { it.value }.joinToString(" ") { it.shortLabel() })
        row("Unpaid lunch", if (state.deductUnpaidLunchBreak) formatHoursAndMinutes(state.lunchBreakDuration) else "No")
        if (state.workplaceType == WorkplaceType.FIXED_HOURS_FIXED_PAY && state.monthlySalaryInput.isNotBlank()) {
            row("Monthly salary", "${currency} ${state.monthlySalaryInput}")
        }
        if (state.workplaceType == WorkplaceType.HOURLY_PAID && state.hourlyRateInput.isNotBlank()) {
            row("Hourly rate", "${currency} ${state.hourlyRateInput}")
        }
        row()
    }

    if (state.exportOptions.includeReportSummaries) {
        row("Reports")
        row("Period", "Start date", "End date", "Actual hours", "Expected hours", "Balance")
        reports.forEach { report ->
            row(
                report.label,
                formatDateInput(report.startDate),
                formatDateInput(report.endDate),
                formatHoursAndMinutes(report.actualDuration),
                formatHoursAndMinutes(report.expectedDuration),
                formatSignedBalance(report.balanceDuration),
            )
        }
        row()
    }

    if (state.exportOptions.includeOvertimeBalance) {
        row("Overtime balance")
        row("Period", "Start date", "End date", "Actual hours", "Expected hours", "Balance")
        row(
            period.label,
            formatDateInput(period.startDate),
            formatDateInput(period.endDate),
            formatHoursAndMinutes(overtimeActual),
            formatHoursAndMinutes(overtimeExpected),
            formatSignedBalance(exportBalance),
        )
        row()
    }

    if (state.exportOptions.includeEarnings) {
        row("Earnings")
        if (earningsRows.isEmpty()) {
            row("No earnings estimate available")
        } else {
            row("Period", "Amount", "Basis")
            earningsRows.forEach { earnings ->
                row(earnings.label, formatMoney(earnings.amount, currency), earnings.basis)
            }
        }
        row()
    }

    if (state.exportOptions.includeSessions) {
        row("Sessions")
        if (state.exportOptions.includeNotes) {
            row("Date", "Clock in", "Clock out", "Duration", "Status", "Note")
        } else {
            row("Date", "Clock in", "Clock out", "Duration", "Status")
        }
        sessions.forEach { session ->
            val clockIn = session.clockIn.atZone(zoneId)
            val clockOut = session.clockOut.atZone(zoneId)
            val cells = mutableListOf(
                formatDateInput(clockIn.toLocalDate()),
                TIME_FORMATTER.format(clockIn),
                TIME_FORMATTER.format(clockOut),
                formatHoursAndMinutes(session.duration),
                if (activeSession != null && session.clockIn == activeSession.clockIn) "Active" else "Completed",
            )
            if (state.exportOptions.includeNotes) {
                cells += session.note
            }
            row(*cells.toTypedArray())
        }
        row()
    }

    if (state.exportOptions.includeAbsences) {
        row("Absences")
        row("Date", "Type", "Hours", if (state.exportOptions.includeNotes) "Note" else "")
        absences.forEach { absence ->
            row(
                formatDateInput(absence.date),
                absence.type.label,
                if (absence.duration > Duration.ZERO) formatHoursAndMinutes(absence.duration) else "",
                if (state.exportOptions.includeNotes) absence.note else "",
            )
        }
    }

    return csv.toString()
}

private fun buildExportPdfBytes(state: TimeClockUiState): ByteArray {
    val document = PdfDocument()
    val output = ByteArrayOutputStream()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 40f
    var pageNumber = 1
    var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
    var canvas = page.canvas
    var y = margin

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(17, 24, 39)
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val whiteTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val whiteBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 10.5f
    }
    val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(15, 118, 110)
        textSize = 15f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(31, 41, 55)
        textSize = 10.5f
    }
    val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(75, 85, 99)
        textSize = 9.5f
    }
    val cardTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(75, 85, 99)
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val cardValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(17, 24, 39)
        textSize = 13f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val tealFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(15, 118, 110)
        style = Paint.Style.FILL
    }
    val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(246, 251, 249)
        style = Paint.Style.FILL
    }

    fun newPage() {
        document.finishPage(page)
        pageNumber += 1
        page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        canvas = page.canvas
        y = margin
    }

    fun ensureSpace(height: Float) {
        if (y + height > pageHeight - margin) newPage()
    }

    fun line(text: String, paint: Paint = bodyPaint, indent: Float = 0f, gap: Float = 16f) {
        ensureSpace(gap)
        canvas.drawText(text, margin + indent, y, paint)
        y += gap
    }

    fun section(title: String) {
        ensureSpace(30f)
        y += 8f
        line(title, headingPaint, gap = 20f)
    }

    fun keyValue(key: String, value: String) {
        line("$key: $value", bodyPaint, gap = 14f)
    }

    fun summaryCard(x: Float, title: String, value: String) {
        val width = 122f
        val height = 54f
        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 12f, 12f, cardFillPaint)
        canvas.drawText(title, x + 10f, y + 18f, cardTitlePaint)
        canvas.drawText(value, x + 10f, y + 39f, cardValuePaint)
    }

    val period = resolveExportPeriod(state)
    val reports = listOf(buildReport(period.label, period.startDate, period.endDate, state))
    val earningsRows = buildEarningsRowsForReports(state, reports)
    val overtimeActual = actualDurationForRange(period.startDate, period.endDate, state)
    val overtimeExpected = expectedDurationForRange(period.startDate, period.endDate, state)
    val exportBalance = overtimeActual.minus(overtimeExpected)
    val currency = state.currencyInput.ifBlank { DEFAULT_CURRENCY_INPUT }
    val zoneId = ZoneId.systemDefault()
    val activeSession = state.clockInTime?.let { WorkSession(it, Instant.now(), note = state.activeSessionNoteInput.trim()) }
    val sessions = (state.completedSessions + listOfNotNull(activeSession))
        .filter { it.overlapsExportPeriod(period, zoneId) }
        .sortedByDescending { it.clockIn }
    val absences = state.absences
        .filter { it.date.isInExportPeriod(period) }
        .sortedByDescending { it.date }
    val earningsTotal = earningsRows.sumOf { it.amount }

    canvas.drawRoundRect(RectF(margin, margin, pageWidth - margin, 128f), 18f, 18f, tealFillPaint)
    canvas.drawText("Time Clock Report", margin + 18f, 76f, whiteTitlePaint)
    canvas.drawText(state.activeProfile.name, margin + 18f, 98f, whiteBodyPaint)
    canvas.drawText("${period.label}: ${formatDateInput(period.startDate)} to ${formatDateInput(period.endDate)}", margin + 18f, 116f, whiteBodyPaint)
    y = 158f

    summaryCard(margin, "Worked", formatHoursAndMinutes(overtimeActual))
    summaryCard(margin + 132f, "Expected", formatHoursAndMinutes(overtimeExpected))
    summaryCard(margin + 264f, "Balance", formatSignedBalance(exportBalance))
    summaryCard(margin + 396f, "Earnings", if (earningsRows.isEmpty()) "-" else formatMoney(earningsTotal, currency))
    y += 76f

    line("Generated ${formatDateInput(LocalDate.now())}", mutedPaint, gap = 18f)

    if (state.exportOptions.includeWorkplaceSettings) {
        section("Workplace")
        keyValue("Tracking start", formatDateInput(state.activeProfile.trackingStartDate))
        keyValue("Type", state.workplaceType.label)
        keyValue("Expected workday", formatHoursAndMinutes(state.expectedDailyDuration))
        keyValue("Expected week", formatHoursAndMinutes(state.expectedWeeklyDuration))
        keyValue("Workdays", state.workDays.sortedBy { it.value }.joinToString(" ") { it.shortLabel() })
        keyValue("Unpaid lunch", if (state.deductUnpaidLunchBreak) formatHoursAndMinutes(state.lunchBreakDuration) else "No")
        if (state.workplaceType == WorkplaceType.FIXED_HOURS_FIXED_PAY && state.monthlySalaryInput.isNotBlank()) {
            keyValue("Monthly salary", "$currency ${state.monthlySalaryInput}")
        }
        if (state.workplaceType == WorkplaceType.HOURLY_PAID && state.hourlyRateInput.isNotBlank()) {
            keyValue("Hourly rate", "$currency ${state.hourlyRateInput}")
        }
    }

    if (state.exportOptions.includeReportSummaries) {
        section("Report Summary")
        reports.forEach { report ->
            line(
                "${report.label}: ${formatHoursAndMinutes(report.actualDuration)} worked, ${formatHoursAndMinutes(report.expectedDuration)} expected, ${formatSignedBalance(report.balanceDuration)}",
                bodyPaint,
                gap = 14f,
            )
            line("${formatDateInput(report.startDate)} to ${formatDateInput(report.endDate)}", mutedPaint, indent = 10f, gap = 12f)
        }
    }

    if (state.exportOptions.includeOvertimeBalance) {
        section("Overtime Balance")
        keyValue("Period", "${period.label}: ${formatDateInput(period.startDate)} to ${formatDateInput(period.endDate)}")
        keyValue("Actual", formatHoursAndMinutes(overtimeActual))
        keyValue("Expected", formatHoursAndMinutes(overtimeExpected))
        keyValue("Balance", formatSignedBalance(exportBalance))
    }

    if (state.exportOptions.includeEarnings) {
        section("Earnings")
        if (earningsRows.isEmpty()) {
            line("No earnings estimate available", mutedPaint, gap = 14f)
        } else {
            earningsRows.forEach { row ->
                line("${row.label}: ${formatMoney(row.amount, currency)}", bodyPaint, gap = 14f)
                line(row.basis, mutedPaint, indent = 10f, gap = 12f)
            }
        }
    }

    if (state.exportOptions.includeSessions) {
        section("Recent Sessions")
        if (sessions.isEmpty()) {
            line("No sessions yet", mutedPaint, gap = 14f)
        } else {
            sessions.take(30).forEach { session ->
                val clockIn = session.clockIn.atZone(zoneId)
                val clockOut = session.clockOut.atZone(zoneId)
                val status = if (activeSession != null && session.clockIn == activeSession.clockIn) "active" else "completed"
                val note = if (state.exportOptions.includeNotes && session.note.isNotBlank()) " - ${session.note}" else ""
                line(
                    "${formatDateInput(clockIn.toLocalDate())}  ${TIME_FORMATTER.format(clockIn)}-${TIME_FORMATTER.format(clockOut)}  ${formatHoursAndMinutes(session.duration)}  $status$note",
                    bodyPaint,
                    gap = 14f,
                )
            }
        }
    }

    if (state.exportOptions.includeAbsences) {
        section("Absences")
        if (absences.isEmpty()) {
            line("No absence entries", mutedPaint, gap = 14f)
        } else {
            absences.take(40).forEach { absence ->
                val hours = if (absence.duration > Duration.ZERO) " (${formatHoursAndMinutes(absence.duration)})" else ""
                val note = if (state.exportOptions.includeNotes && absence.note.isNotBlank()) " - ${absence.note}" else ""
                line("${formatDateInput(absence.date)}  ${absence.type.label}$hours$note", bodyPaint, gap = 14f)
            }
        }
    }

    document.finishPage(page)
    document.writeTo(output)
    document.close()
    return output.toByteArray()
}

private fun String.toCsvCell(): String {
    val escaped = replace("\"", "\"\"")
    return "\"$escaped\""
}

private fun String.toFileNamePart(): String {
    return lowercase()
        .replace(Regex("""[^a-z0-9]+"""), "-")
        .trim('-')
        .ifBlank { "workplace" }
}

private fun ExportOptions.isEnabled(option: ExportOption): Boolean {
    return when (option) {
        ExportOption.WORKPLACE_SETTINGS -> includeWorkplaceSettings
        ExportOption.REPORT_SUMMARIES -> includeReportSummaries
        ExportOption.OVERTIME_BALANCE -> includeOvertimeBalance
        ExportOption.EARNINGS -> includeEarnings
        ExportOption.SESSIONS -> includeSessions
        ExportOption.ABSENCES -> includeAbsences
        ExportOption.NOTES -> includeNotes
    }
}

private fun resolveExportPeriod(state: TimeClockUiState): ExportPeriod {
    val today = LocalDate.now()
    if (state.exportRangeMode == ExportRangeMode.ALL_REGISTERED) {
        return ExportPeriod(
            label = ExportRangeMode.ALL_REGISTERED.label,
            startDate = state.activeProfile.trackingStartDate,
            endDate = today,
        )
    }

    val parsedStart = runCatching { LocalDate.parse(state.exportStartDateInput) }.getOrNull()
    val parsedEnd = runCatching { LocalDate.parse(state.exportEndDateInput) }.getOrNull()
    val startDate = parsedStart ?: today
    val endDate = parsedEnd ?: startDate
    val safeEndDate = if (endDate.isBefore(startDate)) startDate else endDate

    return ExportPeriod(
        label = ExportRangeMode.CUSTOM.label,
        startDate = startDate,
        endDate = safeEndDate,
    )
}

private fun exportPeriodError(
    mode: ExportRangeMode,
    startInput: String,
    endInput: String,
): String? {
    if (mode == ExportRangeMode.ALL_REGISTERED) return null
    val startDate = runCatching { LocalDate.parse(startInput) }.getOrNull()
    val endDate = runCatching { LocalDate.parse(endInput) }.getOrNull()

    return when {
        startDate == null || endDate == null -> "Use dates like YYYY-MM-DD."
        endDate.isBefore(startDate) -> "End date must be after start date."
        else -> null
    }
}

private fun todayOvertimeRangeError(
    range: TodayOvertimeRange,
    startInput: String,
    endInput: String,
): String? {
    if (range != TodayOvertimeRange.CUSTOM) return null
    val startDate = runCatching { LocalDate.parse(startInput) }.getOrNull()
    val endDate = runCatching { LocalDate.parse(endInput) }.getOrNull()

    return when {
        startDate == null || endDate == null -> "Use dates like YYYY-MM-DD."
        endDate.isBefore(startDate) -> "End date must be after start date."
        else -> null
    }
}

private fun WorkSession.overlapsExportPeriod(
    period: ExportPeriod,
    zoneId: ZoneId,
): Boolean {
    val rangeStart = period.startDate.atStartOfDay(zoneId).toInstant()
    val rangeEnd = period.endDate.plusDays(1).atStartOfDay(zoneId).toInstant()
    return clockOut > rangeStart && clockIn < rangeEnd
}

private fun LocalDate.isInExportPeriod(period: ExportPeriod): Boolean {
    return !isBefore(period.startDate) && !isAfter(period.endDate)
}

private fun Duration.toHoursDecimal(): Double {
    return toMinutes().coerceAtLeast(0).toDouble() / 60.0
}

private fun formatProgressMessage(
    isTodayWorkday: Boolean,
    absence: AbsenceEntry?,
    worked: Duration,
    expected: Duration,
): String {
    if (absence != null && absence.type.coversExpectedHours) return "${absence.type.label} today"
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
    val sessionsByDate = state.completedSessions.groupBy { it.clockIn.atZone(zoneId).toLocalDate() }
    val absencesByDate = state.absences.groupBy { it.date }
    val historyDates = (sessionsByDate.keys + absencesByDate.keys).toSet()

    return historyDates
        .map { date ->
            val sessions = sessionsByDate[date].orEmpty()
            val sortedSessions = sessions.sortedBy { it.clockIn }
            val totalDuration = sortedSessions.fold(Duration.ZERO) { total, session ->
                total.plus(session.duration)
            }
            val expectedDuration = expectedDurationForRange(date, date, state)

            WorkDayHistory(
                date = date,
                sessions = sortedSessions,
                absences = absencesByDate[date].orEmpty(),
                totalDuration = totalDuration,
                expectedDuration = expectedDuration,
            )
        }
        .sortedByDescending { it.date }
}

private fun buildReports(state: TimeClockUiState): List<WorkReport> {
    val today = LocalDate.now()
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = minOf(weekStart.plusDays(6), today)
    val monthStart = today.withDayOfMonth(1)
    val monthEnd = today
    val halfYearStart = if (today.monthValue <= 6) {
        LocalDate.of(today.year, 1, 1)
    } else {
        LocalDate.of(today.year, 7, 1)
    }
    val halfYearEnd = if (today.monthValue <= 6) {
        minOf(LocalDate.of(today.year, 6, 30), today)
    } else {
        minOf(LocalDate.of(today.year, 12, 31), today)
    }
    val yearStart = LocalDate.of(today.year, 1, 1)
    val yearEnd = today

    return listOf(
        buildReport("Today", today, today, state),
        buildReport("This week", weekStart, weekEnd, state),
        buildReport("This month", monthStart, monthEnd, state),
        buildReport("Half year", halfYearStart, halfYearEnd, state),
        buildReport("This year", yearStart, yearEnd, state),
    )
}

private fun buildEarningsRows(state: TimeClockUiState): List<EarningsRow> {
    return buildEarningsRowsForReports(state, buildReports(state))
}

private fun buildEarningsRowsForReports(
    state: TimeClockUiState,
    reports: List<WorkReport>,
): List<EarningsRow> {
    if (state.workplaceType == WorkplaceType.TIME_TRACKING_ONLY) return emptyList()

    return when (state.workplaceType) {
        WorkplaceType.HOURLY_PAID -> {
            val hourlyRate = state.hourlyRateInput.toMoneyOrNull() ?: return emptyList()
            reports.map { report ->
                val hours = report.actualDuration.toHoursDecimal()
                EarningsRow(
                    label = report.label,
                    amount = hours * hourlyRate,
                    basis = "${formatHoursAndMinutes(report.actualDuration)} worked at ${formatMoney(hourlyRate, state.currencyInput.ifBlank { DEFAULT_CURRENCY_INPUT })}/h",
                )
            }
        }
        WorkplaceType.FIXED_HOURS_FIXED_PAY -> {
            val monthlySalary = state.monthlySalaryInput.toMoneyOrNull() ?: return emptyList()
            val expectedWeeklyHours = state.expectedWeeklyDuration.toHoursDecimal()
            if (expectedWeeklyHours <= 0.0) return emptyList()
            val hourlyEquivalent = (monthlySalary * 12.0) / (expectedWeeklyHours * 52.0)

            reports.map { report ->
                val paidDuration = if (report.expectedDuration > Duration.ZERO) {
                    report.expectedDuration
                } else {
                    report.actualDuration
                }
                EarningsRow(
                    label = report.label,
                    amount = paidDuration.toHoursDecimal() * hourlyEquivalent,
                    basis = "${formatHoursAndMinutes(paidDuration)} salary value from ${formatMoney(monthlySalary, state.currencyInput.ifBlank { DEFAULT_CURRENCY_INPUT })}/month",
                )
            }
        }
        WorkplaceType.TIME_TRACKING_ONLY -> emptyList()
    }
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

private fun buildTodayOvertimeBalance(state: TimeClockUiState): TodayOvertimeBalance {
    val today = LocalDate.now()
    val defaultStartDate = todayStartDateForOvertimeRange(
        range = state.selectedTodayOvertimeRange,
        today = today,
        workplaceStartDate = state.activeProfile.trackingStartDate,
    )
    val parsedCustomStart = runCatching { LocalDate.parse(state.todayOvertimeStartDateInput) }.getOrNull()
    val parsedCustomEnd = runCatching { LocalDate.parse(state.todayOvertimeEndDateInput) }.getOrNull()
    val rawStartDate = if (state.selectedTodayOvertimeRange == TodayOvertimeRange.CUSTOM) {
        parsedCustomStart ?: today
    } else {
        defaultStartDate
    }
    val rawEndDate = if (state.selectedTodayOvertimeRange == TodayOvertimeRange.CUSTOM) {
        parsedCustomEnd ?: rawStartDate
    } else {
        today
    }
    val startDate = rawStartDate.coerceAtLeast(state.activeProfile.trackingStartDate)
    val endDate = if (rawEndDate.isBefore(startDate)) startDate else rawEndDate
    val actualDuration = actualDurationForRange(startDate, endDate, state)
    val expectedDuration = expectedDurationForRange(startDate, endDate, state)

    return TodayOvertimeBalance(
        label = state.selectedTodayOvertimeRange.label,
        startDate = startDate,
        endDate = endDate,
        actualDuration = actualDuration,
        expectedDuration = expectedDuration,
        startingBalance = Duration.ZERO,
    )
}

private fun todayStartDateForOvertimeRange(
    range: TodayOvertimeRange,
    today: LocalDate,
    workplaceStartDate: LocalDate,
): LocalDate {
    return when (range) {
        TodayOvertimeRange.ONE_WEEK -> today.minusWeeks(1).plusDays(1)
        TodayOvertimeRange.FOUR_WEEKS -> today.minusWeeks(4).plusDays(1)
        TodayOvertimeRange.SIX_MONTHS -> today.minusMonths(6).plusDays(1)
        TodayOvertimeRange.TWELVE_MONTHS -> today.minusYears(1).plusDays(1)
        TodayOvertimeRange.ALL_TIME -> workplaceStartDate
        TodayOvertimeRange.CUSTOM -> today
    }
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
        val isBeforeTrackingStart = date.isBefore(state.activeProfile.trackingStartDate)
        DailyChartEntry(
            date = date,
            actualDuration = if (isBeforeTrackingStart) Duration.ZERO else actualDurationForRange(date, date, state),
            expectedDuration = if (isBeforeTrackingStart) Duration.ZERO else expectedDurationForRange(date, date, state),
        )
    }
}

private fun buildCurrentWeekReport(state: TimeClockUiState): WorkReport {
    val today = LocalDate.now()
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = minOf(weekStart.plusDays(6), today)
    return buildReport("This week", weekStart, weekEnd, state)
}

private fun buildMonthlyTrendEntries(state: TimeClockUiState): List<MonthlyTrendEntry> {
    val today = LocalDate.now()
    val monthStart = today.withDayOfMonth(1)
    val monthEnd = today
    val entries = mutableListOf<MonthlyTrendEntry>()
    var weekStart = monthStart
    var weekNumber = 1

    while (!weekStart.isAfter(monthEnd)) {
        val weekEnd = minOf(weekStart.plusDays(6), monthEnd)
        val effectiveWeekStart = weekStart.coerceAtLeast(state.activeProfile.trackingStartDate)
        val actualDuration = if (effectiveWeekStart.isAfter(weekEnd)) {
            Duration.ZERO
        } else {
            actualDurationForRange(effectiveWeekStart, weekEnd, state)
        }
        val expectedDuration = if (effectiveWeekStart.isAfter(weekEnd)) {
            Duration.ZERO
        } else {
            expectedDurationForRange(effectiveWeekStart, weekEnd, state)
        }
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
        val isOutsideTrackedRange = date.isBefore(state.activeProfile.trackingStartDate) || date.isAfter(today)
        days.add(
            CalendarDayVisual(
                date = date,
                actualDuration = if (isOutsideTrackedRange) Duration.ZERO else actualDurationForRange(date, date, state),
                expectedDuration = if (isOutsideTrackedRange) Duration.ZERO else expectedDurationForRange(date, date, state),
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
    val today = LocalDate.now()
    val effectiveStartDate = startDate.coerceAtLeast(state.activeProfile.trackingStartDate)
    val effectiveEndDate = minOf(endDate, today)
    val hasStartedInPeriod = !effectiveStartDate.isAfter(effectiveEndDate)
    val actualDuration = if (hasStartedInPeriod) {
        actualDurationForRange(effectiveStartDate, effectiveEndDate, state)
    } else {
        Duration.ZERO
    }
    val expectedDuration = if (hasStartedInPeriod) {
        expectedDurationForRange(effectiveStartDate, effectiveEndDate, state)
    } else {
        Duration.ZERO
    }

    return WorkReport(
        label = label,
        startDate = if (hasStartedInPeriod) effectiveStartDate else effectiveEndDate,
        endDate = effectiveEndDate,
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
    val activeSession = state.clockInTime?.let { WorkSession(it, Instant.now(), note = state.activeSessionNoteInput.trim()) }
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
    val effectiveEndDate = state.activeProfile.trackingEndDate?.let { minOf(endDate, it) } ?: endDate
    if (date.isAfter(effectiveEndDate)) return Duration.ZERO

    val dailyExpected = state.expectedDailyDuration.plus(
        if (state.deductUnpaidLunchBreak) state.lunchBreakDuration else Duration.ZERO,
    )
    var expected = Duration.ZERO

    while (!date.isAfter(effectiveEndDate)) {
        val hasCoveredAbsence = state.absences.any { it.date == date && it.type.coversExpectedHours }
        if (date.dayOfWeek in state.workDays && !hasCoveredAbsence) {
            expected = expected.plus(dailyExpected)
        }
        date = date.plusDays(1)
    }

    return expected
}

private fun formatHistoryBalance(day: WorkDayHistory): String {
    val coveredAbsence = day.absences.firstOrNull { it.type.coversExpectedHours }
    if (coveredAbsence != null) {
        return "Covered by ${coveredAbsence.type.label.lowercase()}"
    }

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

private fun formatAbsenceLabel(absence: AbsenceEntry): String {
    return absence.type.label
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

private fun signedDurationColor(duration: Duration): Color {
    return if (duration.isNegative) {
        Color(0xFFB42318)
    } else {
        Color(0xFF0F766E)
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

private fun String.toMoneyOrNull(): Double? {
    val normalized = trim().replace(",", ".")
    if (normalized.isBlank()) return null
    val amount = normalized.toDoubleOrNull() ?: return null
    return amount.takeIf { it >= 0.0 }
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

private fun formatTimeInput(instant: Instant): String {
    return TIME_INPUT_FORMATTER.format(instant.atZone(ZoneId.systemDefault()).toLocalTime())
}

private fun formatDateInput(date: LocalDate): String {
    return DATE_INPUT_FORMATTER.format(date)
}

private fun buildDateRange(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
    return generateSequence(startDate) { date ->
        date.plusDays(1).takeUnless { it.isAfter(endDate) }
    }.toList()
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
private const val REMINDER_NOTIFICATION_CHANNEL_ID = "time_clock_reminders"
private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1201
private const val CLOCK_OUT_REMINDER_REQUEST_CODE = 1202
private const val CLOCK_OUT_REMINDER_NOTIFICATION_ID = 1203
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
private val DEFAULT_CLOCK_IN_REMINDER_INPUT = "08:00"
private val LONG_SESSION_ALERTS = listOf(
    Duration.ZERO,
    Duration.ofHours(1),
    Duration.ofHours(2),
    Duration.ofHours(5),
)
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
private val TODAY_OVERTIME_RANGES = listOf(
    TodayOvertimeRange.ONE_WEEK,
    TodayOvertimeRange.FOUR_WEEKS,
    TodayOvertimeRange.SIX_MONTHS,
    TodayOvertimeRange.TWELVE_MONTHS,
    TodayOvertimeRange.ALL_TIME,
    TodayOvertimeRange.CUSTOM,
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
                deductUnpaidLunchBreak = true,
                todayCreditedDuration = Duration.ofHours(5).plusMinutes(55),
                todayBreakDeduction = Duration.ofMinutes(30),
                recentClockOutEditInput = "16:40",
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
            onTabSelect = {},
            onClockIn = {},
            onClockOut = {},
            onActiveClockInChange = {},
            onActiveClockInSave = { true },
            onRecentClockOutChange = {},
            onRecentClockOutSave = { true },
            onTodayOvertimeRangeChange = {},
            onTodayOvertimeStartDateChange = {},
            onTodayOvertimeEndDateChange = {},
            onInsightsSectionSelect = {},
            onExportExpandToggle = {},
            onProfileSelect = {},
            onProfileNameChange = {},
            onProfileStartDateChange = {},
            onWorkplaceTypeSelect = {},
            onMonthlySalaryChange = {},
            onHourlyRateChange = {},
            onCurrencyChange = {},
            onNewProfileNameChange = {},
            onNewProfileStartDateChange = {},
            onProfileCreate = {},
            onProfileDelete = {},
            onProfileStopTracking = { _ -> },
            onProfileReactivate = {},
            onHistoryDayToggle = {},
            onManualDateChange = {},
            onManualClockInChange = {},
            onManualClockOutChange = {},
            onManualNoteChange = {},
            onManualSessionSave = { true },
            onManualSessionCancel = {},
            onAbsenceDateChange = {},
            onAbsenceEndDateChange = {},
            onAbsenceTypeSelect = {},
            onAbsenceHoursChange = {},
            onAbsenceNoteChange = {},
            onAbsenceSave = { true },
            onAbsenceDelete = {},
            onSessionEdit = {},
            onSessionDelete = {},
            onExpectedDailyHoursChange = {},
            onExpectedWeeklyHoursChange = {},
            onWorkdayToggle = {},
            onUnpaidLunchBreakToggle = {},
            onLunchBreakMinutesChange = {},
            onOvertimeStartDateChange = {},
            onStartingOvertimeBalanceChange = {},
            onOvertimeRangeChange = {},
            onShareCsvText = {},
            onShareCsvFile = {},
            onSaveCsv = {},
            onSharePdf = {},
            onSavePdf = {},
            onExportOptionToggle = {},
            onExportRangeModeSelect = {},
            onExportStartDateChange = {},
            onExportEndDateChange = {},
            onClockInReminderToggle = {},
            onClockInReminderTimeChange = {},
            onClockOutReminderToggle = {},
            onActiveSessionNoteChange = {},
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
                activeClockInEditInput = "08:45",
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
            onTabSelect = {},
            onClockIn = {},
            onClockOut = {},
            onActiveClockInChange = {},
            onActiveClockInSave = { true },
            onRecentClockOutChange = {},
            onRecentClockOutSave = { true },
            onTodayOvertimeRangeChange = {},
            onTodayOvertimeStartDateChange = {},
            onTodayOvertimeEndDateChange = {},
            onInsightsSectionSelect = {},
            onExportExpandToggle = {},
            onProfileSelect = {},
            onProfileNameChange = {},
            onProfileStartDateChange = {},
            onWorkplaceTypeSelect = {},
            onMonthlySalaryChange = {},
            onHourlyRateChange = {},
            onCurrencyChange = {},
            onNewProfileNameChange = {},
            onNewProfileStartDateChange = {},
            onProfileCreate = {},
            onProfileDelete = {},
            onProfileStopTracking = { _ -> },
            onProfileReactivate = {},
            onHistoryDayToggle = {},
            onManualDateChange = {},
            onManualClockInChange = {},
            onManualClockOutChange = {},
            onManualNoteChange = {},
            onManualSessionSave = { true },
            onManualSessionCancel = {},
            onAbsenceDateChange = {},
            onAbsenceEndDateChange = {},
            onAbsenceTypeSelect = {},
            onAbsenceHoursChange = {},
            onAbsenceNoteChange = {},
            onAbsenceSave = { true },
            onAbsenceDelete = {},
            onSessionEdit = {},
            onSessionDelete = {},
            onExpectedDailyHoursChange = {},
            onExpectedWeeklyHoursChange = {},
            onWorkdayToggle = {},
            onUnpaidLunchBreakToggle = {},
            onLunchBreakMinutesChange = {},
            onOvertimeStartDateChange = {},
            onStartingOvertimeBalanceChange = {},
            onOvertimeRangeChange = {},
            onShareCsvText = {},
            onShareCsvFile = {},
            onSaveCsv = {},
            onSharePdf = {},
            onSavePdf = {},
            onExportOptionToggle = {},
            onExportRangeModeSelect = {},
            onExportStartDateChange = {},
            onExportEndDateChange = {},
            onClockInReminderToggle = {},
            onClockInReminderTimeChange = {},
            onClockOutReminderToggle = {},
            onActiveSessionNoteChange = {},
        )
    }
}
