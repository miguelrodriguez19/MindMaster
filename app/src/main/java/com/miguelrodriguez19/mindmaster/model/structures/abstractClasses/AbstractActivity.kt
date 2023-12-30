package com.miguelrodriguez19.mindmaster.model.structures.abstractClasses

import android.content.Context
import android.graphics.Color
import com.google.gson.Gson
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Event
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Reminder
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Task
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.ActivityType
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.getDateFromDatetimeStr
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils
import com.miguelrodriguez19.mindmaster.model.utils.Preferences

abstract class AbstractActivity : java.io.Serializable {
    abstract var uid: String
    abstract val title: String
    abstract val description: String?
    abstract val category: List<String>?
    abstract val colorTag: String
    abstract val type: ActivityType
    //abstract val notificationId: Int

    abstract fun getNotificationTitle(context: Context) : String
    abstract fun getNotificationMessage(context: Context) : String
    fun toJSON() : String {
        return Gson().toJson(this)
    }

    fun fromJSON(json:String) : AbstractActivity {
        return Gson().fromJson(json, this::class.java)
    }

    fun createNotification(context: Context, repetition: Repetition){
        when (repetition) {
            Repetition.ONCE-> {
                NotificationUtils.scheduleOneTimeNotification(
                    context, Preferences.getNextNotificationId(), this
                )
            }

            else -> {
                try {
                    TODO("Develop a function in NotificationUtils that creates notifications with repetition.")
                } catch (e: NotImplementedError) {
                    e.printStackTrace()
                }
            }
        }
    }

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
            } catch (e: Exception) {
                e.printStackTrace()
                context.resources.getColor(R.color.primaryColor, null)
            }
        }

        fun getFormattedDateOf(absActivity: AbstractActivity): String = when (absActivity.type) {
            ActivityType.EVENT -> getDateFromDatetimeStr((absActivity as Event).startTime)
            ActivityType.REMINDER -> (absActivity as Reminder).dateTime
            ActivityType.TASK -> (absActivity as Task).dueDate
        }

    }
}