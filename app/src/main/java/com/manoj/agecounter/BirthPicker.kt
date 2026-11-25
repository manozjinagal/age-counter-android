package com.manoj.agecounter

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.Calendar

/** Date picker, then time picker; saves the result and refreshes every widget. */
object BirthPicker {
    fun show(activity: Activity, onPicked: () -> Unit) {
        val cal = Calendar.getInstance().apply {
            Prefs.birth(activity)?.let { timeInMillis = it } ?: add(Calendar.YEAR, -25)
        }
        val dateDialog = DatePickerDialog(activity, { _, y, mo, d ->
            TimePickerDialog(activity, { _, hour, minute ->
                cal.set(y, mo, d, hour, minute, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Prefs.setBirth(activity, cal.timeInMillis)
                AgeWidgetProvider.updateAll(activity)
                onPicked()
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                android.text.format.DateFormat.is24HourFormat(activity)).apply { setTitle(R.string.pick_time) }.show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dateDialog.datePicker.maxDate = System.currentTimeMillis()
        dateDialog.setTitle(R.string.pick_date)
        dateDialog.show()
    }
}
