package projekt.tf2liveticker.alarm_manager

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.util.Log
import androidx.annotation.RequiresPermission


class AlarmScheduler {
    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(value = "android.permission.SCHEDULE_EXACT_ALARM", conditional = true)
    fun scheduleMatchAlarm(context: Context, triggerAtMillis: Long) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("AlarmScheduler", "Alarm gesetzt für $triggerAtMillis")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

}