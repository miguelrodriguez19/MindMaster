package com.miguelrodriguez19.mindmaster.model.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils.ABSTRACT_ACTIVITY
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils.CALLER

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.getStringExtra(CALLER)) {
            context.getString(R.string.notification_caller_schedule) -> {
                val absActivityJson = intent.getStringExtra(ABSTRACT_ACTIVITY)
                val absActivity = absActivityJson?.let { AbstractActivity.fromJSON(it) } ?: return

                val notificationId = absActivity.notificationId
                val title = absActivity.getNotificationTitle(context)
                val message = absActivity.getNotificationMessage(context)
                val description = absActivity.description

                // Shows actual notification
                createSimpleNotification(context, notificationId, title, message, description)

                // If the notification is repetitive, reschedule the next alarm
                if (absActivity.getActivityRepetition() != Repetition.ONCE) {
                    NotificationUtils.createNextScheduleRepeatingNotification(context, notificationId ,absActivity)
                }
            }

            else -> {}
        }
    }

    private fun createSimpleNotification(
        context: Context, notificationId: Int, title: String, message: String, description: String?
    ) {
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = context.getString(R.string.global_notifications_channel_id)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (!description.isNullOrBlank()) {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(description))
        }

        val notification = notificationBuilder.build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}
