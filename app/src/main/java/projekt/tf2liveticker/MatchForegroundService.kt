package projekt.tf2liveticker

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ServiceCompat
import androidx.work.ListenableWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import projekt.tf2liveticker.alarm_manager.AlarmReceiver
import projekt.tf2liveticker.helper_classes.NotificationHelper


class MatchForegroundService()
    : Service() {

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        //createNotificationChannel()
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

        serviceScope.launch {
            try {
                val storageRepository = MatchStorageRepository(applicationContext!!)

                val repository = MatchRepository()
                var matchesToUpdate = storageRepository.getActiveMatches()
                while (matchesToUpdate.isNotEmpty()) {
                    matchesToUpdate.forEach {

                        if (it.selected) {
                            repository.refreshResults(it, applicationContext)
                            storageRepository.updateMatch(it)

                        }
                    }
                    withContext(Dispatchers.IO) {
                        Thread.sleep(MinuteToMS.MINUTE_01)
                    }
                    matchesToUpdate = storageRepository.getActiveMatches()
                }
                ListenableWorker.Result.success()


            } catch (e: Exception) {
                Log.e("MatchService", "Match-Abfrage fehlgeschlagen", e)
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        return NotificationHelper.showForegroundNotification(this)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "match_service"
        private const val NOTIFICATION_ID = 1001
    }
}