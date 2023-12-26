package com.miguelrodriguez19.mindmaster.models.structures.dto.schedule

import com.google.firebase.firestore.PropertyName
import com.miguelrodriguez19.mindmaster.models.structures.abstractClasses.AbstractEvent
import com.miguelrodriguez19.mindmaster.models.structures.enums.EventType
import com.miguelrodriguez19.mindmaster.models.structures.enums.Repetition

data class Reminder(
    override var uid: String,
    override val title: String,
    @get:PropertyName("date_time")
    val dateTime: String,
    override val description: String?,
    override val category: List<String>?,
    override val colorTag: String,
    val repetition: Repetition,
    override val type: EventType
) : AbstractEvent() {
    constructor() : this("", "", "", null, null, "", Repetition.ONCE, EventType.REMINDER)
    constructor(
        title: String, dateTime: String, description: String?,
        category: List<String>?, colorTag: String, repetition: Repetition, type: EventType
    ) : this("", title, dateTime, description, category, colorTag,repetition, type)

    constructor(uid: String, r: Reminder) : this(
        uid, r.title, r.dateTime, r.description,
        r.category, r.colorTag, r.repetition, r.type
    )
}
