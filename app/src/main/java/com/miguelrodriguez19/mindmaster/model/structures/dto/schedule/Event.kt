package com.miguelrodriguez19.mindmaster.model.structures.dto.schedule

import com.google.firebase.firestore.PropertyName
import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.structures.enums.ActivityType
import com.miguelrodriguez19.mindmaster.model.structures.enums.Repetition

data class Event(
    override var uid: String,
    override val title: String,
    @get:PropertyName("start_time")
    val startTime: String,
    @get:PropertyName("end_time")
    val endTime: String,
    val location: String,
    override val description: String?,
    val participants: List<String>,
    override val category: List<String>,
    val repetition: Repetition,
    override val colorTag: String,
    override val type: ActivityType
) : AbstractActivity() {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        null,
        emptyList(),
        emptyList(),
        Repetition.ONCE,
        "",
        ActivityType.EVENT
    )

    constructor(
        title: String, startTime: String, endTime: String,
        location: String, description: String?, participants: List<String>,
        category: List<String>, repetition: Repetition, colorTag: String, type: ActivityType
    ) : this(
        "", title, startTime, endTime, location,
        description, participants, category, repetition,
        colorTag, type
    )

    constructor(uid: String, e: Event) : this(
        uid, e.title, e.startTime, e.endTime, e.location,
        e.description, e.participants, e.category, e.repetition,
        e.colorTag, e.type
    )
}