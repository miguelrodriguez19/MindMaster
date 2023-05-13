package com.miguelrodriguez19.mindmaster.models.comparators

import com.miguelrodriguez19.mindmaster.models.EventsResponse
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

class DateComparator : Comparator<EventsResponse> {
    override fun compare(e1: EventsResponse, e2: EventsResponse): Int {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val parsedDate1 = dateFormat.parse(e1.date)
        val parsedDate2 = dateFormat.parse(e2.date)
        return parsedDate1.compareTo(parsedDate2)
    }
}