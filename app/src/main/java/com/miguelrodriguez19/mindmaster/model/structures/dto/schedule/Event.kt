package com.miguelrodriguez19.mindmaster.model.structures.dto.schedule

import android.content.Context
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.ActivityType
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils
import com.miguelrodriguez19.mindmaster.model.utils.Preferences

data class Event(
    override var uid: String,
    override val title: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    override val description: String?,
    val participants: List<String>,
    override val category: List<String>,
    val repetition: Repetition,
    override val colorTag: String,
    override val type: ActivityType,
    override val notificationId: Int
) : AbstractActivity() {
    constructor() : this(
        "", "", "", "", "", null,
        emptyList(), emptyList(), Repetition.ONCE, "", ActivityType.EVENT,
        Preferences.getNextNotificationId()
    )

    override fun getNotificationTitle(context: Context): String {
        return context.getString(R.string.event_notification_custom_title, this.title)
    }

    override fun getNotificationMessage(context: Context): String {
        val time = DateTimeUtils.getTimeFromDatetimeStr(this.startTime)
        return context.getString(R.string.event_notification_custom_message, this.title, time)
    }

}