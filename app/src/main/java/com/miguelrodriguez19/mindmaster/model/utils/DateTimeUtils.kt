package com.miguelrodriguez19.mindmaster.model.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateTimeUtils {
    const val defaultDateFormat = "dd-MM-yyyy"
    const val defaultTimeFormat = "HH:mm"
    const val defaultDateTimeFormat = "dd-MM-yyyy HH:mm"
    fun compareDateTimes(date1Str: String, date2Str: String): Int {
        val format = SimpleDateFormat(defaultDateTimeFormat, Locale.getDefault())
        val date1 = format.parse(date1Str)
        val date2 = format.parse(date2Str)
        if (date1 == null || date2 == null)
            throw IllegalArgumentException("Invalid date-time format, al least one date does not match \"$defaultDateTimeFormat\".")

        return date1.compareTo(date2)
    }

    fun compareDates(date1Str: String, date2Str: String): Int {
        val format = SimpleDateFormat(defaultDateFormat, Locale.getDefault())
        val date1 = format.parse(date1Str)
        val date2 = format.parse(date2Str)
        if (date1 == null || date2 == null)
            throw IllegalArgumentException("Invalid date format, al least one date does not match \"$defaultDateFormat\".")

        return date1.compareTo(date2)
    }

    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return String.format("%02d-%02d-%04d", day, month, year)
    }

    fun getDateFromDatetimeStr(dateTime: String): String {
        val dateFormat = SimpleDateFormat(defaultDateFormat, Locale.getDefault())
        val date = dateFormat.parse(dateTime)!!
        return dateFormat.format(date)
    }

    fun getTimeFromDatetimeStr(dateTime: String): String {
        val dateFormat = SimpleDateFormat(defaultDateFormat, Locale.getDefault())
        val date = dateFormat.parse(dateTime)!!
        return dateFormat.format(date)
    }

    fun getMonthYearOf(currentDate: String): String {
        val parser = SimpleDateFormat(defaultDateFormat, Locale.getDefault())
        val formatter = SimpleDateFormat("MM-yyyy", Locale.getDefault())
        val date = parser.parse(currentDate)
        return date?.let {
            formatter.format(it)
        }
            ?: throw IllegalArgumentException("Invalid date format, does not match \"$defaultDateFormat\".")
    }

}