package projekt.tf2liveticker.alarm_manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import projekt.tf2liveticker.MatchForegroundService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, p1: Intent?) {
        val serviceIntent = Intent(
            context,
            MatchForegroundService::class.java
        )

        ContextCompat.startForegroundService(
            context,
            serviceIntent
        )

    }
}