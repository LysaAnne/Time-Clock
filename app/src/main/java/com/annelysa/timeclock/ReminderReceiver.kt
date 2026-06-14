package com.annelysa.timeclock

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.Duration

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val profileId = intent.getStringExtra(EXTRA_PROFILE_ID) ?: return
        val preferences = context.getSharedPreferences(REMINDER_PREFS_NAME, Context.MODE_PRIVATE)
        val activeProfileId = preferences.getString(REMINDER_KEY_ACTIVE_PROFILE_ID, DEFAULT_REMINDER_PROFILE_ID)
            ?: DEFAULT_REMINDER_PROFILE_ID
        val reminderType = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: REMINDER_TYPE_CLOCK_IN

        if (reminderType == REMINDER_TYPE_LONG_SESSION) {
            val clockInMillis = intent.getLongExtra(EXTRA_CLOCK_IN_MILLIS, 0L)
            val overtimeMinutes = intent.getLongExtra(EXTRA_OVERTIME_MINUTES, 0L)
            if (profileId == activeProfileId && shouldShowLongSessionReminder(preferences, profileId, clockInMillis)) {
                showLongSessionReminderNotification(
                    context = context,
                    preferences = preferences,
                    profileId = profileId,
                    overtimeDuration = Duration.ofMinutes(overtimeMinutes),
                )
            }
            return
        }

        if (profileId == activeProfileId && shouldShowClockInReminder(context, preferences, profileId)) {
            showClockInReminderNotification(context, preferences, profileId)
        }

        val reminderTime = preferences.getString(
            reminderProfileKey(profileId, REMINDER_KEY_CLOCK_IN_REMINDER_TIME),
            DEFAULT_CLOCK_IN_REMINDER_TIME,
        )?.let { runCatching { LocalTime.parse(it, REMINDER_TIME_FORMATTER) }.getOrNull() }
            ?: LocalTime.of(8, 0)
        val workDays = preferences.getString(reminderProfileKey(profileId, REMINDER_KEY_WORK_DAYS), null)
            ?.let(::decodeReminderWorkDays)
            ?: DEFAULT_REMINDER_WORK_DAYS

        scheduleClockInReminder(context, profileId, reminderTime, workDays)
    }
}

fun scheduleLongSessionReminders(
    context: Context,
    profileId: String,
    clockInMillis: Long,
    expectedDuration: Duration,
) {
    if (expectedDuration <= Duration.ZERO) return

    val alarmManager = context.getSystemService(AlarmManager::class.java)
    LONG_SESSION_OVERTIME_MINUTES.forEachIndexed { index, overtimeMinutes ->
        val triggerAtMillis = clockInMillis + expectedDuration.toMillis() + Duration.ofMinutes(overtimeMinutes).toMillis()
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            longSessionReminderPendingIntent(
                context = context,
                profileId = profileId,
                clockInMillis = clockInMillis,
                overtimeMinutes = overtimeMinutes,
                index = index,
            ),
        )
    }
}

fun cancelLongSessionReminders(
    context: Context,
    profileId: String,
    clockInMillis: Long,
) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    LONG_SESSION_OVERTIME_MINUTES.forEachIndexed { index, overtimeMinutes ->
        alarmManager.cancel(
            longSessionReminderPendingIntent(
                context = context,
                profileId = profileId,
                clockInMillis = clockInMillis,
                overtimeMinutes = overtimeMinutes,
                index = index,
            ),
        )
    }
}

fun scheduleClockInReminder(
    context: Context,
    profileId: String,
    reminderTime: LocalTime,
    workDays: Set<DayOfWeek>,
) {
    if (workDays.isEmpty()) {
        cancelClockInReminder(context)
        return
    }

    val alarmManager = context.getSystemService(AlarmManager::class.java)
    val triggerAtMillis = nextReminderMillis(reminderTime, workDays)
    alarmManager.set(
        AlarmManager.RTC_WAKEUP,
        triggerAtMillis,
        clockInReminderPendingIntent(context, profileId),
    )
}

fun cancelClockInReminder(context: Context) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    alarmManager.cancel(clockInReminderPendingIntent(context, DEFAULT_REMINDER_PROFILE_ID))
}

private fun shouldShowClockInReminder(
    context: Context,
    preferences: android.content.SharedPreferences,
    profileId: String,
): Boolean {
    if (!canPostReminderNotifications(context)) return false
    if (preferences.getLong(reminderProfileKey(profileId, REMINDER_KEY_ACTIVE_CLOCK_IN), 0L) > 0L) return false

    val workDays = preferences.getString(reminderProfileKey(profileId, REMINDER_KEY_WORK_DAYS), null)
        ?.let(::decodeReminderWorkDays)
        ?: DEFAULT_REMINDER_WORK_DAYS

    return LocalDate.now().dayOfWeek in workDays
}

private fun showClockInReminderNotification(
    context: Context,
    preferences: android.content.SharedPreferences,
    profileId: String,
) {
    val profileName = preferences.getString(REMINDER_KEY_WORK_PROFILES, null)
        ?.lineSequence()
        ?.mapNotNull { line ->
            val parts = line.split("|")
            val id = parts.getOrNull(0)
            val name = parts.getOrNull(1)
            if (id == profileId) name else null
        }
        ?.firstOrNull()
        ?: "Work"
    val pendingIntent = PendingIntent.getActivity(
        context,
        CLOCK_IN_REMINDER_OPEN_APP_REQUEST_CODE,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val notification = Notification.Builder(context, REMINDER_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Time to clock in")
        .setContentText(profileName)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    context.getSystemService(NotificationManager::class.java)
        .notify(CLOCK_IN_REMINDER_NOTIFICATION_ID, notification)
}

private fun shouldShowLongSessionReminder(
    preferences: android.content.SharedPreferences,
    profileId: String,
    clockInMillis: Long,
): Boolean {
    if (clockInMillis <= 0L) return false
    val activeClockInMillis = preferences.getLong(reminderProfileKey(profileId, REMINDER_KEY_ACTIVE_CLOCK_IN), 0L)
    return activeClockInMillis == clockInMillis
}

private fun showLongSessionReminderNotification(
    context: Context,
    preferences: android.content.SharedPreferences,
    profileId: String,
    overtimeDuration: Duration,
) {
    if (!canPostReminderNotifications(context)) return

    val profileName = profileName(preferences, profileId)
    val pendingIntent = PendingIntent.getActivity(
        context,
        LONG_SESSION_OPEN_APP_REQUEST_CODE,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val title = if (overtimeDuration == Duration.ZERO) {
        "Time to clock out"
    } else {
        "${formatReminderDuration(overtimeDuration)} overtime"
    }
    val notification = Notification.Builder(context, REMINDER_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(profileName)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    context.getSystemService(NotificationManager::class.java)
        .notify(LONG_SESSION_NOTIFICATION_ID + overtimeDuration.toMinutes().toInt(), notification)
}

private fun profileName(
    preferences: android.content.SharedPreferences,
    profileId: String,
): String {
    return preferences.getString(REMINDER_KEY_WORK_PROFILES, null)
        ?.lineSequence()
        ?.mapNotNull { line ->
            val parts = line.split("|")
            val id = parts.getOrNull(0)
            val name = parts.getOrNull(1)
            if (id == profileId) name else null
        }
        ?.firstOrNull()
        ?: "Work"
}

private fun nextReminderMillis(
    reminderTime: LocalTime,
    workDays: Set<DayOfWeek>,
): Long {
    val zoneId = ZoneId.systemDefault()
    val now = java.time.ZonedDateTime.now(zoneId)
    var candidateDate = now.toLocalDate()

    repeat(8) {
        val candidate = candidateDate.atTime(reminderTime).atZone(zoneId)
        if (candidate.toInstant().toEpochMilli() > now.toInstant().toEpochMilli() && candidateDate.dayOfWeek in workDays) {
            return candidate.toInstant().toEpochMilli()
        }
        candidateDate = candidateDate.plusDays(1)
    }

    return candidateDate.atTime(reminderTime).atZone(zoneId).toInstant().toEpochMilli()
}

private fun clockInReminderPendingIntent(
    context: Context,
    profileId: String,
): PendingIntent {
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra(EXTRA_PROFILE_ID, profileId)
        putExtra(EXTRA_REMINDER_TYPE, REMINDER_TYPE_CLOCK_IN)
    }
    return PendingIntent.getBroadcast(
        context,
        CLOCK_IN_REMINDER_REQUEST_CODE,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

private fun longSessionReminderPendingIntent(
    context: Context,
    profileId: String,
    clockInMillis: Long,
    overtimeMinutes: Long,
    index: Int,
): PendingIntent {
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra(EXTRA_PROFILE_ID, profileId)
        putExtra(EXTRA_REMINDER_TYPE, REMINDER_TYPE_LONG_SESSION)
        putExtra(EXTRA_CLOCK_IN_MILLIS, clockInMillis)
        putExtra(EXTRA_OVERTIME_MINUTES, overtimeMinutes)
    }
    return PendingIntent.getBroadcast(
        context,
        LONG_SESSION_REQUEST_CODE_BASE + index,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

private fun canPostReminderNotifications(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

private fun decodeReminderWorkDays(encoded: String): Set<DayOfWeek> {
    val days = encoded.split(",")
        .mapNotNull { value -> runCatching { DayOfWeek.valueOf(value) }.getOrNull() }
        .toSet()

    return days.ifEmpty { DEFAULT_REMINDER_WORK_DAYS }
}

private fun reminderProfileKey(profileId: String, key: String): String {
    return "profile_${profileId}_$key"
}

private fun formatReminderDuration(duration: Duration): String {
    val minutes = duration.toMinutes().coerceAtLeast(0L)
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (remainingMinutes == 0L) {
        "${hours}h"
    } else {
        "${hours}h ${remainingMinutes}m"
    }
}

private const val EXTRA_PROFILE_ID = "profile_id"
private const val EXTRA_REMINDER_TYPE = "reminder_type"
private const val EXTRA_CLOCK_IN_MILLIS = "clock_in_millis"
private const val EXTRA_OVERTIME_MINUTES = "overtime_minutes"
private const val REMINDER_TYPE_CLOCK_IN = "clock_in"
private const val REMINDER_TYPE_LONG_SESSION = "long_session"
private const val REMINDER_PREFS_NAME = "time_clock_preferences"
private const val REMINDER_NOTIFICATION_CHANNEL_ID = "time_clock_reminders"
private const val REMINDER_KEY_ACTIVE_PROFILE_ID = "active_profile_id"
private const val REMINDER_KEY_WORK_PROFILES = "work_profiles"
private const val REMINDER_KEY_ACTIVE_CLOCK_IN = "active_clock_in"
private const val REMINDER_KEY_WORK_DAYS = "work_days"
private const val REMINDER_KEY_CLOCK_IN_REMINDER_TIME = "clock_in_reminder_time"
private const val DEFAULT_REMINDER_PROFILE_ID = "default_profile"
private const val DEFAULT_CLOCK_IN_REMINDER_TIME = "08:00"
private const val CLOCK_IN_REMINDER_REQUEST_CODE = 1301
private const val CLOCK_IN_REMINDER_OPEN_APP_REQUEST_CODE = 1302
private const val CLOCK_IN_REMINDER_NOTIFICATION_ID = 1303
private const val LONG_SESSION_REQUEST_CODE_BASE = 1400
private const val LONG_SESSION_OPEN_APP_REQUEST_CODE = 1404
private const val LONG_SESSION_NOTIFICATION_ID = 1405
private val REMINDER_TIME_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("H:mm")
private val LONG_SESSION_OVERTIME_MINUTES = listOf(0L, 60L, 120L, 300L)
private val DEFAULT_REMINDER_WORK_DAYS = setOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
)
