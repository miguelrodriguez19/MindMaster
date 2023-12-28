package com.miguelrodriguez19.mindmaster.model.structures.dto.schedule

import com.google.firebase.firestore.PropertyName
import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.structures.enums.ActivityType
import com.miguelrodriguez19.mindmaster.model.structures.enums.Repetition

data class Reminder(
    override var uid: String,
    override val title: String,
    val dateTime: String,
    override val description: String?,
    override val category: List<String>?,
    override val colorTag: String,
    val repetition: Repetition,
    override val type: ActivityType
) : AbstractActivity() {
    constructor() : this("", "", "", null, null, "", Repetition.ONCE, ActivityType.REMINDER)
    constructor(
        title: String, dateTime: String, description: String?,
        category: List<String>?, colorTag: String, repetition: Repetition, type: ActivityType
    ) : this("", title, dateTime, description, category, colorTag,repetition, type)

    constructor(uid: String, r: Reminder) : this(
        uid, r.title, r.dateTime, r.description,
        r.category, r.colorTag, r.repetition, r.type
    )
}
