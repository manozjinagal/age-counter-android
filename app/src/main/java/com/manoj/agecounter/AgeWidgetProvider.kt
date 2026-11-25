package com.manoj.agecounter

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
        ids.forEach { manager.updateAppWidget(it, build(context, it)) }
        scheduleNext(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        manager.updateAppWidget(id, build(context, id))
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { Prefs.forget(context, it) }
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
            ids.forEach { manager.updateAppWidget(it, build(context, it)) }
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
            context.getSystemService(AlarmManager::class.java).setWindow(AlarmManager.RTC, next, 60_000L, pending)
        }

        /** One layout per size; the launcher picks the one that fits. */
        fun build(context: Context, widgetId: Int): RemoteViews {
            val style = Prefs.style(context, widgetId)
            val opacity = Prefs.opacity(context, widgetId)
            return RemoteViews(
                mapOf(
                    SizeF(100f, 40f) to views(context, style, opacity, "compact"),
                    SizeF(100f, 100f) to views(context, style, opacity, "small"),
                    SizeF(230f, 90f) to views(context, style, opacity, "wide"),
                    SizeF(230f, 200f) to views(context, style, opacity, "large"),
                )
            )
        }

        /** A single size, used for the live preview on the settings screen. */
        fun views(context: Context, style: WidgetStyle, opacity: Int, size: String): RemoteViews {
            val v = RemoteViews(context.packageName, style.layout(size))
            v.setInt(R.id.bg, "setImageAlpha", (opacity.coerceIn(0, 100) * 255) / 100)
            v.setOnClickPendingIntent(
                R.id.root,
                PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
            )

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
            when (size) {
                "compact" -> {
                    v.setTextViewText(R.id.years, "${p.years}y")
                    v.setTextViewText(R.id.months, "${AgeMath.two(p.months)}m")
                    v.setTextViewText(R.id.days, "${AgeMath.two(p.days)}d")
                }
                "small" -> {
                    v.setTextViewText(R.id.years, p.years.toString())
                    v.setTextViewText(R.id.months, "${AgeMath.two(p.months)} mo")
                    v.setTextViewText(R.id.days, "${AgeMath.two(p.days)} d")
                }
                else -> {
                    val heroYears = size == "large" || style == WidgetStyle.HERO
                    v.setTextViewText(R.id.years, if (heroYears) p.years.toString() else AgeMath.two(p.years))
                    v.setTextViewText(R.id.months, AgeMath.two(p.months))
                    v.setTextViewText(R.id.days, AgeMath.two(p.days))
                }
            }

            // Live hours:minutes:seconds since the current day of your age began
            v.setChronometer(R.id.clock, SystemClock.elapsedRealtime() - (now - p.dayStartMillis), null, true)

            val isProgressStyle = style == WidgetStyle.PROGRESS
            val showBar = size != "compact" &&
                (isProgressStyle || (Prefs.showProgress(context) && (size == "wide" || size == "large")))
            if (showBar) {
                val (prev, next) = AgeMath.birthdays(birth, now)
                val fraction = ((now - prev).toDouble() / (next - prev)).coerceIn(0.0, 1.0)
                val days = AgeMath.daysUntil(next, now)
                v.setProgressBar(R.id.progress, 1000, (fraction * 1000).toInt(), false)
                val toGo = if (days == 0L) "Happy birthday! 🎂"
                    else "$days ${if (days == 1L) "day" else "days"} to your ${AgeMath.ordinal(p.years + 1)} birthday"
                v.setTextViewText(
                    R.id.caption,
                    if (isProgressStyle && days != 0L) String.format("%.1f%% · %s", fraction * 100, toGo) else toGo
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
