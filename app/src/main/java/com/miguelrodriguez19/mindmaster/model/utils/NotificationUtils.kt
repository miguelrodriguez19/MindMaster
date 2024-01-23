package com.miguelrodriguez19.mindmaster.model.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.notifications.NotificationReceiver
import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition.*
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.defaultDateFormat
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.getCurrentDate
import com.miguelrodriguez19.mindmaster.model.utils.Preferences.getUserPreferredNotificationHour
import com.miguelrodriguez19.mindmaster.model.utils.Preferences.getUserPreferredNotificationMinute
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object NotificationUtils {
    const val CALLER = "caller"
    const val NOTIFICATION_ID = "notification_id"
    const val TITLE = "title"
    const val MESSAGE = "message"
    const val DESCRIPTION = "description"
    const val ABSTRACT_ACTIVITY = "abstract_activity"

    fun createScheduleNotification(context: Context, absActivity: AbstractActivity) {
        if (!checkExactAlarmPermission(context)) return

        val deadLineDate = AbstractActivity.getFormattedDateOf(absActivity) // "dd-MM-yyyy"

        // Notification is not created because it has already happened
        if (DateTimeUtils.compareDates(deadLineDate,getCurrentDate()) <= 0) return

        val alarmTimeInMillis = calculateOneTimeAlarmTime(deadLineDate)
        val absActivityJson = absActivity.toJSON()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(CALLER, context.getString(R.string.notification_caller_schedule))
            putExtra(ABSTRACT_ACTIVITY, absActivityJson)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, absActivity.notificationId, intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent)
    }

    fun createNextScheduleRepeatingNotification(context: Context, notificationId: Int, absActivity: AbstractActivity) {
        if (!checkExactAlarmPermission(context)) return

        val absActivityJson = absActivity.toJSON()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(CALLER, context.getString(R.string.notification_caller_schedule))
            putExtra(ABSTRACT_ACTIVITY, absActivityJson)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val deadLineDate = AbstractActivity.getFormattedDateOf(absActivity)
        val alarmTimeInMillis = calculateOneTimeAlarmTime(deadLineDate)
        val nextAlarmTime = calculateNextRepeatingAlarmTime(absActivity.getActivityRepetition(), alarmTimeInMillis)

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTime, pendingIntent)
    }

    private fun calculateOneTimeAlarmTime(deadLineDateStr: String): Long {
        val dateFormat = SimpleDateFormat(defaultDateFormat, Locale.getDefault())
        val deadline = dateFormat.parse(deadLineDateStr) ?: return -1
        val calendar = Calendar.getInstance().apply {
            time = deadline
            add(Calendar.DAY_OF_YEAR, -1) // 24 hours left for notification
            set(Calendar.HOUR_OF_DAY, getUserPreferredNotificationHour())
            set(Calendar.MINUTE, getUserPreferredNotificationMinute())
            set(Calendar.SECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun calculateNextRepeatingAlarmTime(repetition: Repetition, currentTime: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime

            when (repetition) {
                DAILY -> add(Calendar.DAY_OF_YEAR, 1)
                WEEKLY -> add(Calendar.WEEK_OF_YEAR, 1)
                MONTHLY -> add(Calendar.MONTH, 1)
                ANNUAL -> add(Calendar.YEAR, 1)
                else -> {} // No action needed for ONCE
            }

            // Set specific time for alarm (by default 8:00 AM)
            set(Calendar.HOUR_OF_DAY, getUserPreferredNotificationHour())
            set(Calendar.MINUTE, getUserPreferredNotificationMinute())
            set(Calendar.SECOND, 0)
        }

        return calendar.timeInMillis
    }

    private fun checkExactAlarmPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(settingsIntent)
                return false
            }
        }
        return true
    }
}