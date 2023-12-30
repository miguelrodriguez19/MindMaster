package com.miguelrodriguez19.mindmaster.model.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.miguelrodriguez19.mindmaster.model.notifications.NotificationReceiver
import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.defaultDateFormat
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.getCurrentDate

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object NotificationUtils {
    const val NOTIFICATION_ID = "notification_id"
    const val TITLE = "title"
    const val MESSAGE = "message"
    const val DESCRIPTION = "description"

    fun scheduleOneTimeNotification(appContext: Context, notificationId: Int, absActivity: AbstractActivity) {
        if (!checkExactAlarmPermission(appContext)) {
            return
        }

        val deadLineDate = AbstractActivity.getFormattedDateOf(absActivity) // "dd-MM-yyyy"

        if(DateTimeUtils.compareDates(deadLineDate, getCurrentDate()) <= 0) return // Notification is not created because it has already happened

        val alarmTimeInMillis = getAlarmTime(deadLineDate)

        val title = absActivity.getNotificationTitle(appContext)
        val message =  absActivity.getNotificationMessage(appContext)

        val intent = Intent(appContext, NotificationReceiver::class.java).apply {
            putExtra(NOTIFICATION_ID, notificationId)
            putExtra(TITLE, title)
            putExtra(MESSAGE, message)
            putExtra(DESCRIPTION, absActivity.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent)
    }

    private fun getAlarmTime(deadLineDateStr: String): Long {
        val dateFormat = SimpleDateFormat(defaultDateFormat, Locale.getDefault())
        val deadline = dateFormat.parse(deadLineDateStr) ?: return -1
        val calendar = Calendar.getInstance().apply {
            time = deadline
            add(Calendar.DAY_OF_YEAR, -1) // 24 hours left for notification
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