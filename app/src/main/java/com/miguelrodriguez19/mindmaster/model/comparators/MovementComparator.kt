package com.miguelrodriguez19.mindmaster.model.comparators

import com.miguelrodriguez19.mindmaster.model.structures.dto.expenses.Movement
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.defaultDateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

class MovementComparator : Comparator<Movement> {
    override fun compare(obj1: Movement, obj2: Movement): Int {
        val dateFormat = SimpleDateFormat(defaultDateFormat, Locale.getDefault())
        val parsedDate1 = dateFormat.parse(obj1.date)
        val parsedDate2 = dateFormat.parse(obj2.date)
        val result = parsedDate1.compareTo(parsedDate2)
        return if (result != 0){
            result
        }else{
            obj1.concept.compareTo(obj2.concept)
        }
    }
}