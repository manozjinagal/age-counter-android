# Age Counter for Android

A home screen and lock screen widget that shows your age counting up live: years, months, days, and hours, minutes and seconds that tick every second. Made for the Galaxy S25 Ultra (Android 12 or later).

## Install

1. Open the latest release on your phone: **Releases → Age Counter (newest) → AgeCounter.apk**.
2. Tap the downloaded file. If asked, allow your browser or Files app to **install unknown apps**, then tap **Install**.
3. Open **Age Counter** and set your date and time of birth.

New builds install over the old one; your settings are kept.

## Add it to the home screen

Tap **Add widget to home screen** in the app, or long-press the home screen → **Widgets** → **Age Counter**. Resize it to switch layouts:

| Size | Shows |
| --- | --- |
| 1 row (compact) | 28y 05m 09d and a ticking time |
| Square (small) | Big years, months and days, ticking time |
| 4 × 2 (wide) | YY : MM : DD : HH:MM:SS, birthday progress |
| 4 × 3 or bigger (large) | Hero years, months, days, big ticking time, birthday progress |

## Styles and transparency

When you add a widget (or long-press it and tap **Settings**), pick one of six styles and how see-through the background is, with a live preview:

- **Rolling odometer:** bold white numbers
- **Mono ticker:** typewriter digits, with the ticking time in orange
- **Stacked labels:** thin numbers with thin dividers
- **Hero years:** your years shown big
- **Life progress:** always shows the bar to your next birthday, with a percentage
- **Dot-matrix LED:** glowing amber dot font ([Doto](https://github.com/oliverlalan/Doto), SIL Open Font License)

Slide **Background** to 0% for a fully transparent widget. Every widget keeps its own style, so you can use different ones on the home and lock screens.

## Add it to the lock screen

Samsung's lock screen only shows Samsung's own widgets by itself. Use LockStar:

1. Install **Good Lock** from the Galaxy Store.
2. In Good Lock, install **LockStar** and open it.
3. Turn LockStar on, tap the lock screen preview, choose **Add widget** and pick **Age Counter**.

## Good to know

- Android widgets can't redraw fast enough for milliseconds. The seconds tick live; the app itself shows milliseconds.
- Years, months and days update at the start of each new day of your age (the time of day you were born).
- Your date of birth is saved only on your phone.

## Building

Every push builds the APK with GitHub Actions (`.github/workflows/build.yml`) and publishes it as a release.
