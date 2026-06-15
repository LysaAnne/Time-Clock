package com.annelysa.timeclock

import android.app.PendingIntent
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RemoteViews
import android.widget.TextView
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TimeClockTodayWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildTodayWidget(context, appWidgetId))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_WIDGET_CLOCK_IN -> clockInFromWidget(context, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID))
            ACTION_WIDGET_CLOCK_OUT -> clockOutFromWidget(context, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID))
            ACTION_WIDGET_ADJUST_BACK -> adjustWidgetTime(context, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID), -WIDGET_QUICK_ADJUST_MINUTES)
            ACTION_WIDGET_ADJUST_FORWARD -> adjustWidgetTime(context, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID), WIDGET_QUICK_ADJUST_MINUTES)
        }
        updateTimeClockWidgets(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        removeWidgetProfileSelections(context, appWidgetIds)
    }
}

class TimeClockBalanceWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildBalanceWidget(context, appWidgetId))
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        removeWidgetProfileSelections(context, appWidgetIds)
    }
}

class TimeClockWidgetConfigureActivity : Activity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val preferences = getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
        val profiles = decodeWidgetProfiles(preferences.getString(WIDGET_KEY_WORK_PROFILES, null))
        val activeProfileId = activeWidgetProfileId(preferences)
        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
        }

        profiles.forEachIndexed { index, profile ->
            val radioButton = RadioButton(this).apply {
                id = View.generateViewId()
                text = profile.name
                tag = profile.id
                textSize = 18f
                isChecked = profile.id == activeProfileId || (index == 0 && activeProfileId !in profiles.map { it.id })
            }
            radioGroup.addView(radioButton)
        }

        val saveButton = Button(this).apply {
            text = "Use selected workplace"
            setOnClickListener {
                val checkedButton = radioGroup.findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
                val selectedProfileId = checkedButton?.tag as? String ?: profiles.first().id
                saveWidgetProfileSelection(
                    context = this@TimeClockWidgetConfigureActivity,
                    appWidgetId = appWidgetId,
                    profileId = selectedProfileId,
                )
                updateSingleWidget(this@TimeClockWidgetConfigureActivity, appWidgetId)

                setResult(
                    RESULT_OK,
                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
                )
                finish()
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 36, 36, 36)
            addView(
                TextView(this@TimeClockWidgetConfigureActivity).apply {
                    text = "Choose workplace"
                    textSize = 22f
                },
            )
            addView(radioGroup)
            addView(saveButton)
        }

        setContentView(root)
    }
}

fun updateTimeClockWidgets(context: Context) {
    val appWidgetManager = AppWidgetManager.getInstance(context)

    val todayComponent = ComponentName(context, TimeClockTodayWidgetProvider::class.java)
    val todayWidgetIds = appWidgetManager.getAppWidgetIds(todayComponent)
    todayWidgetIds.forEach { appWidgetId ->
        appWidgetManager.updateAppWidget(appWidgetId, buildTodayWidget(context, appWidgetId))
    }

    val balanceComponent = ComponentName(context, TimeClockBalanceWidgetProvider::class.java)
    val balanceWidgetIds = appWidgetManager.getAppWidgetIds(balanceComponent)
    balanceWidgetIds.forEach { appWidgetId ->
        appWidgetManager.updateAppWidget(appWidgetId, buildBalanceWidget(context, appWidgetId))
    }
}

private fun updateSingleWidget(context: Context, appWidgetId: Int) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val providerClassName = appWidgetManager.getAppWidgetInfo(appWidgetId)?.provider?.className
    val views = if (providerClassName == TimeClockBalanceWidgetProvider::class.java.name) {
        buildBalanceWidget(context, appWidgetId)
    } else {
        buildTodayWidget(context, appWidgetId)
    }
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun buildTodayWidget(context: Context, appWidgetId: Int): RemoteViews {
    val snapshot = loadWidgetSnapshot(context, appWidgetId)
    val isClockedIn = snapshot.activeClockInMillis != null
    val buttonAction = if (isClockedIn) ACTION_WIDGET_CLOCK_OUT else ACTION_WIDGET_CLOCK_IN

    return RemoteViews(context.packageName, R.layout.widget_time_clock_today).apply {
        setTextViewText(R.id.widget_workplace_name, snapshot.profileName)
        setTextViewText(R.id.widget_clock_action, if (isClockedIn) "Clock out" else "Clock in")
        setTextViewText(R.id.widget_today_total, "${formatWidgetDuration(snapshot.todayWorkedDuration)} today")
        setTextViewText(R.id.widget_timer_fallback, snapshot.activeDurationLabel)
        setTextViewText(R.id.widget_correction_label, if (isClockedIn) "Adjust start" else "Adjust last out")
        setViewVisibility(R.id.widget_correction_label, if (isClockedIn || snapshot.hasCompletedSession) View.VISIBLE else View.GONE)
        setViewVisibility(R.id.widget_correction_row, if (isClockedIn || snapshot.hasCompletedSession) View.VISIBLE else View.GONE)
        setViewVisibility(R.id.widget_active_timer, if (isClockedIn) View.VISIBLE else View.GONE)
        setViewVisibility(R.id.widget_timer_fallback, if (isClockedIn) View.GONE else View.VISIBLE)
        if (isClockedIn) {
            setChronometer(
                R.id.widget_active_timer,
                android.os.SystemClock.elapsedRealtime() - snapshot.activeDuration.toMillis(),
                null,
                true,
            )
        }
        setInt(
            R.id.widget_clock_button_container,
            "setBackgroundResource",
            if (isClockedIn) R.drawable.widget_button_background_red else R.drawable.widget_button_background,
        )
        setOnClickPendingIntent(R.id.widget_clock_button_container, widgetActionPendingIntent(context, buttonAction, appWidgetId))
        setOnClickPendingIntent(R.id.widget_correction_minus, widgetActionPendingIntent(context, ACTION_WIDGET_ADJUST_BACK, appWidgetId))
        setOnClickPendingIntent(R.id.widget_correction_plus, widgetActionPendingIntent(context, ACTION_WIDGET_ADJUST_FORWARD, appWidgetId))
    }
}

private fun buildBalanceWidget(context: Context, appWidgetId: Int): RemoteViews {
    val snapshot = loadWidgetSnapshot(context, appWidgetId)

    return RemoteViews(context.packageName, R.layout.widget_time_clock_balance).apply {
        setTextViewText(R.id.widget_balance_workplace_name, snapshot.profileName)
        setTextViewText(R.id.widget_balance_range, snapshot.overtimeRangeLabel)
        setTextViewText(R.id.widget_balance_value, formatWidgetSignedDuration(snapshot.overtimeBalance))
        setTextViewText(R.id.widget_balance_hint, snapshot.balanceHint)
        setOnClickPendingIntent(R.id.widget_balance_root, openAppPendingIntent(context, WIDGET_OPEN_APP_REQUEST_CODE_BALANCE))
    }
}

private fun clockInFromWidget(context: Context, appWidgetId: Int) {
    val preferences = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
    val profileId = widgetProfileId(preferences, appWidgetId)
    val activeKey = widgetProfileKey(profileId, WIDGET_KEY_ACTIVE_CLOCK_IN)
    if (preferences.contains(activeKey)) return

    val clockInMillis = Instant.now().toEpochMilli()
    preferences.edit()
        .putLong(activeKey, clockInMillis)
        .remove(widgetProfileKey(profileId, WIDGET_KEY_CLOCK_OUT_REMINDER_SENT_MASK))
        .remove(WIDGET_KEY_ACTIVE_CLOCK_IN)
        .apply()

    scheduleWidgetLongSessionReminderIfNeeded(context, preferences, profileId, clockInMillis)
}

private fun clockOutFromWidget(context: Context, appWidgetId: Int) {
    val preferences = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
    val profileId = widgetProfileId(preferences, appWidgetId)
    val activeKey = widgetProfileKey(profileId, WIDGET_KEY_ACTIVE_CLOCK_IN)
    val clockInMillis = preferences.getLongOrNull(activeKey) ?: return
    val clockOutMillis = Instant.now().toEpochMilli()
    if (clockOutMillis < clockInMillis) return

    val sessionsKey = widgetProfileKey(profileId, WIDGET_KEY_COMPLETED_SESSIONS)
    val existingSessions = preferences.getString(sessionsKey, null).orEmpty()
    val newSession = "$clockInMillis,$clockOutMillis"
    val encodedSessions = listOf(existingSessions, newSession)
        .filter { it.isNotBlank() }
        .joinToString(separator = "\n")

    preferences.edit()
        .putString(sessionsKey, encodedSessions)
        .remove(activeKey)
        .remove(widgetProfileKey(profileId, WIDGET_KEY_CLOCK_OUT_REMINDER_SENT_MASK))
        .remove(WIDGET_KEY_ACTIVE_CLOCK_IN)
        .apply()

    cancelLongSessionReminders(
        context = context,
        profileId = profileId,
        clockInMillis = clockInMillis,
    )
}

private fun scheduleWidgetLongSessionReminderIfNeeded(
    context: Context,
    preferences: SharedPreferences,
    profileId: String,
    clockInMillis: Long,
) {
    if (!preferences.getBoolean(widgetProfileKey(profileId, WIDGET_KEY_CLOCK_OUT_REMINDER_ENABLED), false)) return

    val profile = decodeWidgetProfiles(preferences.getString(WIDGET_KEY_WORK_PROFILES, null))
        .firstOrNull { it.id == profileId }
        ?: DEFAULT_WIDGET_PROFILE
    scheduleLongSessionReminders(
        context = context,
        profileId = profileId,
        clockInMillis = clockInMillis,
        expectedDuration = expectedWidgetDurationForRange(
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            preferences = preferences,
            profile = profile,
        ),
    )
}

private fun adjustWidgetTime(context: Context, appWidgetId: Int, offsetMinutes: Long) {
    val preferences = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
    val profileId = widgetProfileId(preferences, appWidgetId)
    val activeKey = widgetProfileKey(profileId, WIDGET_KEY_ACTIVE_CLOCK_IN)
    val offsetMillis = Duration.ofMinutes(offsetMinutes).toMillis()
    val activeClockInMillis = preferences.getLongOrNull(activeKey)

    if (activeClockInMillis != null) {
        val newClockInMillis = activeClockInMillis + offsetMillis
        val nowMillis = Instant.now().toEpochMilli()
        if (newClockInMillis >= nowMillis) return

        preferences.edit()
            .putLong(activeKey, newClockInMillis)
            .remove(widgetProfileKey(profileId, WIDGET_KEY_CLOCK_OUT_REMINDER_SENT_MASK))
            .apply()

        cancelLongSessionReminders(
            context = context,
            profileId = profileId,
            clockInMillis = activeClockInMillis,
        )
        scheduleWidgetLongSessionReminderIfNeeded(context, preferences, profileId, newClockInMillis)
        return
    }

    val sessionsKey = widgetProfileKey(profileId, WIDGET_KEY_COMPLETED_SESSIONS)
    val sessions = decodeWidgetSessions(preferences.getString(sessionsKey, null))
    val latestSession = sessions.maxByOrNull { it.clockOutMillis } ?: return
    val newClockOutMillis = latestSession.clockOutMillis + offsetMillis
    val nowMillis = Instant.now().toEpochMilli()
    if (newClockOutMillis <= latestSession.clockInMillis || newClockOutMillis > nowMillis) return

    val updatedSessions = sessions.map { session ->
        if (session.clockInMillis == latestSession.clockInMillis && session.clockOutMillis == latestSession.clockOutMillis) {
            session.copy(clockOutMillis = newClockOutMillis)
        } else {
            session
        }
    }.sortedBy { it.clockInMillis }

    preferences.edit()
        .putString(sessionsKey, encodeWidgetSessions(updatedSessions))
        .apply()
}

private fun loadWidgetSnapshot(context: Context, appWidgetId: Int): WidgetSnapshot {
    val preferences = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
    val profiles = decodeWidgetProfiles(preferences.getString(WIDGET_KEY_WORK_PROFILES, null))
    val profileId = widgetProfileId(preferences, appWidgetId)
    val profile = profiles.firstOrNull { it.id == profileId } ?: DEFAULT_WIDGET_PROFILE
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)
    val activeClockInMillis = preferences.getLongOrNull(widgetProfileKey(profile.id, WIDGET_KEY_ACTIVE_CLOCK_IN))
    val sessions = decodeWidgetSessions(preferences.getString(widgetProfileKey(profile.id, WIDGET_KEY_COMPLETED_SESSIONS), null))
    val activeSession = activeClockInMillis?.let { WidgetSession(it, Instant.now().toEpochMilli()) }
    val allSessions = sessions + listOfNotNull(activeSession)
    val todayWorkedDuration = allSessions.fold(Duration.ZERO) { total, session ->
        total.plus(session.durationOnDate(today, zoneId))
    }
    val expectedTodayDuration = expectedWidgetDurationForRange(today, today, preferences, profile)
    val remainingDuration = expectedTodayDuration.minus(todayWorkedDuration)
    val progressPercent = if (expectedTodayDuration <= Duration.ZERO) {
        0
    } else {
        ((todayWorkedDuration.toMinutes().toDouble() / expectedTodayDuration.toMinutes().coerceAtLeast(1L)) * 100)
            .toInt()
            .coerceIn(0, 100)
    }
    val overtimeRange = widgetOvertimeRange(
        preferences.getString(widgetProfileKey(profile.id, WIDGET_KEY_OVERTIME_RANGE), null),
    )
    val balanceStartDate = widgetStartDateForOvertimeRange(
        range = overtimeRange,
        allTimeStartDate = maxOf(
            profile.trackingStartDate,
            preferences.getString(widgetProfileKey(profile.id, WIDGET_KEY_OVERTIME_START_DATE), null)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: profile.trackingStartDate,
        ),
        today = today,
    ).coerceAtLeast(profile.trackingStartDate)
    val actualBalanceDuration = actualWidgetDurationForRange(balanceStartDate, today, allSessions, zoneId)
    val expectedBalanceDuration = expectedWidgetDurationForRange(balanceStartDate, today, preferences, profile)
    val startingBalance = if (overtimeRange == WidgetOvertimeRange.ALL_TIME) {
        preferences.getString(widgetProfileKey(profile.id, WIDGET_KEY_STARTING_OVERTIME_BALANCE), null)
            ?.toWidgetSignedDurationOrNull()
            ?: Duration.ZERO
    } else {
        Duration.ZERO
    }
    val balance = startingBalance.plus(actualBalanceDuration.minus(expectedBalanceDuration))

    return WidgetSnapshot(
        profileName = profile.name,
        activeClockInMillis = activeClockInMillis,
        activeDuration = activeSession?.durationInRange(today, today, zoneId) ?: Duration.ZERO,
        activeDurationLabel = activeSession?.durationInRange(today, today, zoneId)?.let(::formatWidgetDurationWithSeconds) ?: "00:00:00",
        todayWorkedDuration = todayWorkedDuration,
        hasCompletedSession = sessions.isNotEmpty(),
        timeLeftLabel = when {
            expectedTodayDuration <= Duration.ZERO -> "No target today"
            remainingDuration.isNegative || remainingDuration == Duration.ZERO -> "${formatWidgetDuration(remainingDuration.abs())} ahead"
            else -> "${formatWidgetDuration(remainingDuration)} left"
        },
        progressPercent = progressPercent,
        overtimeRangeLabel = overtimeRange.label,
        overtimeBalance = balance,
        balanceHint = "${formatWidgetDuration(actualBalanceDuration)} worked",
    )
}

private fun actualWidgetDurationForRange(
    startDate: LocalDate,
    endDate: LocalDate,
    sessions: List<WidgetSession>,
    zoneId: ZoneId,
): Duration {
    if (startDate.isAfter(endDate)) return Duration.ZERO

    return sessions.fold(Duration.ZERO) { total, session ->
        total.plus(session.durationInRange(startDate, endDate, zoneId))
    }
}

private fun expectedWidgetDurationForRange(
    startDate: LocalDate,
    endDate: LocalDate,
    preferences: SharedPreferences,
    profile: WidgetProfile,
): Duration {
    var date = startDate.coerceAtLeast(profile.trackingStartDate)
    if (date.isAfter(endDate)) return Duration.ZERO

    val expectedDaily = widgetDurationInput(
        preferences.getString(widgetProfileKey(profile.id, WIDGET_KEY_EXPECTED_DAILY_HOURS), null),
    )
        ?: widgetDurationInput(preferences.getString(widgetProfileKey(profile.id, WIDGET_KEY_EXPECTED_WEEKLY_HOURS), null))
            ?.dividedBy(DEFAULT_WIDGET_WORK_DAYS.size.toLong())
        ?: DEFAULT_WIDGET_DAILY_DURATION
    val lunchBreak = if (preferences.getBoolean(widgetProfileKey(profile.id, WIDGET_KEY_DEDUCT_UNPAID_LUNCH_BREAK), false)) {
        Duration.ofMinutes(
            preferences.getString(widgetProfileKey(profile.id, WIDGET_KEY_LUNCH_BREAK_MINUTES), null)
                ?.toLongOrNull()
                ?.coerceIn(0L, 240L)
                ?: DEFAULT_WIDGET_LUNCH_MINUTES,
        )
    } else {
        Duration.ZERO
    }
    val dailyTarget = expectedDaily.plus(lunchBreak)
    val workDays = preferences.getString(widgetProfileKey(profile.id, WIDGET_KEY_WORK_DAYS), null)
        ?.let(::decodeWidgetWorkDays)
        ?: DEFAULT_WIDGET_WORK_DAYS
    val absences = decodeWidgetAbsences(preferences.getString(widgetProfileKey(profile.id, WIDGET_KEY_ABSENCES), null))
    var expected = Duration.ZERO

    while (!date.isAfter(endDate)) {
        val coveredAbsence = absences.any { it.date == date && it.coversExpectedHours }
        if (date.dayOfWeek in workDays && !coveredAbsence) {
            expected = expected.plus(dailyTarget)
        }
        date = date.plusDays(1)
    }

    return expected
}

private fun decodeWidgetProfiles(encoded: String?): List<WidgetProfile> {
    return encoded
        ?.lineSequence()
        ?.mapNotNull { line ->
            val parts = line.split("|")
            val id = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val name = parts.getOrNull(1)?.takeIf { it.isNotBlank() } ?: DEFAULT_WIDGET_PROFILE.name
            val trackingStartDate = parts.getOrNull(2)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: LocalDate.now()
            WidgetProfile(id = id, name = name, trackingStartDate = trackingStartDate)
        }
        ?.toList()
        ?.ifEmpty { listOf(DEFAULT_WIDGET_PROFILE) }
        ?: listOf(DEFAULT_WIDGET_PROFILE)
}

private fun decodeWidgetSessions(encoded: String?): List<WidgetSession> {
    return encoded
        ?.lineSequence()
        ?.mapNotNull { line ->
            val parts = line.split(",")
            val clockIn = parts.getOrNull(0)?.toLongOrNull()
            val clockOut = parts.getOrNull(1)?.toLongOrNull()
            if (clockIn != null && clockOut != null && clockOut >= clockIn) {
                WidgetSession(clockIn, clockOut)
            } else {
                null
            }
        }
        ?.toList()
        .orEmpty()
}

private fun encodeWidgetSessions(sessions: List<WidgetSession>): String {
    return sessions.joinToString(separator = "\n") { session ->
        "${session.clockInMillis},${session.clockOutMillis}"
    }
}

private fun decodeWidgetAbsences(encoded: String?): List<WidgetAbsence> {
    return encoded
        ?.lineSequence()
        ?.mapNotNull { line ->
            val parts = line.split("|")
            val date = parts.getOrNull(0)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: return@mapNotNull null
            val type = parts.getOrNull(1).orEmpty()
            WidgetAbsence(
                date = date,
                coversExpectedHours = type != "TIME_OFF",
            )
        }
        ?.toList()
        .orEmpty()
}

private fun decodeWidgetWorkDays(encoded: String): Set<DayOfWeek> {
    return encoded.split(",")
        .mapNotNull { value -> runCatching { DayOfWeek.valueOf(value) }.getOrNull() }
        .toSet()
        .ifEmpty { DEFAULT_WIDGET_WORK_DAYS }
}

private fun widgetOvertimeRange(value: String?): WidgetOvertimeRange {
    return runCatching { WidgetOvertimeRange.valueOf(value.orEmpty()) }.getOrNull()
        ?: WidgetOvertimeRange.ALL_TIME
}

private fun widgetStartDateForOvertimeRange(
    range: WidgetOvertimeRange,
    allTimeStartDate: LocalDate,
    today: LocalDate,
): LocalDate {
    return when (range) {
        WidgetOvertimeRange.TODAY -> today
        WidgetOvertimeRange.ONE_WEEK -> today.minusWeeks(1).plusDays(1)
        WidgetOvertimeRange.FOUR_WEEKS -> today.minusWeeks(4).plusDays(1)
        WidgetOvertimeRange.ONE_MONTH -> today.minusMonths(1).plusDays(1)
        WidgetOvertimeRange.SIX_MONTHS -> today.minusMonths(6).plusDays(1)
        WidgetOvertimeRange.TWELVE_MONTHS -> today.minusMonths(12).plusDays(1)
        WidgetOvertimeRange.ALL_TIME -> allTimeStartDate
    }
}

private fun widgetDurationInput(input: String?): Duration? {
    val normalized = input?.trim()?.lowercase()?.replace(",", ".") ?: return null
    if (normalized.isBlank()) return null
    val minutes = when {
        ":" in normalized -> {
            val parts = normalized.split(":")
            val hours = parts.getOrNull(0)?.toLongOrNull() ?: return null
            val mins = parts.getOrNull(1)?.toLongOrNull() ?: return null
            if (mins !in 0L..59L) return null
            hours * 60 + mins
        }
        " " in normalized -> {
            val parts = normalized.split(Regex("""\s+""")).filter { it.isNotBlank() }
            val hours = parts.getOrNull(0)?.toLongOrNull() ?: return null
            val mins = parts.getOrNull(1)?.toLongOrNull() ?: 0L
            if (parts.size > 2 || mins !in 0L..59L) return null
            hours * 60 + mins
        }
        normalized.endsWith("m") && "h" !in normalized -> normalized.removeSuffix("m").toLongOrNull()
        "h" in normalized || "m" in normalized -> {
            val hours = normalized.substringBefore("h", "0").toLongOrNull() ?: 0L
            val minutes = normalized.substringAfter("h", "").removeSuffix("m").takeIf { it.isNotBlank() }?.toLongOrNull() ?: 0L
            hours * 60 + minutes
        }
        else -> normalized.toDoubleOrNull()?.let { (it * 60).toLong() }
    } ?: return null

    return Duration.ofMinutes(minutes.coerceAtLeast(0L))
}

private fun String.toWidgetSignedDurationOrNull(): Duration? {
    val trimmed = trim()
    if (trimmed.isBlank()) return Duration.ZERO
    val isNegative = trimmed.startsWith("-")
    val unsigned = trimmed.removePrefix("-").removePrefix("+")
    val duration = widgetDurationInput(unsigned) ?: return null
    return if (isNegative) duration.negated() else duration
}

private fun formatWidgetDuration(duration: Duration): String {
    val minutes = duration.toMinutes().coerceAtLeast(0L)
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return "${hours}h ${remainingMinutes}m"
}

private fun formatWidgetDurationWithSeconds(duration: Duration): String {
    val seconds = duration.seconds.coerceAtLeast(0L)
    val hours = seconds / 3_600
    val minutes = (seconds % 3_600) / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, remainingSeconds)
}

private fun formatWidgetSignedDuration(duration: Duration): String {
    return when {
        duration.isNegative -> "-${formatWidgetDuration(duration.abs())}"
        duration == Duration.ZERO -> "0h 0m"
        else -> "+${formatWidgetDuration(duration)}"
    }
}

private fun activeWidgetProfileId(preferences: SharedPreferences): String {
    return preferences.getString(WIDGET_KEY_ACTIVE_PROFILE_ID, DEFAULT_WIDGET_PROFILE_ID)
        ?: DEFAULT_WIDGET_PROFILE_ID
}

private fun widgetProfileId(preferences: SharedPreferences, appWidgetId: Int): String {
    return preferences.getString(widgetSelectionKey(appWidgetId), null)
        ?: activeWidgetProfileId(preferences)
}

private fun saveWidgetProfileSelection(
    context: Context,
    appWidgetId: Int,
    profileId: String,
) {
    context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(widgetSelectionKey(appWidgetId), profileId)
        .apply()
}

private fun removeWidgetProfileSelections(context: Context, appWidgetIds: IntArray) {
    val editor = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE).edit()
    appWidgetIds.forEach { appWidgetId ->
        editor.remove(widgetSelectionKey(appWidgetId))
    }
    editor.apply()
}

private fun widgetSelectionKey(appWidgetId: Int): String {
    return "${WIDGET_KEY_SELECTED_PROFILE_PREFIX}_$appWidgetId"
}

private fun widgetProfileKey(profileId: String, key: String): String {
    return "profile_${profileId}_$key"
}

private fun widgetActionPendingIntent(context: Context, action: String, appWidgetId: Int): PendingIntent {
    val intent = Intent(context, TimeClockTodayWidgetProvider::class.java).apply {
        this.action = action
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }
    return PendingIntent.getBroadcast(
        context,
        appWidgetId * 10 + widgetRequestCodeForAction(action),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

private fun widgetRequestCodeForAction(action: String): Int {
    return when (action) {
        ACTION_WIDGET_CLOCK_IN -> WIDGET_CLOCK_IN_REQUEST_CODE
        ACTION_WIDGET_CLOCK_OUT -> WIDGET_CLOCK_OUT_REQUEST_CODE
        ACTION_WIDGET_ADJUST_BACK -> WIDGET_ADJUST_BACK_REQUEST_CODE
        ACTION_WIDGET_ADJUST_FORWARD -> WIDGET_ADJUST_FORWARD_REQUEST_CODE
        else -> WIDGET_OPEN_APP_REQUEST_CODE_TODAY
    }
}

private fun openAppPendingIntent(context: Context, requestCode: Int): PendingIntent {
    return PendingIntent.getActivity(
        context,
        requestCode,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

private fun SharedPreferences.getLongOrNull(key: String): Long? {
    return if (contains(key)) getLong(key, 0L) else null
}

private data class WidgetSnapshot(
    val profileName: String,
    val activeClockInMillis: Long?,
    val activeDuration: Duration,
    val activeDurationLabel: String,
    val todayWorkedDuration: Duration,
    val hasCompletedSession: Boolean,
    val timeLeftLabel: String,
    val progressPercent: Int,
    val overtimeRangeLabel: String,
    val overtimeBalance: Duration,
    val balanceHint: String,
)

private data class WidgetProfile(
    val id: String,
    val name: String,
    val trackingStartDate: LocalDate,
)

private data class WidgetSession(
    val clockInMillis: Long,
    val clockOutMillis: Long,
) {
    fun durationOnDate(date: LocalDate, zoneId: ZoneId): Duration {
        val dayStart = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val dayEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val overlapStart = maxOf(clockInMillis, dayStart)
        val overlapEnd = minOf(clockOutMillis, dayEnd)
        return if (overlapEnd > overlapStart) {
            Duration.ofMillis(overlapEnd - overlapStart)
        } else {
            Duration.ZERO
        }
    }

    fun durationInRange(startDate: LocalDate, endDate: LocalDate, zoneId: ZoneId): Duration {
        val rangeStart = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val rangeEnd = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val overlapStart = maxOf(clockInMillis, rangeStart)
        val overlapEnd = minOf(clockOutMillis, rangeEnd)
        return if (overlapEnd > overlapStart) {
            Duration.ofMillis(overlapEnd - overlapStart)
        } else {
            Duration.ZERO
        }
    }
}

private data class WidgetAbsence(
    val date: LocalDate,
    val coversExpectedHours: Boolean,
)

private enum class WidgetOvertimeRange(val label: String) {
    TODAY("Today"),
    ONE_WEEK("1 week"),
    FOUR_WEEKS("4 weeks"),
    ONE_MONTH("1 month"),
    SIX_MONTHS("6 months"),
    TWELVE_MONTHS("12 months"),
    ALL_TIME("All time"),
}

private const val ACTION_WIDGET_CLOCK_IN = "com.annelysa.timeclock.action.WIDGET_CLOCK_IN"
private const val ACTION_WIDGET_CLOCK_OUT = "com.annelysa.timeclock.action.WIDGET_CLOCK_OUT"
private const val ACTION_WIDGET_ADJUST_BACK = "com.annelysa.timeclock.action.WIDGET_ADJUST_BACK"
private const val ACTION_WIDGET_ADJUST_FORWARD = "com.annelysa.timeclock.action.WIDGET_ADJUST_FORWARD"
private const val WIDGET_PREFS_NAME = "time_clock_preferences"
private const val WIDGET_KEY_WORK_PROFILES = "work_profiles"
private const val WIDGET_KEY_ACTIVE_PROFILE_ID = "active_profile_id"
private const val WIDGET_KEY_ACTIVE_CLOCK_IN = "active_clock_in"
private const val WIDGET_KEY_COMPLETED_SESSIONS = "completed_sessions"
private const val WIDGET_KEY_ABSENCES = "absences"
private const val WIDGET_KEY_EXPECTED_DAILY_HOURS = "expected_daily_hours"
private const val WIDGET_KEY_EXPECTED_WEEKLY_HOURS = "expected_weekly_hours"
private const val WIDGET_KEY_WORK_DAYS = "work_days"
private const val WIDGET_KEY_DEDUCT_UNPAID_LUNCH_BREAK = "deduct_unpaid_lunch_break"
private const val WIDGET_KEY_LUNCH_BREAK_MINUTES = "lunch_break_minutes"
private const val WIDGET_KEY_OVERTIME_START_DATE = "overtime_start_date"
private const val WIDGET_KEY_STARTING_OVERTIME_BALANCE = "starting_overtime_balance"
private const val WIDGET_KEY_OVERTIME_RANGE = "overtime_range"
private const val WIDGET_KEY_CLOCK_OUT_REMINDER_ENABLED = "clock_out_reminder_enabled"
private const val WIDGET_KEY_CLOCK_OUT_REMINDER_SENT_MASK = "clock_out_reminder_sent_mask"
private const val WIDGET_KEY_SELECTED_PROFILE_PREFIX = "widget_selected_profile"
private const val DEFAULT_WIDGET_PROFILE_ID = "default_profile"
private const val DEFAULT_WIDGET_PROFILE_NAME = "My workplace"
private const val DEFAULT_WIDGET_LUNCH_MINUTES = 30L
private const val WIDGET_CLOCK_IN_REQUEST_CODE = 2101
private const val WIDGET_CLOCK_OUT_REQUEST_CODE = 2102
private const val WIDGET_OPEN_APP_REQUEST_CODE_TODAY = 2103
private const val WIDGET_OPEN_APP_REQUEST_CODE_BALANCE = 2104
private const val WIDGET_ADJUST_BACK_REQUEST_CODE = 2105
private const val WIDGET_ADJUST_FORWARD_REQUEST_CODE = 2106
private const val WIDGET_QUICK_ADJUST_MINUTES = 5L
private val DEFAULT_WIDGET_DAILY_DURATION: Duration = Duration.ofHours(7).plusMinutes(30)
private val DEFAULT_WIDGET_WORK_DAYS = setOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
)
private val DEFAULT_WIDGET_PROFILE = WidgetProfile(
    id = DEFAULT_WIDGET_PROFILE_ID,
    name = DEFAULT_WIDGET_PROFILE_NAME,
    trackingStartDate = LocalDate.now(),
)
