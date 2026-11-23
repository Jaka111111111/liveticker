package projekt.tf2liveticker.helper_classes


import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import projekt.tf2liveticker.R

object NotificationHelper {
    const val MATCH_CHANNEL_ID = "match_notifications"
    const val DEBUG_CHANNEL_ID = "debug_notifications"
    const val FOREGROUND_CHANNEL_ID = "foreground_notifications"

    fun createMatchNotificationChannel(context: Context) {
        val name = "Match Notifications"
        val descriptionText = "Blablabla"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(MATCH_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun createDebugNotificationChannel(context: Context) {
        val name = "Debug Notifications"
        val descriptionText = "Info über Prozesse"
        val importance = NotificationManager.IMPORTANCE_MIN
        val channel = NotificationChannel(DEBUG_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun createForegroundNotificationChannel(context: Context) {
        val name = "Foreground Service Notifications"
        val descriptionText = "Used for the Foreground Service Notification"
        val importance = NotificationManager.IMPORTANCE_MIN
        val channel = NotificationChannel(FOREGROUND_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showMatchNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int,
        onGoing: Boolean = true
    ) {

        val notification = NotificationCompat.Builder(
            context,
            MATCH_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOngoing(onGoing)
            .setOnlyAlertOnce(false)
            .build()

        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat
            .from(context)
            .notify(notificationId, notification)
    }


    fun showDebugNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int
    ) {

        val notification = NotificationCompat.Builder(
            context,
            DEBUG_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setAutoCancel(false)
            .build()

        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat
            .from(context)
            .notify(notificationId, notification)
    }

    fun showForegroundNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle("TF2 Live Ticker is running...")
            .setContentText("Looking through logs and team pages")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }
}