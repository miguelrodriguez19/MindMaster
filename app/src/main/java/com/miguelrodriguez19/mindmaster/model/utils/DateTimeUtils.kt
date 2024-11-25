package com.miguelrodriguez19.mindmaster.model.utils

import com.miguelrodriguez19.mindmaster.model.structures.exceptions.ExceptionHolder
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {
    const val DEFAULT_DATE_FORMAT = "dd-MM-yyyy"
    const val DEFAULT_TIME_FORMAT = "HH:mm"
    const val DEFAULT_DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm"
    const val DEFAULT_DATE_MONTH_YEAR_FORMAT = "MM-yyyy"

    fun compareDateTimes(date1Str: String, date2Str: String): Int {
        val format = SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT, Locale.getDefault())
        val date1 = format.parse(date1Str)
        val date2 = format.parse(date2Str)
        if (date1 == null || date2 == null)
            ExceptionHolder.illegalArgument("Invalid date-time format, al least one date does not match \"$DEFAULT_DATE_TIME_FORMAT\": date1=$date1 || date2=$date2")

        return date1.compareTo(date2)
    }

    fun compareDates(date1Str: String, date2Str: String): Int {
        val format = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault())
        val date1 = format.parse(date1Str)
        val date2 = format.parse(date2Str)
        if (date1 == null || date2 == null)
            ExceptionHolder.illegalArgument("Invalid date format, al least one date does not match \"$DEFAULT_DATE_FORMAT\": date1=$date1 || date2=$date2")

        return date1.compareTo(date2)
    }

    fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)
        return LocalDate.now().format(formatter)
    }

    fun getDateFromDatetimeStr(dateTime: String): String {
        val dateFormat = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault())
        val date = dateFormat.parse(dateTime)!!
        return dateFormat.format(date)
    }

    fun getTimeFromDatetimeStr(dateTime: String): String {
        val dateFormat = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault())
        val date = dateFormat.parse(dateTime)!!
        return dateFormat.format(date)
    }

    fun getMonthYearOf(currentDate: String): String {
        val parser = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault())
        val formatter = SimpleDateFormat(DEFAULT_DATE_MONTH_YEAR_FORMAT, Locale.getDefault())
        val date = parser.parse(currentDate)
        return date?.let {
            formatter.format(it)
        } ?: ExceptionHolder.illegalArgument("Invalid date format, does not match \"$DEFAULT_DATE_FORMAT\".")
    }

    fun getLocalDateFromString(date: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)
        return LocalDate.parse(date, formatter)
    }

    fun formatEpochMillis(epochMillis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("$DEFAULT_DATE_TIME_FORMAT:ss")
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }
}