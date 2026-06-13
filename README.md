# Time Clock Android App

This is a Kotlin Android app for tracking work clock-in and clock-out sessions.

## Current Version

Features 1, 2, 3, 4, and 5 are implemented:

- Clock in
- Clock out
- Show current clocked-in status
- Show a live active-session timer
- Prevent invalid clock actions by disabling buttons
- Save the active clock-in timestamp locally
- Save and show the last completed session
- Save completed sessions locally
- Show today's total worked time
- Show today's session count
- Show today's first clock-in and latest clock-out status
- Set expected hours per workday
- Set expected hours per week
- Automatically calculate daily hours from weekly hours and selected workdays
- Select workdays from Monday through Sunday
- Show expected weekly hours
- Show whether you need more time today or are ahead
- Collapse settings behind an expand/collapse button
- Deduct unpaid lunch from credited work time
- Set lunch break length in minutes
- Review completed sessions in History
- Expand a day to see individual sessions
- See total worked time per day
- See whether a day is ahead, missing time, or on target
- Add missed sessions manually
- Edit existing history sessions
- Delete incorrect history sessions

Expected time inputs accept formats like `7:30`, `7h30m`, `7 30`, `450m`, or decimal hours like `7.5`.
Manual sessions use date format `YYYY-MM-DD` and time format `HH:mm`.

## How To Open

1. Install Android Studio.
2. Open this folder in Android Studio.
3. Let Android Studio sync the Gradle project.
4. Run the app on an Android emulator or connected Android phone.

## Next Feature

The next recommended feature is:

**Break tracking.**

That will make breaks first-class sessions instead of only using the current unpaid lunch setting.
