package com.miguelrodriguez19.mindmaster.model.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils.DESCRIPTION
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils.EXTRAS
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils.MESSAGE
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils.NOTIFICATION_ID
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils.REPETITION
import com.miguelrodriguez19.mindmaster.model.utils.NotificationUtils.TITLE

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val extrasStr = intent?.getStringExtra(EXTRAS)
        val extrasMap = extrasStr?.let { Gson().fromJson(it, HashMap<String, String>()::class.java) }

        if (extrasMap != null) {
            val title = extrasMap.getOrDefault(TITLE, "")
            val message = extrasMap.getOrDefault(MESSAGE, "")
            val description = extrasMap.getOrDefault(DESCRIPTION, null)
            val notificationId:String = extrasMap.getOrDefault(NOTIFICATION_ID, "0")
            val repetition = extrasMap.getOrDefault(REPETITION, "ONCE")

            createSimpleNotification(
                context, notificationId.toInt(), title, message, description
            )

            // If repetition's activity isn't ONCE, we calculate the next notification
            if (Repetition.valueOf(repetition) != Repetition.ONCE) {
                NotificationUtils.createNextScheduleRepeatingNotification(context, extrasMap)
            }
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
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
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
