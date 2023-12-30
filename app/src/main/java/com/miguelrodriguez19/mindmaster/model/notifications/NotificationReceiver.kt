package com.miguelrodriguez19.mindmaster.model.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils.DESCRIPTION
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils.MESSAGE
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils.NOTIFICATION_ID
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils.TITLE

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val notificationId = intent?.getIntExtra(NOTIFICATION_ID, 1000) ?: 1000
        val title =
            intent?.getStringExtra(TITLE) ?: context.getString(R.string.default_notifications_title)
        val message = intent?.getStringExtra(MESSAGE) ?: context.getString(R.string.default_notifications_message)
        val description = intent?.getStringExtra(DESCRIPTION)

        createSimpleNotification(context, notificationId, title, message, description)
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
