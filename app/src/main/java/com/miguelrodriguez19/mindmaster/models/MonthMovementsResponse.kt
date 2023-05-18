package com.miguelrodriguez19.mindmaster.models

import java.io.Serializable

data class MonthMovementsResponse(
    val date: String,
    val incomeList: List<Movement>,
    val expensesList: List<Movement>,
) : Serializable {
    data class Movement(
        val uid: String,
        val date: String,
        val concept: String,
        val amount: Float,
        val description: String?,
        val type: Type
    ) : Serializable {
        constructor() : this(
            "", "", "", 0.0F, "", Type.INCOME
        )

        constructor(
            date: String, concept: String, amount: Float,
            description: String?, type: Type
        ) : this("", date, concept, amount, description, type)

        constructor(uid: String, m: Movement) : this(
            uid, m.date, m.concept, m.amount, m.description, m.type
        )
    }

    enum class Type {
        INCOME, EXPENSE
    }
}
