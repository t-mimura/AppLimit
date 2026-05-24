package studio.hazeray.applimit.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import studio.hazeray.applimit.R

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
                context.getString(R.string.monitor_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.monitor_channel_desc)
            }

            val warningChannel = NotificationChannel(
                WARNING_CHANNEL_ID,
                context.getString(R.string.warning_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.warning_channel_desc)
            }

            val updateChannel = NotificationChannel(
                UPDATE_CHANNEL_ID,
                context.getString(R.string.update_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.update_channel_desc)
            }

            // The previous "update" channel was registered with IMPORTANCE_LOW,
            // which Android won't let us raise programmatically — drop it so it
            // doesn't linger in system settings.
            notificationManager.deleteNotificationChannel(LEGACY_UPDATE_CHANNEL_ID)

            notificationManager.createNotificationChannel(monitorChannel)
            notificationManager.createNotificationChannel(warningChannel)
            notificationManager.createNotificationChannel(updateChannel)
        }
    }

    fun buildMonitorNotification(): Notification =
        NotificationCompat.Builder(context, MONITOR_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle(
                context.getString(R.string.monitor_notification_title)
            )
            .setContentText(
                context.getString(R.string.monitor_notification_text)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

    fun showWarningNotification(appName: String, remainingMinutes: Int) {
        val notification = NotificationCompat.Builder(context, WARNING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(
                context.getString(R.string.warning_notification_title)
            )
            .setContentText(
                context.getString(
                    R.string.warning_notification_text,
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
            .setContentTitle(
                context.getString(R.string.limit_reached_title)
            )
            .setContentText(
                context.getString(R.string.limit_reached_text, appName)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(LIMIT_NOTIFICATION_ID, notification)
    }

    fun cancelWarningNotification() {
        notificationManager.cancel(WARNING_NOTIFICATION_ID)
    }

    fun cancelLimitReachedNotification() {
        notificationManager.cancel(LIMIT_NOTIFICATION_ID)
    }

    fun showUpdateReadyNotification(version: String, apkUri: Uri) {
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.update_ready_title))
            .setContentText(context.getString(R.string.update_ready_text, version))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)
    }

    fun cancelUpdateReadyNotification() {
        notificationManager.cancel(UPDATE_NOTIFICATION_ID)
    }

    companion object {
        const val MONITOR_CHANNEL_ID = "monitor_service"
        const val WARNING_CHANNEL_ID = "warning"
        const val UPDATE_CHANNEL_ID = "update_v2"
        const val MONITOR_NOTIFICATION_ID = 1
        const val WARNING_NOTIFICATION_ID = 2
        const val LIMIT_NOTIFICATION_ID = 3
        const val UPDATE_NOTIFICATION_ID = 4
        private const val LEGACY_UPDATE_CHANNEL_ID = "update"
    }
}
