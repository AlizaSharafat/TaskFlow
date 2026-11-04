package com.example.taskflow.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Due dates are stored as the first millisecond of the due day, so two dates
 * can be compared without worrying about the time of day.
 */
object DateUtils {

    private val display = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    /** Midnight at the start of today, on the device's own clock. */
    fun todayStart(): Long = startOfDay(System.currentTimeMillis())

    /** Midnight at the start of whatever day the given time falls on. */
    fun startOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /** Midnight of a day a number of days away from today. Negative goes backwards. */
    fun daysFromToday(days: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = todayStart()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.timeInMillis
    }

    fun isToday(millis: Long): Boolean = startOfDay(millis) == todayStart()

    fun format(millis: Long): String = display.format(Date(millis))

    fun yearOf(millis: Long): Int = fieldOf(millis, Calendar.YEAR)

    fun monthOf(millis: Long): Int = fieldOf(millis, Calendar.MONTH)

    fun dayOf(millis: Long): Int = fieldOf(millis, Calendar.DAY_OF_MONTH)

    private fun fieldOf(millis: Long, field: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        return calendar.get(field)
    }

    /** Builds the start of a day from the values a DatePickerDialog hands back. */
    fun fromPicker(year: Int, month: Int, dayOfMonth: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
