package com.miguelrodriguez19.mindmaster.models

import java.io.Serializable

data class MonthMovementsResponse(
    val codMonthMovement: String,
    val date: String,
    val movementsList: List<Movement>,
) : Serializable {
    data class Movement(
        val codMovement: String,
        val date: String,
        val concept: String,
        val amount: Double,
        val description: String?,
        val type: Type
    ) : Serializable

    enum class Type {
        INCOME, EXPENSE
    }
}
