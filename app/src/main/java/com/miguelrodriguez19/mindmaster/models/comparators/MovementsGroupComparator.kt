package com.miguelrodriguez19.mindmaster.models.comparators

import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

class MovementsGroupComparator : Comparator<MonthMovementsResponse> {
    override fun compare(e1: MonthMovementsResponse, e2: MonthMovementsResponse): Int {
        val dateFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())
        val parsedDate1 = dateFormat.parse("01-${e1.date}")
        val parsedDate2 = dateFormat.parse("01-${e2.date}")
        return parsedDate1.compareTo(parsedDate2)
    }
}