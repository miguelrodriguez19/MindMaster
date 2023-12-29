package com.miguelrodriguez19.mindmaster.model.structures.dto.schedule

import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.ActivityType
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Priority
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Status

data class Task(
    override var uid: String,
    override val title: String,
    val dueDate: String,
    override val description: String?,
    val priority: Priority,
    val status: Status,
    override val category: List<String>?,
    override val colorTag: String,
    override val type: ActivityType
) : AbstractActivity() {
    constructor() : this("", "", "", null, Priority.LOW, Status.PENDING, null, "", ActivityType.TASK)

    constructor(
        title: String, dueDate: String, description: String?,
        priority: Priority, status: Status, category: List<String>?,
        colorTag: String, type: ActivityType
    ) : this("", title, dueDate, description, priority, status, category, colorTag, type)

    constructor(uid: String, t: Task) : this(
        uid, t.title, t.dueDate, t.description,
        t.priority, t.status, t.category, t.colorTag, t.type
    )
}