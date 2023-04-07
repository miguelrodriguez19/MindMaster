package com.miguelrodriguez19.mindmaster.models

import android.content.Context
import android.graphics.Color
import com.miguelrodriguez19.mindmaster.R
import java.util.*

data class EventsResponse(
    val date: String,
    val allEventsList: List<AbstractEvents>
) : java.io.Serializable

abstract class AbstractEvents : java.io.Serializable {
    abstract val cod: String
    abstract val title: String
    abstract val description: String?
    abstract val category: String?
    abstract val color_tag: String
    abstract val type: EventType

    fun getItemType(context: Context, type: EventType): String {
        return when (type) {
            EventType.EVENT -> context.getString(R.string.event)
            EventType.REMINDER -> context.getString(R.string.reminder)
            EventType.TASK -> context.getString(R.string.task)
            else -> ""
        }
    }

    fun getColor(context: Context, hexColor: String): Int {
        return Color.parseColor(hexColor) ?: context.resources.getColor(R.color.primaryColor, null)
    }
}

data class Event(
    override val cod: String,
    override val title: String,
    val start_time: String,
    val end_time: String,
    val location: String,
    override val description: String?,
    val participants: List<String>?,
    override val category: String?,
    val repetition: Repetition,
    override val color_tag: String,
    override val type: EventType
) : AbstractEvents()

data class Reminder(
    override val cod: String,
    override val title: String,
    val reminder_time: String,
    override val description: String?,
    override val category: String?,
    override val color_tag: String,
    override val type: EventType
) : AbstractEvents()

data class Task(
    override val cod: String,
    override val title: String,
    val due_date: String,
    override val description: String?,
    val priority: Priority,
    val status: Status,
    override val category: String?,
    override val color_tag: String,
    override val type: EventType
) : AbstractEvents()

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class Status {
    COMPLETED, PENDING, IN_PROGRESS, CANCELLED
}

enum class Repetition {
    ONCE,DAILY, WEEKLY, MONTHLY, ANNUAL
}

enum class EventType {
    EVENT, REMINDER, TASK
}

