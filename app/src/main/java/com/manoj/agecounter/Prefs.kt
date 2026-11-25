package com.manoj.agecounter

import android.content.Context

/** Settings stored only on this phone. */
object Prefs {
    private const val FILE = "age_counter"
    private const val BIRTH = "birth_millis"
    private const val PROGRESS = "show_progress"

    fun birth(context: Context): Long? =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getLong(BIRTH, -1L).takeIf { it > 0 }

    fun setBirth(context: Context, millis: Long) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putLong(BIRTH, millis).apply()

    fun showProgress(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(PROGRESS, true)

    fun setShowProgress(context: Context, show: Boolean) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(PROGRESS, show).apply()
}
