package com.example.applimit.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val monitorChannel = NotificationChannel(
                MONITOR_CHANNEL_ID,
                context.getString(com.example.applimit.R.string.monitor_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(com.example.applimit.R.string.monitor_channel_desc)
            }

            val warningChannel = NotificationChannel(
                WARNING_CHANNEL_ID,
                context.getString(com.example.applimit.R.string.warning_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(com.example.applimit.R.string.warning_channel_desc)
            }

            notificationManager.createNotificationChannel(monitorChannel)
            notificationManager.createNotificationChannel(warningChannel)
        }
    }

    fun buildMonitorNotification(): Notification =
        NotificationCompat.Builder(context, MONITOR_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle(
                context.getString(com.example.applimit.R.string.monitor_notification_title)
            )
            .setContentText(
                context.getString(com.example.applimit.R.string.monitor_notification_text)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

    fun showWarningNotification(appName: String, remainingMinutes: Int) {
        val notification = NotificationCompat.Builder(context, WARNING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(
                context.getString(com.example.applimit.R.string.warning_notification_title)
            )
            .setContentText(
                context.getString(
                    com.example.applimit.R.string.warning_notification_text,
                    appName,
                    remainingMinutes
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(WARNING_NOTIFICATION_ID, notification)
    }

    fun showLimitReachedNotification(appName: String) {
        val notification = NotificationCompat.Builder(context, WARNING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(context.getString(com.example.applimit.R.string.limit_reached_title))
            .setContentText(
                context.getString(com.example.applimit.R.string.limit_reached_text, appName)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(LIMIT_NOTIFICATION_ID, notification)
    }

    fun cancelWarningNotifications() {
        notificationManager.cancel(WARNING_NOTIFICATION_ID)
        notificationManager.cancel(LIMIT_NOTIFICATION_ID)
    }

    companion object {
        const val MONITOR_CHANNEL_ID = "monitor_service"
        const val WARNING_CHANNEL_ID = "warning"
        const val MONITOR_NOTIFICATION_ID = 1
        const val WARNING_NOTIFICATION_ID = 2
        const val LIMIT_NOTIFICATION_ID = 3
    }
}
