package com.miguelrodriguez19.mindmaster.model.structures.abstractClasses

import android.content.Context
import android.graphics.Color
import com.google.firebase.firestore.PropertyName
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Event
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Reminder
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Task
import com.miguelrodriguez19.mindmaster.model.structures.enums.ActivityType
import com.miguelrodriguez19.mindmaster.model.structures.enums.Repetition
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.getDateFromDatetime

abstract class AbstractActivity : java.io.Serializable {
    abstract var uid: String
    abstract val title: String
    abstract val description: String?
    abstract val category: List<String>?

    @get:PropertyName("color_tag")
    abstract val colorTag: String
    abstract val type: ActivityType

    companion object {
        fun getItemType(context: Context, type: ActivityType): String {
            return when (type) {
                ActivityType.EVENT -> context.getString(R.string.event)
                ActivityType.REMINDER -> context.getString(R.string.reminder)
                ActivityType.TASK -> context.getString(R.string.task)
            }
        }

        fun getColor(context: Context, hexColor: String): Int {
            return try {
                Color.parseColor(hexColor)
            } catch (e: IllegalArgumentException) {
                context.resources.getColor(R.color.primaryColor, null)
            }
        }

        fun getDateOf(absEvent: AbstractActivity): String = when (absEvent.type) {
            ActivityType.EVENT -> getDateFromDatetime((absEvent as Event).startTime)
            ActivityType.REMINDER -> (absEvent as Reminder).dateTime
            ActivityType.TASK -> (absEvent as Task).dueDate
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