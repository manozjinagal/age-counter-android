package com.manoj.agecounter

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/** Your age in calendar units, plus when the current "day" of your age began and when the next one begins. */
data class AgeParts(
    val years: Long,
    val months: Long,
    val days: Long,
    val dayStartMillis: Long,
    val nextDayStartMillis: Long,
)

object AgeMath {
    private fun zoned(millis: Long): ZonedDateTime =
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())

    /**
     * Calendar-accurate age: whole months first (so 28/30/31-day months tick over on the right date;
     * if you were born on the 31st, shorter months count as done on their last day), then whole days.
     */
    fun parts(birthMillis: Long, nowMillis: Long): AgeParts {
        val birth = zoned(birthMillis)
        val now = zoned(nowMillis)
        var months = ChronoUnit.MONTHS.between(birth, now)
        var monthStart = birth.plusMonths(months)
        if (monthStart.isAfter(now)) {
            months -= 1
            monthStart = birth.plusMonths(months)
        }
        val nextMonthStart = birth.plusMonths(months + 1)

        var days = ChronoUnit.DAYS.between(monthStart, now)
        var dayStart = monthStart.plusDays(days)
        if (dayStart.isAfter(now)) {
            days -= 1
            dayStart = monthStart.plusDays(days)
        }
        val plusOne = dayStart.plusDays(1)
        val nextDayStart = if (plusOne.isAfter(nextMonthStart)) nextMonthStart else plusOne

        return AgeParts(
            years = maxOf(0, months / 12),
            months = maxOf(0, months % 12),
            days = maxOf(0, days),
            dayStartMillis = dayStart.toInstant().toEpochMilli(),
            nextDayStartMillis = nextDayStart.toInstant().toEpochMilli(),
        )
    }

    /** The last birthday and the next one, as epoch milliseconds. */
    fun birthdays(birthMillis: Long, nowMillis: Long): Pair<Long, Long> {
        val birth = zoned(birthMillis)
        val now = zoned(nowMillis)
        val years = ChronoUnit.YEARS.between(birth, now)
        return birth.plusYears(years).toInstant().toEpochMilli() to
            birth.plusYears(years + 1).toInstant().toEpochMilli()
    }

    fun daysUntil(targetMillis: Long, nowMillis: Long): Long {
        val a = zoned(nowMillis).toLocalDate()
        val b = zoned(targetMillis).toLocalDate()
        return ChronoUnit.DAYS.between(a, b)
    }

    fun ordinal(n: Long): String {
        val suffix = if (n % 100 in 11..13) "th" else when (n % 10) { 1L -> "st"; 2L -> "nd"; 3L -> "rd"; else -> "th" }
        return "$n$suffix"
    }

    fun two(n: Long) = n.toString().padStart(2, '0')
}
