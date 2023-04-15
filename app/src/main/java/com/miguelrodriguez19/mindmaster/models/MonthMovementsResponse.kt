package com.miguelrodriguez19.mindmaster.models

import java.io.Serializable

data class MonthMovementsResponse(
    val codMonthMovement: Int,
    val date: String,
    val movementsList: List<Movement>,
) : Serializable {
    data class Movement(
        val codMovement: Int,
        val date: String,
        val title: String,
        val amount: Float,
        val type: String
    ) : Serializable
}

enum class Sender {
    CALENDAR, SCHEDULE
}