package com.miguelrodriguez19.mindmaster.models.structures.abstractClasses

import android.content.Context
import android.graphics.Color
import com.google.firebase.firestore.PropertyName
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.models.structures.dto.schedule.Event
import com.miguelrodriguez19.mindmaster.models.structures.enums.EventType
import com.miguelrodriguez19.mindmaster.models.structures.dto.schedule.Reminder
import com.miguelrodriguez19.mindmaster.models.structures.enums.Repetition
import com.miguelrodriguez19.mindmaster.models.structures.dto.schedule.Task
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.getDateFromDatetime

data class EventsResponse(
    val date: String, val allEventsList: List<AbstractEvent>
) : java.io.Serializable

abstract class AbstractEvent : java.io.Serializable {
    abstract var uid: String
    abstract val title: String
    abstract val description: String?
    abstract val category: List<String>?
    @get:PropertyName("color_tag")
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
                EventType.EVENT -> getDateFromDatetime((absEvent as Event).startTime)
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