package com.miguelrodriguez19.mindmaster.models.structures.dto.schedule

import com.google.firebase.firestore.PropertyName
import com.miguelrodriguez19.mindmaster.models.structures.abstractClasses.AbstractEvent
import com.miguelrodriguez19.mindmaster.models.structures.enums.EventType
import com.miguelrodriguez19.mindmaster.models.structures.enums.Priority
import com.miguelrodriguez19.mindmaster.models.structures.enums.Status

data class Task(
    override var uid: String,
    override val title: String,
    @get:PropertyName("due_date")
    val dueDate: String,
    override val description: String?,
    val priority: Priority,
    val status: Status,
    override val category: List<String>?,
    override val colorTag: String,
    override val type: EventType
) : AbstractEvent() {
    constructor() : this("", "", "", null, Priority.LOW, Status.PENDING, null, "", EventType.TASK)

    constructor(
        title: String, dueDate: String, description: String?,
        priority: Priority, status: Status, category: List<String>?,
        colorTag: String, type: EventType
    ) : this("", title, dueDate, description, priority, status, category, colorTag, type)

    constructor(uid: String, t: Task) : this(
        uid, t.title, t.dueDate, t.description,
        t.priority, t.status, t.category, t.colorTag, t.type
    )
}