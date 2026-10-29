package com.pentacart.app.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object WeekDates {
    private val dayFormat = DateTimeFormatter.ofPattern("EEEE d", Locale.ITALIAN)
    private val dayMonthFormat = DateTimeFormatter.ofPattern("d MMMM", Locale.ITALIAN)

    fun mondayOf(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun daysOf(monday: LocalDate): List<LocalDate> =
        (0L..6L).map { monday.plusDays(it) }

    /** "Lunedì 2". */
    fun dayLabel(date: LocalDate): String =
        dayFormat.format(date).replaceFirstChar { it.titlecase(Locale.ITALIAN) }

    /** "2 – 8 novembre", oppure "27 ottobre – 2 novembre" se la settimana è a cavallo di due mesi. */
    fun weekLabel(monday: LocalDate): String {
        val sunday = monday.plusDays(6)
        val start = if (monday.month == sunday.month) monday.dayOfMonth.toString() else dayMonthFormat.format(monday)
        return "$start – ${dayMonthFormat.format(sunday)}"
    }
}
