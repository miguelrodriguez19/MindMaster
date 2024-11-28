package com.miguelrodriguez19.mindmaster.model.utils

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

object PermissionsUtils {
    fun checkNotificationPermission(activity: Activity, granted: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 and higher
            checkExactAlarmPermission(activity) { granted(it) }
        }
    }

    fun checkExactAlarmPermission(activity: Activity, granted: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                activity.startActivity(intent)
                granted(false)
            } else {
                granted(true)
            }
        } else {
            granted(true)
        }
    }

}
