package com.miguelrodriguez19.mindmaster.model.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.miguelrodriguez19.mindmaster.model.notifications.NotificationReceiver
import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition.ANNUAL
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition.DAILY
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition.MONTHLY
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition.ONCE
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition.WEEKLY
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition.valueOf
import com.miguelrodriguez19.mindmaster.model.structures.exceptions.ExceptionHolder
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.getCurrentDate
import com.miguelrodriguez19.mindmaster.model.utils.Preferences.getUserPreferredNotificationHour
import com.miguelrodriguez19.mindmaster.model.utils.Preferences.getUserPreferredNotificationMinute
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationUtils {
    private const val TAG = "NotificationUtils"

    const val NOTIFICATION_ID = "notification_id"
    const val TITLE = "title"
    const val MESSAGE = "message"
    const val DESCRIPTION = "description"
    const val REPETITION = "repetition"
    const val EXTRAS = "extras"
    const val DATE = "date"

    fun createActivityNotification(appContext: Context, absActivity: AbstractActivity) {
        val activityDate = AbstractActivity.getFormattedDateOf(absActivity) // "dd-MM-yyyy"
        if(DateTimeUtils.compareDates(activityDate, getCurrentDate()) <= 0)
            return // Notification is not created because it has already happened

        val activityData = hashMapOf(
            TITLE to absActivity.getNotificationTitle(appContext),
            MESSAGE to absActivity.getNotificationMessage(appContext),
            DESCRIPTION to absActivity.description,
            REPETITION to absActivity.getActivityRepetition().toString(),
            NOTIFICATION_ID to absActivity.notificationId.toString(),
            DATE to activityDate
        )

        Log.d(TAG, "createActivityNotification: activityData -> $activityData")

        val alarmTimeInMillis = getAlarmTime(activityDate)
        scheduleNotification(appContext, alarmTimeInMillis, activityData)
    }

    fun createNextScheduleRepeatingNotification(
        context: Context,
        activityData: Map<String, String?>
    ) {
        if (activityData.count() != 6)
            ExceptionHolder.illegalState("activityData is not filled correctly: $activityData")

        // Calculates the new notification date by adding the repetition
        val activityDate = activityData[DATE]?.let {
            DateTimeUtils.getLocalDateFromString(it)
        } ?: ExceptionHolder.illegalArgument("At this point date cant be null")
        val repetitionDate = sumRepetition(activityDate, valueOf(activityData[REPETITION]!!))

        val alarmTimeInMillis = getAlarmTime(repetitionDate)

        scheduleNotification(context, alarmTimeInMillis, activityData)
    }

    private fun scheduleNotification(appContext: Context, alarmTime: Long, activityData: Map<String, String?>) {
        val intent = Intent(appContext, NotificationReceiver::class.java).apply {
            putExtra(EXTRAS, Gson().toJson(activityData))
        }

        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            activityData[NOTIFICATION_ID]!!.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        Log.d(
            TAG,
            "scheduleNotification: Notification scheduled at ${LocalDateTime.now()} " +
                    "to ring on ${DateTimeUtils.formatEpochMillis(alarmTime)} " +
                    "with identifier ${activityData[NOTIFICATION_ID]}"
        )

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (hasPermission) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,alarmTime,pendingIntent)
        } else{
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,alarmTime,pendingIntent)
        }
    }

    private fun getAlarmTime(deadLineDateStr: String): Long =
        getAlarmTime(DateTimeUtils.getLocalDateFromString(deadLineDateStr))

    private fun getAlarmTime(deadlineDate: LocalDate): Long {
        val notificationTime = LocalDateTime.of(deadlineDate, LocalTime.MIN)
            .minusDays(1) // 24 hours after due date
            .withHour(getUserPreferredNotificationHour())
            .withMinute(getUserPreferredNotificationMinute())
            .withSecond(0)

        return notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun removeNotifications(appContext: Context, notificationId: Int) {
        val intent = Intent(appContext, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        pendingIntent?.let {
            val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun sumRepetition(date: LocalDate, repetition: Repetition): LocalDate {
        return when (repetition) {
            ONCE -> date
            DAILY -> date.plusDays(1)
            WEEKLY -> date.plusWeeks(1)
            MONTHLY -> date.plusMonths(1)
            ANNUAL -> date.plusYears(1)
        }
    }
}