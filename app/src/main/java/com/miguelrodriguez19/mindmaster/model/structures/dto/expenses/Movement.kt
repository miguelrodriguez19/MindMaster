package com.miguelrodriguez19.mindmaster.model.structures.dto.expenses

import com.miguelrodriguez19.mindmaster.model.structures.enums.MovementType
import java.io.Serializable

data class Movement(
    val uid: String,
    val date: String,
    val concept: String,
    val amount: Float,
    val description: String?,
    val type: MovementType
) : Serializable {
    constructor() : this(
        "", "", "", 0.0F, "", MovementType.INCOME
    )

    constructor(
        date: String, concept: String, amount: Float,
        description: String?, type: MovementType
    ) : this("", date, concept, amount, description, type)

    constructor(uid: String, m: Movement) : this(
        uid, m.date, m.concept, m.amount, m.description, m.type
    )

    fun amIEmpty(): Boolean {
        return uid == "" && concept == ""
    }
}