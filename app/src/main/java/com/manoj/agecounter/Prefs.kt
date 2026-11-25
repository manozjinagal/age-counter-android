package com.manoj.agecounter

import android.content.Context

/** Settings stored only on this phone. Style and background are saved per widget. */
object Prefs {
    private const val FILE = "age_counter"
    private const val BIRTH = "birth_millis"
    private const val PROGRESS = "show_progress"
    private const val LAST_STYLE = "last_style"
    private const val LAST_OPACITY = "last_opacity"
    const val DEFAULT_OPACITY = 85

    private fun sp(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun birth(c: Context): Long? = sp(c).getLong(BIRTH, -1L).takeIf { it > 0 }
    fun setBirth(c: Context, millis: Long) = sp(c).edit().putLong(BIRTH, millis).apply()

    fun showProgress(c: Context): Boolean = sp(c).getBoolean(PROGRESS, true)
    fun setShowProgress(c: Context, show: Boolean) = sp(c).edit().putBoolean(PROGRESS, show).apply()

    /** A widget without its own setting uses the last style you picked. */
    fun style(c: Context, widgetId: Int): WidgetStyle =
        WidgetStyle.of(sp(c).getString("style_$widgetId", null) ?: sp(c).getString(LAST_STYLE, null))

    /** Background opacity, 0 (fully transparent) to 100 (solid). */
    fun opacity(c: Context, widgetId: Int): Int =
        sp(c).getInt("opacity_$widgetId", sp(c).getInt(LAST_OPACITY, DEFAULT_OPACITY))

    fun setWidget(c: Context, widgetId: Int, style: WidgetStyle, opacity: Int) = sp(c).edit()
        .putString("style_$widgetId", style.key).putInt("opacity_$widgetId", opacity)
        .putString(LAST_STYLE, style.key).putInt(LAST_OPACITY, opacity).apply()

    fun forget(c: Context, widgetId: Int) = sp(c).edit().remove("style_$widgetId").remove("opacity_$widgetId").apply()
}
