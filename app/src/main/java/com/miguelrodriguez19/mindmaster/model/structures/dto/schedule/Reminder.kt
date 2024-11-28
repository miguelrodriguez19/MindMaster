package com.miguelrodriguez19.mindmaster.model.structures.dto.schedule

import android.content.Context
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.ActivityType
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils
import com.miguelrodriguez19.mindmaster.model.utils.Preferences

data class Reminder(
    override var uid: String,
    override val title: String,
    val dateTime: String,
    override val description: String?,
    override val category: List<String>?,
    override val colorTag: String,
    val repetition: Repetition,
    override val type: ActivityType,
    override val notificationId: Int
) : AbstractActivity() {
    constructor() : this(
        "", "", "", null, null, "",
        Repetition.ONCE, ActivityType.REMINDER, Preferences.getNextNotificationId()
    )

    override fun getNotificationTitle(context: Context): String {
        return context.getString(R.string.reminder_notification_custom_title, this.title)
    }

    override fun getNotificationMessage(context: Context): String {
        return context.getString(R.string.reminder_notification_custom_message, this.title)
    }
}
