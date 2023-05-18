package com.miguelrodriguez19.mindmaster.models

import android.content.Context
import android.graphics.Color
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.utils.Toolkit.getDateFromDatetime

data class EventsResponse(
    val date: String, val allEventsList: List<AbstractEvent>
) : java.io.Serializable

abstract class AbstractEvent : java.io.Serializable {
    abstract var uid: String
    abstract val title: String
    abstract val description: String?
    abstract val category: List<String>?
    abstract val color_tag: String
    abstract val type: EventType

    companion object {
        fun getItemType(context: Context, type: EventType): String {
            return when (type) {
                EventType.EVENT -> context.getString(R.string.event)
                EventType.REMINDER -> context.getString(R.string.reminder)
                EventType.TASK -> context.getString(R.string.task)
            }
        }

        fun getColor(context: Context, hexColor: String): Int {
            return try {
                Color.parseColor(hexColor)
            } catch (e: IllegalArgumentException) {
                context.resources.getColor(R.color.primaryColor, null)
            }
        }

        fun getDateOf(absEvent: AbstractEvent): String {
            return when (absEvent.type) {
                EventType.EVENT -> getDateFromDatetime((absEvent as Event).start_time)
                EventType.REMINDER -> (absEvent as Reminder).date_time
                EventType.TASK -> (absEvent as Task).due_date
            }
        }
    }
}

data class Event(
    override var uid: String,
    override val title: String,
    val start_time: String,
    val end_time: String,
    val location: String,
    override val description: String?,
    val participants: List<String>,
    override val category: List<String>,
    val repetition: Repetition,
    override val color_tag: String,
    override val type: EventType
) : AbstractEvent() {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        null,
        emptyList(),
        emptyList(),
        Repetition.NONE,
        "",
        EventType.EVENT
    )

    constructor(
        title: String, start_time: String, end_time: String,
        location: String, description: String?, participants: List<String>,
        category: List<String>, repetition: Repetition, color_tag: String, type: EventType
    ) : this(
        "", title, start_time, end_time, location,
        description, participants, category, repetition,
        color_tag, type
    )

    constructor(uid: String, e: Event) : this(
        uid, e.title, e.start_time, e.end_time, e.location,
        e.description, e.participants, e.category, e.repetition,
        e.color_tag, e.type
    )
}

data class Reminder(
    override var uid: String,
    override val title: String,
    val date_time: String,
    override val description: String?,
    override val category: List<String>?,
    override val color_tag: String,
    override val type: EventType
) : AbstractEvent() {
    constructor() : this("", "", "", null, null, "", EventType.REMINDER)
    constructor(
        title: String, date_time: String, description: String?,
        category: List<String>?, color_tag: String, type: EventType
    ) : this("", title, date_time, description, category, color_tag, type)

    constructor(uid: String, r: Reminder) : this(
        uid, r.title, r.date_time, r.description,
        r.category, r.color_tag, r.type
    )
}

data class Task(
    override var uid: String,
    override val title: String,
    val due_date: String,
    override val description: String?,
    val priority: Priority,
    val status: Status,
    override val category: List<String>?,
    override val color_tag: String,
    override val type: EventType
) : AbstractEvent() {
    constructor() : this("", "", "", null, Priority.LOW, Status.PENDING, null, "", EventType.TASK)

    constructor(
        title: String, due_date: String, description: String?,
        priority: Priority, status: Status, category: List<String>?,
        color_tag: String, type: EventType
    ) : this("", title, due_date, description, priority, status, category, color_tag, type)

    constructor(uid: String, t: Task) : this(
        uid, t.title, t.due_date, t.description,
        t.priority, t.status, t.category, t.color_tag, t.type
    )
}

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class Status {
    COMPLETED, PENDING, IN_PROGRESS, CANCELLED
}

enum class Repetition {
    NONE, DAILY, WEEKLY, MONTHLY, ANNUAL
}

enum class EventType {
    EVENT, REMINDER, TASK
}

