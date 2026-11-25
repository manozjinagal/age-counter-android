package com.manoj.agecounter

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews

/**
 * The home screen / lock screen widget.
 * Years, months and days are redrawn at the start of each new day of your age.
 * Hours, minutes and seconds use Android's built-in Chronometer, which ticks every second by itself.
 */
class AgeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { manager.updateAppWidget(it, build(context)) }
        scheduleNext(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: android.os.Bundle) {
        manager.updateAppWidget(id, build(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED, ACTION_TICK_OVER -> updateAll(context)
        }
    }

    companion object {
        private const val ACTION_TICK_OVER = "com.manoj.agecounter.TICK_OVER"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, AgeWidgetProvider::class.java))
            ids.forEach { manager.updateAppWidget(it, build(context)) }
            scheduleNext(context)
        }

        /** Wake up when the next day of your age begins, so years/months/days roll over. */
        private fun scheduleNext(context: Context) {
            val birth = Prefs.birth(context) ?: return
            val next = AgeMath.parts(birth, System.currentTimeMillis()).nextDayStartMillis
            val intent = Intent(context, AgeWidgetProvider::class.java).setAction(ACTION_TICK_OVER)
            val pending = PendingIntent.getBroadcast(
                context, 1, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarms = context.getSystemService(AlarmManager::class.java)
            alarms.setWindow(AlarmManager.RTC, next, 60_000L, pending)
        }

        private enum class Kind { COMPACT, SMALL, WIDE, LARGE }

        /** One layout per size; the launcher picks the one that fits. */
        fun build(context: Context): RemoteViews = RemoteViews(
            mapOf(
                SizeF(100f, 40f) to views(context, Kind.COMPACT, R.layout.widget_compact),
                SizeF(100f, 100f) to views(context, Kind.SMALL, R.layout.widget_small),
                SizeF(230f, 90f) to views(context, Kind.WIDE, R.layout.widget_wide),
                SizeF(230f, 200f) to views(context, Kind.LARGE, R.layout.widget_large),
            )
        )

        private fun views(context: Context, kind: Kind, layout: Int): RemoteViews {
            val v = RemoteViews(context.packageName, layout)
            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
            )
            v.setOnClickPendingIntent(R.id.root, open)

            val birth = Prefs.birth(context)
            val now = System.currentTimeMillis()
            if (birth == null || birth >= now) {
                v.setViewVisibility(R.id.content, View.GONE)
                v.setViewVisibility(R.id.setup, View.VISIBLE)
                return v
            }
            v.setViewVisibility(R.id.content, View.VISIBLE)
            v.setViewVisibility(R.id.setup, View.GONE)

            val p = AgeMath.parts(birth, now)
            when (kind) {
                Kind.COMPACT -> {
                    v.setTextViewText(R.id.years, "${p.years}y")
                    v.setTextViewText(R.id.months, "${AgeMath.two(p.months)}m")
                    v.setTextViewText(R.id.days, "${AgeMath.two(p.days)}d")
                }
                Kind.SMALL -> {
                    v.setTextViewText(R.id.years, p.years.toString())
                    v.setTextViewText(R.id.months, "${AgeMath.two(p.months)} mo")
                    v.setTextViewText(R.id.days, "${AgeMath.two(p.days)} d")
                }
                else -> {
                    v.setTextViewText(R.id.years, if (kind == Kind.LARGE) p.years.toString() else AgeMath.two(p.years))
                    v.setTextViewText(R.id.months, AgeMath.two(p.months))
                    v.setTextViewText(R.id.days, AgeMath.two(p.days))
                }
            }

            // Live hours:minutes:seconds since the current day of your age began
            val base = SystemClock.elapsedRealtime() - (now - p.dayStartMillis)
            v.setChronometer(R.id.clock, base, null, true)

            val showBar = Prefs.showProgress(context) && (kind == Kind.WIDE || kind == Kind.LARGE)
            if (showBar) {
                val (prev, next) = AgeMath.birthdays(birth, now)
                val permille = (((now - prev).toDouble() / (next - prev)) * 1000).toInt().coerceIn(0, 1000)
                val days = AgeMath.daysUntil(next, now)
                v.setProgressBar(R.id.progress, 1000, permille, false)
                v.setTextViewText(
                    R.id.caption,
                    if (days == 0L) "Happy birthday! 🎂"
                    else "$days ${if (days == 1L) "day" else "days"} to your ${AgeMath.ordinal(p.years + 1)} birthday"
                )
                v.setViewVisibility(R.id.progress, View.VISIBLE)
                v.setViewVisibility(R.id.caption, View.VISIBLE)
            } else {
                v.setViewVisibility(R.id.progress, View.GONE)
                v.setViewVisibility(R.id.caption, View.GONE)
            }
            return v
        }
    }
}
