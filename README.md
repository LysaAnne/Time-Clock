# Time Clock Android App

This is a Kotlin Android app for tracking work clock-in and clock-out sessions.

## Current Version

Features 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, and 13 are implemented. Feature 6 was intentionally skipped.

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
- See reports for today, this week, this month, half year, and this year
- Compare actual hours against expected hours in reports
- See ahead/missing/on-target balance for each report period
- Track running overtime balance
- Choose overtime balance range: today, 1 week, 4 weeks, 1 month, 6 months, 12 months, or all time
- Set an overtime balance start date
- Set an optional starting overtime balance
- See a 7-day hours bar chart
- See current weekly progress against expected hours
- See this month's weekly overtime trend
- See a color-coded month view for missing, on-target, and overtime days
- Create work profiles for different workplaces or jobs
- Switch between work profiles
- Delete workplaces you no longer need
- Set a tracking start date for each workplace
- Keep sessions, settings, reports, charts, and overtime balances separate per profile
- Prevent expected hours from counting before the active workplace's tracking start date
- Navigate with Today, History, Insights, and Settings tabs
- Keep the active workplace visible at the top on every tab
- Switch workplaces from a dropdown menu
- Keep clocking actions focused on the Today tab
- Move manual entries and past sessions into History
- Move reports, charts, and full overtime details into Insights
- Move work profiles and work-hour settings into Settings
- Enable profile-specific clock-in reminders near your work start time
- Enable profile-specific clock-out reminders
- Get a clock-out reminder when your active session reaches the expected daily work time
- Get follow-up overtime reminders at 1, 2, and 5 hours over expected daily work time
- Add full-day vacation/holiday, sick day, and no-work absence entries
- Add vacation/holiday ranges with start and end dates
- Add sickness for one day or a date range
- Add time-off hours that use overtime balance
- Keep absence entries separate per work profile
- Prevent absence days from counting as missing hours in reports, charts, history, and overtime balance

Expected time inputs accept formats like `7:30`, `7h30m`, `7 30`, `450m`, or decimal hours like `7.5`.
Starting overtime balance accepts formats like `2:30` or `-1:15` and is applied to the all-time overtime balance.
Manual sessions use date format `YYYY-MM-DD` and time format `HH:mm`.

## How To Open

1. Install Android Studio.
2. Open this folder in Android Studio.
3. Let Android Studio sync the Gradle project.
4. Run the app on an Android emulator or connected Android phone.

## Next Feature

The next recommended feature is:

**Home screen widgets.**

That will add quick clocking, daily progress, and overtime balance directly from the Android home screen.
