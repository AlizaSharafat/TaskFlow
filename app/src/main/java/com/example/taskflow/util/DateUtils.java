package com.example.taskflow.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Due dates are stored as the first millisecond of the due day, so two dates
 * can be compared without worrying about the time of day.
 */
public final class DateUtils {

    private static final SimpleDateFormat DISPLAY =
            new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

    private DateUtils() {
        // Utility class, never instantiated.
    }

    /** Midnight at the start of today, on the device's own clock. */
    public static long todayStart() {
        return startOfDay(System.currentTimeMillis());
    }

    /** Midnight at the start of whatever day the given time falls on. */
    public static long startOfDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /** Midnight a number of days away from today. Negative goes backwards. */
    public static long daysFromToday(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(todayStart());
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTimeInMillis();
    }

    public static boolean isToday(long millis) {
        return startOfDay(millis) == todayStart();
    }

    public static String format(long millis) {
        return DISPLAY.format(new Date(millis));
    }

    public static int yearOf(long millis) {
        return fieldOf(millis, Calendar.YEAR);
    }

    public static int monthOf(long millis) {
        return fieldOf(millis, Calendar.MONTH);
    }

    public static int dayOf(long millis) {
        return fieldOf(millis, Calendar.DAY_OF_MONTH);
    }

    private static int fieldOf(long millis, int field) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(field);
    }

    /** Builds the start of a day from the values a DatePickerDialog hands back. */
    public static long fromPicker(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
