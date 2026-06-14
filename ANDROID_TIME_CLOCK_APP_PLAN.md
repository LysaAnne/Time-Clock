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

Status: **Planned**

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
- Keep advanced settings collapsed or grouped clearly
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

### 12. Reminders and Notifications

Help the user remember to clock in or out.

Consider:

- Reminder to clock in near work start time
- Reminder to clock out near work end time
- Alert if still clocked in after many hours
- Location-based reminder when arriving at or leaving work
- Notification showing active session duration

### 13. Location Features

Optional, but useful for work tracking.

Consider:

- Save clock-in location
- Save clock-out location
- Warn if clocking in away from workplace
- Automatic prompts based on location
- Privacy-friendly setting to disable location entirely

### 14. Export

Allow the user to take their data elsewhere.

Consider:

- Export to CSV
- Export to Excel
- Export monthly report as PDF
- Share report by email
- Include notes and edited entries

### 15. Backup and Sync

Protect the data if the phone is lost.

Consider:

- Local-only mode first
- Manual backup file
- Google Drive backup
- Firebase sync
- Multi-device support
- Restore from backup

### 16. Calendar Integration

Optional integration with the user's schedule.

Consider:

- Import planned work shifts from calendar
- Compare calendar shifts against actual hours
- Add work sessions to calendar
- Show holidays and days off

### 17. Vacation, Sick Days, and Absence

Useful if the app becomes a complete work-time tracker.

Consider:

- Add vacation days
- Add sick days
- Add public holidays
- Mark days as not expected work days
- Adjust expected hours automatically

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
- Home screen widget
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
14. Add export
15. Add backup

## First Feature To Build

Start with:

**A single screen with a clock in / clock out button and today's total worked time.**

This gives the app its core value immediately and creates the foundation for every later feature.
