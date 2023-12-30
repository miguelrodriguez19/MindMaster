package com.miguelrodriguez19.mindmaster.model.structures.dto.schedule

import android.content.Context
import com.miguelrodriguez19.mindmaster.R
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
    //override val notificationId: Int
) : AbstractActivity() {
    constructor() : this("", "", "", null, Priority.LOW, Status.PENDING, null, "", ActivityType.TASK)

    override fun getNotificationTitle(context: Context): String {
        val title = context.getString(R.string.task_notification_custom_title, this.title)
        return when (this.priority) {
            Priority.HIGH, Priority.URGENT -> "⚠ $title"
            else -> title
        }
    }

    override fun getNotificationMessage(context: Context): String {
        return context.getString(R.string.task_notification_custom_message, this.title)
    }
}