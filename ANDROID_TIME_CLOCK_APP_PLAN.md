# Android Time Clock App Plan

## Recommended Programming Language

Use **Kotlin**.

Kotlin is the best default choice for a modern Android app because it is officially supported by Google, works beautifully with Android Studio, and is less verbose than Java. It is also a good fit for long-term app growth because modern Android libraries are designed with Kotlin in mind.

Recommended stack:

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Local database:** Room
- **Architecture:** MVVM
- **Date/time handling:** kotlinx-datetime or Java Time APIs
- **Charts:** Vico, MPAndroidChart, or Compose-based charts
- **Backup/sync later:** Google Drive, Firebase, or custom cloud sync

## Core App Goal

The app should help you clock in and out of work, track worked time over different periods, and compare actual hours against expected work hours.

The app should answer questions like:

- How many hours did I work today?
- Am I ahead or behind this week?
- How much overtime have I built up this month?
- Do I need to stay longer today to meet my target?
- How much have I worked over the past half year or year?

## Suggested First Version

The first version should be small and reliable:

1. Clock in
2. Clock out
3. Show today's worked time
4. Store work sessions locally on the phone
5. Show a simple history list

Once that works well, we can add overtime calculations, reports, editing, charts, and reminders.

## Feature Roadmap

### 1. Clock In / Clock Out

Basic start and stop tracking.

Status: **Built in version 0.1.0**

Consider:

- One large clock in / clock out button
- Show current clocked-in status
- Show active session duration while clocked in
- Prevent clocking in twice without clocking out
- Prevent clocking out if no active session exists
- Store exact start and end timestamps

Implemented:

- A Compose screen with clock in and clock out actions
- A live active-session timer
- Disabled invalid actions based on current clock state
- Active clock-in timestamp saved locally
- Last completed session saved locally with exact clock-in and clock-out timestamps
- Last completed session shown on the main screen

### 2. Daily Summary

Show how much time was worked today.

Status: **Built in version 0.2.0**

Consider:

- Total worked time for the current day
- Number of work sessions today
- First clock-in time
- Last clock-out time
- Difference between worked hours and expected daily hours
- Message such as "You need 1h 15m more today" or "You are 35m ahead"

Implemented:

- Total worked time for today
- Today's completed sessions are saved locally
- Active session time is included in today's total while clocked in
- Sessions that cross midnight are counted only for the part that belongs to today
- Number of today's sessions
- First clock-in time today
- Last clock-out time today, or "Active" while clocked in

### 3. Work Hours Settings

Let the user define their expected work schedule.

Status: **Built in version 0.3.0**

Consider:

- Expected hours per day
- Expected hours per week
- Work days, such as Monday to Friday
- Optional different hours for different weekdays
- Lunch break rules
- Unpaid lunch breaks
- Time zone handling

Implemented:

- Expected hours per workday
- Expected hours per week
- Automatic daily-hours calculation from weekly hours and selected workdays
- Time inputs with hours and minutes, such as `7:30`, `7h30m`, `7 30`, or `450m`
- Expected weekly hours calculated from selected workdays
- Workday selection for Monday through Sunday
- Collapsed settings menu that opens with an expand/collapse button
- Settings are saved locally
- Today's summary compares worked time with expected time
- Ahead/behind message, such as "You need 1h 15m more today"
- Unpaid lunch deduction
- Configurable lunch break length

Not implemented yet:

- Different expected hours per weekday
- More advanced break categories beyond lunch

### 4. History

Let the user review previous work sessions.

Status: **Built in version 0.4.0**

Consider:

- List of days
- Expand a day to see individual sessions
- Search or filter by date
- Show total hours per day
- Show overtime or missing time per day

Implemented:

- History card on the main screen
- Completed sessions grouped by day
- Newest days first
- Expand/collapse each day
- Individual clock-in and clock-out times for each session
- Total worked time per day
- Ahead/missing/on-target status per day based on current work-hour settings

### 5. Manual Editing

People forget to clock in or out, so editing is important.

Status: **Built in version 0.5.0**

Consider:

- Add a missed session manually
- Edit clock-in time
- Edit clock-out time
- Delete incorrect sessions
- Add a note explaining manual changes
- Mark edited entries visually

Implemented:

- Add a missed session manually
- Edit clock-in time
- Edit clock-out time
- Delete incorrect sessions
- Manual entry validation for date and time format
- History totals update after adding, editing, or deleting sessions

Not implemented yet:

- Notes explaining manual changes
- Visual edited-entry marker

### 6. Break Tracking

Breaks can be handled automatically or manually.

Status: **Intentionally skipped**

Consider:

- Manual break start / stop
- Automatic unpaid lunch deduction
- Configurable break length
- Paid break support
- Include or exclude breaks from total work time

Decision:

- Separate break tracking is not needed right now.
- The app already supports clocking out for breaks and clocking back in afterward.
- The app also supports unpaid lunch as a setting that adds break time to the required clocked target.

### 7. Reports

Summaries over longer periods.

Status: **Built in version 0.7.0**

Consider:

- Daily report
- Weekly report
- Monthly report
- Half-year report
- Yearly report
- Custom date range
- Total expected hours
- Total actual hours
- Overtime balance
- Missing hours balance

Implemented:

- Reports card on the main screen
- Today report
- Current week report
- Current month report
- Current half-year report
- Current year report
- Actual hours
- Expected hours based on selected workdays and unpaid lunch setting
- Ahead/missing/on-target balance

### 8. Overtime Balance

Track whether the user is ahead or behind.

Status: **Built in version 0.8.0**

Consider:

- Running overtime balance
- Daily overtime
- Weekly overtime
- Monthly overtime
- Carry overtime across periods
- Reset balance from a chosen date
- Optional starting balance if the user already has overtime hours

Implemented:

- Overtime balance card on the main screen
- Running total balance since a chosen start date
- Selectable balance ranges: today, 1 week, 4 weeks, 1 month, 6 months, 12 months, and all time
- Actual hours since the balance start date
- Expected hours since the balance start date
- Period balance
- Optional starting overtime balance
- Positive and negative balance display
- Balance range, start date, and starting balance saved locally
- Starting balance applied to the all-time overtime balance

### 9. Charts and Visuals

Make the data easier to understand.

Status: **Built in version 0.9.0**

Consider:

- Bar chart of hours per day
- Weekly progress chart
- Monthly overtime trend
- Calendar view with color-coded days
- Green for enough hours, red for missing hours, blue for overtime

Implemented:

- Charts card on the main screen
- Last 7 days hours bar chart
- Weekly progress bar comparing actual and expected hours
- Monthly overtime trend grouped by week
- Current month color-coded day grid
- Red for missing hours, green for on-target days, and blue for overtime days

### 10. Work Profiles

Support multiple workplaces or jobs, each with its own tracking rules and overtime balance.

Status: **Built in version 1.0.0**

Goal:

- Let the user create a profile for each workplace
- Let the user switch between active work profiles
- Keep time sessions, settings, reports, charts, and overtime balances separated by profile
- Prevent overtime balance from counting days before the user started at that workplace

Consider:

- Workplace name, such as company, department, client, or job title
- Tracking start date for the workplace
- Active profile selector near the top of the main screen
- Create, rename, switch, and delete work profiles
- Prevent deleting a profile without confirmation
- Choose one default profile for app startup
- Separate clock-in status per profile
- Clear warning if the user is clocked in on one profile and switches to another
- Profile-specific expected daily hours
- Profile-specific expected weekly hours
- Profile-specific workdays
- Profile-specific unpaid lunch setting
- Profile-specific lunch break length
- Profile-specific overtime balance start date
- Profile-specific starting overtime balance
- Profile-specific reports, history, charts, and overtime range
- Manual sessions assigned to the currently selected profile
- Future export grouped by profile

Recommended first version:

- Create a default profile automatically, such as "My workplace"
- Add profile name and employment/tracking start date
- Move all current settings into the active profile
- Filter history, reports, charts, and overtime balance by active profile
- Make overtime calculations start no earlier than the profile tracking start date
- Add a simple profile switcher

Implemented:

- Default work profile created automatically
- Existing single-workplace data migrates into the default profile
- Active profile selector near the top of the main screen
- Edit the active workplace name
- Edit the active workplace tracking start date
- Create additional work profiles
- Switch between work profiles
- Delete work profiles while keeping at least one workplace
- Profile-specific active clock-in state
- Profile-specific completed sessions
- Profile-specific expected daily hours
- Profile-specific expected weekly hours
- Profile-specific workdays
- Profile-specific unpaid lunch setting
- Profile-specific lunch break length
- Profile-specific overtime start date
- Profile-specific starting overtime balance
- Profile-specific overtime balance range
- History, reports, charts, and overtime balance update when switching profiles
- Actual and expected calculations start no earlier than the active profile tracking start date

Important behavior:

- If the tracking start date is `2026-06-01`, the app should not count expected hours before `2026-06-01`.
- If a profile has no sessions before its start date, the overtime balance should not become negative just because the person was not employed there yet.
- All existing settings should belong to the selected profile, not the whole app.
- When switching profiles, the screen should immediately update totals, reports, charts, history, and overtime balance for that workplace.

Not needed in the first version:

- Cloud sync between profiles
- Company logos
- Team or manager sharing
- Different pay rates

### 11. UI Simplification

Keep all functionality, but make the app feel calmer and easier to use.

Status: **Built in version 1.1.0**

Goal:

- Reduce the overloaded feeling on the main screen
- Keep clock in and clock out as the fastest action
- Move detailed reviewing and configuration into clearer areas
- Make the app feel like a simple daily tool, not one long dashboard

Recommended direction:

- Add bottom navigation with four main tabs
- Keep the first tab focused on today and the active work session
- Move history, reports, charts, overtime details, profiles, and settings into their own areas

Suggested tabs:

- **Today:** active work profile, clock in / clock out button, active timer, today summary, small overtime preview
- **History:** completed sessions, daily history, manual add/edit/delete
- **Insights:** reports, charts, overtime balance, range selectors
- **Settings:** work profiles, work hours, workdays, lunch settings, overtime start date, starting balance

Consider:

- Show only the active workplace name on the Today screen, such as `Workplace: Cafe Job`
- Hide profile editing behind the Settings tab
- Show a compact overtime preview on Today instead of the full overtime card
- Keep full overtime range controls under Insights
- Keep settings grouped clearly inside the Settings tab
- Avoid putting every card on one scrolling screen
- Make manual entry available from History instead of always showing it on Today
- Keep button labels short and predictable
- Make the first screen useful within a few seconds of opening the app

Alternative lighter version:

- Keep the single-screen layout
- Collapse Work Profile, Settings, Manual Entry, Reports, Charts, Overtime, and History by default
- Leave only Clock In / Clock Out, timer, and Today open by default

Recommended first version:

- Add bottom navigation tabs
- Put the current clocking workflow into Today
- Move manual entry and session editing into History
- Move Reports, Charts, and Overtime Balance into Insights
- Move Work Profiles and Work Hours Settings into Settings

Implemented:

- Bottom navigation with Today, History, Insights, and Settings tabs
- Active workplace dropdown shown at the top on every tab
- Today tab focused on active workplace, clock in / clock out, timer, today summary, overtime preview, and last session
- History tab for manual entry and completed session history
- Insights tab for reports, charts, and full overtime balance controls
- Settings tab for work profiles and work-hour settings
- Compact work profile display on Today
- Compact overtime preview on Today
- Work-hour settings are always visible inside Settings instead of hidden behind a second collapse control
- All existing functionality kept, but split into clearer areas

### 12. Reminders and Notifications

Help the user remember to clock in or out.

Status: **Built in version 1.2.0**

Consider:

- Reminder to clock in near work start time
- Reminder to clock out near work end time
- Alert if still clocked in after many hours
- Location-based reminder when arriving at or leaving work
- Notification showing active session duration

Implemented:

- Android notification permission added
- Reminder notification channel created
- Profile-specific clock-in reminder setting
- Scheduled reminder to clock in near the selected work start time
- Profile-specific clock-out reminder setting
- Long-session alert calculated from the active profile's expected daily work time
- Clock-out alert scheduled for expected clock-out time after clocking in
- Follow-up overtime alerts at 1, 2, and 5 hours over expected daily work time
- Notification opens the app when tapped
- Clock-in reminders and long-session reminders use Android alarms

Not implemented yet:

- Location-based reminders
- Persistent notification while clocked in

### 13. Vacation, Sick Days, and Absence

Handle days where the user should not clock work time, but the day should still be counted correctly.

Status: **Built in version 1.3.0**

Goal:

- Mark days as vacation/holiday, sick leave, absence, or no-work days
- Prevent expected hours from becoming missing time on approved absence days
- Keep reports, charts, history, and overtime balance fair when the user is away from work

Consider:

- Add vacation days
- Add sick days
- Treat vacation and holiday as the same absence type
- Add vacation/holiday date ranges with start and end dates
- Let sickness be either one day or a date range
- Add unpaid absence days
- Add time off using overtime balance
- Mark days as not expected work days
- Choose whether an absence counts as paid expected hours or removes expected hours
- Add half-day absence
- Add notes to absence entries
- Show absence labels in History and calendar visuals
- Adjust expected hours automatically in reports and overtime balance
- Keep absence entries profile-specific

Recommended first version:

- Add absence entries for the active work profile
- Support vacation/holiday, sick day, no-work day, and time off
- Support full-day entries first
- Remove expected hours for no-work/unpaid absence days
- Count vacation/holiday and sick days as covered days so they do not create missing hours
- Show absence entries in History
- Include absence adjustments in reports, charts, and overtime balance

Implemented:

- Profile-specific absence entries
- Full-day vacation/holiday, sick day, and no-work entries
- Vacation/holiday ranges with start and end dates
- Sick-day entries can be one day or a date range
- Time-off entries with hours/minutes, such as `2:00`
- Optional note on absence entries
- Absence entry form in History
- Absence entries shown in History, including days with no work sessions
- Delete absence entries from History
- Expected hours skipped on absence days
- Time-off entries do not remove expected hours, so they reduce overtime balance naturally when fewer hours are worked
- Reports, charts, history, today summary, long-session reminders, and overtime balance respect absence days

Not implemented yet:

- Half-day absence
- Different paid/unpaid absence rules
- Recurring public holidays

### 14. Workplace Type, Pay, and Earnings

Let each work profile describe how that workplace should calculate time, balance, and money.

Status: **Built in version 1.4.0**

Goal:

- Support different kinds of work without forcing every workplace into the same overtime model
- Track earned money when the user wants pay or salary calculations
- Keep profiles flexible for regular employment, consulting, and self-employment

Consider:

- Workplace type setting per profile
- `Fixed hours + fixed pay` for regular salaried jobs with expected hours and overtime balance
- `Hourly paid` for consultant, shift, or hourly jobs where earnings are based on worked hours
- `Time tracking only` for own business, study, projects, or unpaid work where the user only wants totals
- Salary amount for fixed-pay profiles
- Hourly rate for hourly profiles
- Pay period, such as monthly, weekly, or every 14 days
- Currency setting
- Optional overtime rate, evening/weekend rate, or holiday rate
- Optional unpaid break behavior per workplace type
- Earnings summaries for today, week, month, year, and all time
- Separate earnings from overtime balance so the UI stays understandable

Recommended first version:

- Add a workplace type dropdown in Settings
- Keep existing profiles as `Fixed hours + fixed pay` by default
- Add optional salary input for fixed-pay profiles
- Add hourly rate input for hourly-paid profiles
- Hide overtime-balance pressure for `Time tracking only` profiles
- Show an estimated earned amount in Insights when pay information is available

Nice later ideas:

- Multiple pay rates for the same workplace
- Weekend, evening, or holiday multipliers
- Tax estimate fields
- Invoice-ready consultant report
- Project/client tags for self-employed tracking

Implemented:

- Workplace type selector for each work profile
- Existing profiles default to `Fixed hours + fixed pay`
- Optional monthly salary for fixed-pay profiles
- Optional hourly rate for hourly-paid profiles
- Currency setting per workplace, defaulting to `DKK`
- `Time tracking only` mode for profiles without pay tracking
- Earnings card in Insights
- Hourly-paid earnings use actual clocked hours
- Fixed-pay earnings estimate salary value from monthly salary and expected weekly hours
- Pay settings are saved separately for each workplace

### 15. Home Screen Widgets

Let the user see and control work time without opening the full app.

Status: **Built in version 1.5.0**

Goal:

- Make clocking in and out faster from the Android home screen
- Show the most important work-time status at a glance
- Support the active work profile, so the widget matches the workplace currently being tracked

Consider:

- Clock in / clock out widget with one large action button
- Daily progress bar showing how much of today's expected time is completed
- Remaining-time text, such as `2h 15m left today`
- Overtime balance widget showing the selected balance range
- Compact balance widget showing only `+2h 10m` or `-45m`
- Active-session widget showing live clocked-in duration
- Workplace label on every widget so the user knows which profile it controls
- Widget tap opens the relevant app tab, such as Today or Insights
- Widget refresh after clocking in, clocking out, editing sessions, or switching work profiles
- Different widget sizes, such as small, medium, and wide
- Clear disabled/error state if no work profile exists

Recommended first version:

- A medium Today widget with active workplace, clock in / clock out button, progress bar, and time left today
- A small Balance widget with active workplace and overtime balance
- Tapping the Today widget opens the Today tab
- Tapping the Balance widget opens the Insights tab

Nice later widget ideas:

- Weekly progress widget
- Last session widget
- Quick manual-entry shortcut widget
- Multi-workplace widget showing balances for several profiles
- Lock-screen or glance-style status if supported by the Android version

Implemented:

- Today home-screen widget with active workplace name
- Clock in / clock out button from the widget
- Workplace picker when adding a widget
- Each widget stays tied to its selected workplace even when the active workplace changes in the app
- Simplified Today widget focused on one large clock in / clock out button
- Red clock-out state while clocked in
- Running active-session timer on the Today widget
- Small worked-today summary on the Today widget
- Balance home-screen widget with active workplace name
- Balance widget follows the selected overtime range
- Widget taps open the app
- Widgets refresh after app clocking actions, manual edits, absence edits, profile changes, and work-hour setting changes
- Clock-in from the widget schedules long-session reminders when enabled
- Clock-out from the widget cancels long-session reminders

### 16. Export

Allow the user to take their data elsewhere.

Consider:

- Export to CSV
- Export to Excel
- Export monthly report as PDF
- Share report by email
- Include notes and edited entries
- Include hours, overtime balance, absence entries, time off, workplace name, and earnings when available

### 17. Backup and Sync

Protect the data if the phone is lost.

Consider:

- Local-only mode first
- Manual backup file
- Google Drive backup
- Firebase sync
- Multi-device support
- Restore from backup

### 18. Data Privacy

Important because work-time data is personal.

Consider:

- Keep data local by default
- Clear privacy policy if publishing to Google Play
- Option to delete all data
- Optional app lock
- Avoid unnecessary tracking

### 19. Polish and Convenience

Small features that make the app feel good to use.

Consider:

- Dark mode
- Quick settings tile
- Wear OS support later
- Nice empty states
- Clear error messages
- Fast startup

## Suggested Build Order

Build the app in this order:

1. Create the Android project
2. Build the clock in / clock out screen
3. Save sessions locally
4. Show today's total
5. Add history view
6. Add work-hours settings
7. Calculate overtime and missing hours
8. Add editing
9. Add weekly and monthly reports
10. Add charts
11. Add work profiles
12. Simplify the UI with tabs
13. Add reminders
14. Add vacation, sick days, and absence
15. Add workplace type, pay, and earnings
16. Add home screen widgets
17. Add export
18. Add backup

## First Feature To Build

Start with:

**A single screen with a clock in / clock out button and today's total worked time.**

This gives the app its core value immediately and creates the foundation for every later feature.
