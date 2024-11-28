package com.miguelrodriguez19.mindmaster.model.structures.dto.expenses

import java.io.Serializable

data class MonthMovementsResponse(
    val date: String,
    val incomeList: List<Movement>,
    val expensesList: List<Movement>,
) : Serializable {

}
