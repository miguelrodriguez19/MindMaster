package com.miguelrodriguez19.mindmaster.models.structures

import android.content.Context
import android.graphics.Color
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.getDateFromDatetime

data class EventsResponse(
    val date: String, val allEventsList: List<AbstractEvent>
) : java.io.Serializable

abstract class AbstractEvent : java.io.Serializable {
    abstract var uid: String
    abstract val title: String
    abstract val description: String?
    abstract val category: List<String>?
    abstract val colorTag: String
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
                EventType.REMINDER -> (absEvent as Reminder).dateTime
                EventType.TASK -> (absEvent as Task).dueDate
            }
        }

        fun getRepetitionString(r: Repetition): Int = when (r) {
            Repetition.ONCE -> R.string.time_once
            Repetition.DAILY -> R.string.time_daily
            Repetition.WEEKLY -> R.string.time_weekly
            Repetition.MONTHLY -> R.string.time_monthly
            Repetition.ANNUAL -> R.string.time_annual
        }

        fun getRepetitionOf(context: Context, repStr: String): Repetition = when (repStr) {
            context.getString(R.string.time_once) -> Repetition.ONCE
            context.getString(R.string.time_daily) -> Repetition.DAILY
            context.getString(R.string.time_weekly) -> Repetition.WEEKLY
            context.getString(R.string.time_monthly) -> Repetition.MONTHLY
            context.getString(R.string.time_annual) -> Repetition.ANNUAL
            else -> Repetition.ONCE
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
    override val colorTag: String,
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
        Repetition.ONCE,
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
        e.colorTag, e.type
    )
}

data class Reminder(
    override var uid: String,
    override val title: String,
    val dateTime: String,
    override val description: String?,
    override val category: List<String>?,
    override val colorTag: String,
    val repetition: Repetition,
    override val type: EventType
) : AbstractEvent() {
    constructor() : this("", "", "", null, null, "", Repetition.ONCE,EventType.REMINDER)
    constructor(
        title: String, dateTime: String, description: String?,
        category: List<String>?, colorTag: String, repetition: Repetition, type: EventType
    ) : this("", title, dateTime, description, category, colorTag,repetition, type)

    constructor(uid: String, r: Reminder) : this(
        uid, r.title, r.dateTime, r.description,
        r.category, r.colorTag, r.repetition, r.type
    )
}

data class Task(
    override var uid: String,
    override val title: String,
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

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class Status {
    COMPLETED, PENDING, IN_PROGRESS, CANCELLED
}

enum class Repetition {
    ONCE, DAILY, WEEKLY, MONTHLY, ANNUAL
}

enum class EventType {
    EVENT, REMINDER, TASK
}

